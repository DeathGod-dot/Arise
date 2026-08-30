package com.example.arise.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri

class AriseAlarmReceiver : BroadcastReceiver() {

    companion object {
        var activeMediaPlayer: MediaPlayer? = null

        fun stopAlarmMedia() {
            try {
                activeMediaPlayer?.apply {
                    if (isPlaying) {
                        stop()
                    }
                    reset()
                    release()
                }
            } catch (_: Exception) {
            } finally {
                activeMediaPlayer = null
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        stopAlarmMedia()
        val audioUriString = intent?.getStringExtra("ALARM_AUDIO_URI")

        if (!audioUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(audioUriString)
                val scheme = uri.scheme?.lowercase()
                if (scheme == "content" || scheme == "android.resource" || scheme == "file") {
                    val player = MediaPlayer().apply {
                        setAudioAttributes(
                            AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build()
                        )
                        setDataSource(context.applicationContext, uri)
                        isLooping = true
                        prepare()
                        start()
                    }
                    activeMediaPlayer = player
                }
            } catch (_: Exception) {
                stopAlarmMedia()
            }
        }

        NotificationHelper.showAlarmNotificationWithActions(
            context = context,
            title = "SYSTEM ALARM: DAILY QUEST TIME!",
            message = "System Alert: Time for your daily physical and focus quests. Prepare for combat!",
            audioUri = audioUriString
        )

        // Reschedule for next day
        val hour = intent?.getIntExtra("ALARM_HOUR", -1) ?: -1
        val minute = intent?.getIntExtra("ALARM_MINUTE", -1) ?: -1
        if (hour != -1 && minute != -1) {
            AlarmScheduler.scheduleDailyAlarm(context, hour, minute, audioUriString)
        }
    }
}
