package com.example.arise.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.arise.MainActivity
import com.example.arise.R
import com.example.arise.data.AriseRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class StudyTimerService : Service() {

    @Inject
    lateinit var repository: AriseRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null

    private var activeSubId: String? = null
    private var activeQuestId: String? = null
    private var sessionLabel: String = "Study Session"
    private var targetSeconds: Int = 7200
    private var currentElapsedSeconds: Int = 0
    private var isRunning: Boolean = false
    private var lastMilestoneAnnounced: Int = 0

    // Wall-clock anchors for accurate timing
    private var wallClockStartMs: Long = 0L
    private var initialElapsedOffset: Int = 0

    companion object {
        const val CHANNEL_ID = "arise_study_timer_channel"
        const val NOTIFICATION_ID = 3001

        const val ACTION_START = "ACTION_START_STUDY_TIMER"
        const val ACTION_PAUSE = "ACTION_PAUSE_STUDY_TIMER"
        const val ACTION_STOP = "ACTION_STOP_STUDY_TIMER"

        const val EXTRA_SUB_ID = "EXTRA_SUB_ID"
        const val EXTRA_QUEST_ID = "EXTRA_QUEST_ID"
        const val EXTRA_LABEL = "EXTRA_LABEL"
        const val EXTRA_TARGET_SEC = "EXTRA_TARGET_SEC"
        const val EXTRA_ELAPSED_SEC = "EXTRA_ELAPSED_SEC"

        private val _activeTimerState = kotlinx.coroutines.flow.MutableStateFlow<TimerState?>(null)
        val activeTimerState: kotlinx.coroutines.flow.StateFlow<TimerState?> = _activeTimerState

        fun start(
            context: Context,
            subId: String,
            questId: String,
            label: String,
            targetSec: Int,
            elapsedSec: Int
        ) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_SUB_ID, subId)
                putExtra(EXTRA_QUEST_ID, questId)
                putExtra(EXTRA_LABEL, label)
                putExtra(EXTRA_TARGET_SEC, targetSec)
                putExtra(EXTRA_ELAPSED_SEC, elapsedSec)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun pause(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, StudyTimerService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    data class TimerState(
        val subId: String,
        val questId: String,
        val label: String,
        val elapsedSeconds: Int,
        val targetSeconds: Int,
        val isRunning: Boolean
    )

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val subId = intent.getStringExtra(EXTRA_SUB_ID)
                val questId = intent.getStringExtra(EXTRA_QUEST_ID)
                if (subId == null || questId == null) {
                    // Must call startForeground before stopping to avoid RemoteServiceException
                    startForegroundCompat()
                    stopSelf()
                    return START_NOT_STICKY
                }
                val label = intent.getStringExtra(EXTRA_LABEL) ?: "Study Session"
                val targetSec = intent.getIntExtra(EXTRA_TARGET_SEC, 7200)
                val elapsedSec = intent.getIntExtra(EXTRA_ELAPSED_SEC, 0)

                startStudyTimer(subId, questId, label, targetSec, elapsedSec)
            }
            ACTION_PAUSE -> pauseStudyTimer()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    private fun startStudyTimer(
        subId: String,
        questId: String,
        label: String,
        targetSec: Int,
        elapsedSec: Int
    ) {
        activeSubId = subId
        activeQuestId = questId
        sessionLabel = label
        targetSeconds = targetSec
        initialElapsedOffset = elapsedSec
        currentElapsedSeconds = elapsedSec
        isRunning = true
        lastMilestoneAnnounced = currentElapsedSeconds / 1800

        // Anchor wall-clock time: elapsed = initialElapsedOffset + (now - wallClockStartMs) / 1000
        wallClockStartMs = android.os.SystemClock.elapsedRealtime()

        startForegroundCompat()

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var lastSavedSecond = currentElapsedSeconds

            while (isRunning) {
                delay(500L) // Poll at 500ms for smooth UI updates

                // Calculate elapsed from wall clock — immune to drift
                val wallElapsedMs = android.os.SystemClock.elapsedRealtime() - wallClockStartMs
                currentElapsedSeconds = initialElapsedOffset + (wallElapsedMs / 1000).toInt()

                if (currentElapsedSeconds >= targetSeconds) {
                    currentElapsedSeconds = targetSeconds
                }

                _activeTimerState.value = TimerState(
                    subId = subId,
                    questId = questId,
                    label = label,
                    elapsedSeconds = currentElapsedSeconds,
                    targetSeconds = targetSec,
                    isRunning = true
                )

                // Save to DB every 5 seconds of elapsed time
                if (currentElapsedSeconds / 5 > lastSavedSecond / 5) {
                    lastSavedSecond = currentElapsedSeconds
                    repository.updateSubObjective(subId, currentElapsedSeconds, false)
                }

                // 30-Minute Interval Alert Check (1800s, 3600s, 5400s, 7200s)
                val currentMilestone = currentElapsedSeconds / 1800
                if (currentMilestone > lastMilestoneAnnounced && currentMilestone > 0) {
                    lastMilestoneAnnounced = currentMilestone
                    val minsCompleted = currentMilestone * 30
                    NotificationHelper.showStudyMilestoneNotification(applicationContext, minsCompleted)
                }

                updateNotification()

                // Timer complete
                if (currentElapsedSeconds >= targetSeconds) break
            }

            if (currentElapsedSeconds >= targetSeconds) {
                isRunning = false
                repository.updateSubObjective(subId, targetSeconds, false)
                NotificationHelper.showStudyMilestoneNotification(applicationContext, targetSeconds / 60)
                _activeTimerState.value = null
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun pauseStudyTimer() {
        isRunning = false
        timerJob?.cancel()
        val subId = activeSubId
        val elapsed = currentElapsedSeconds
        val target = targetSeconds
        _activeTimerState.value = _activeTimerState.value?.copy(isRunning = false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        // Save progress in a non-cancellable scope before destroying service
        if (subId != null) {
            serviceScope.launch(NonCancellable) {
                val isComplete = elapsed >= target
                repository.updateSubObjective(subId, elapsed, isComplete)
                withContext(Dispatchers.Main) {
                    stopSelf()
                }
            }
        } else {
            stopSelf()
        }
    }

    private fun startForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "ARISE Background Study Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows active background study countdown and milestone alerts."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val remaining = (targetSeconds - currentElapsedSeconds).coerceAtLeast(0)
        val h = remaining / 3600
        val m = (remaining % 3600) / 60
        val s = remaining % 60
        val timeStr = String.format("%02d:%02d:%02d", h, m, s)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🧠 $sessionLabel IN PROGRESS")
            .setContentText("Time Remaining: $timeStr | Milestone Alert Active")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Time Remaining: $timeStr\n30-Min Alert Active: 'Keep advancing, you are doing good!'"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, buildNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        _activeTimerState.value = null
    }
}
