package com.example.arise.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.arise.MainActivity
import com.example.arise.R

object NotificationHelper {
    const val CHANNEL_ID = "arise_system_notifications"
    const val CHANNEL_NAME = "ARISE System Alerts"
    const val CHANNEL_DESC = "Notifications for daily quests, level-ups, and system alerts."

    private fun areNotificationsEnabled(context: Context): Boolean {
        return context.getSharedPreferences("arise_settings", Context.MODE_PRIVATE)
            .getBoolean("systemReminders", true)
    }

    private fun isSoundEnabled(context: Context): Boolean {
        return context.getSharedPreferences("arise_settings", Context.MODE_PRIVATE)
            .getBoolean("soundEffectsEnabled", true)
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showSystemNotification(context: Context, title: String, message: String, notificationId: Int = System.currentTimeMillis().toInt()) {
        if (!areNotificationsEnabled(context)) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚔️ $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+
        }
    }

    fun showQuestReminder(context: Context, incompleteCount: Int) {
        val title = "SYSTEM ALERT: DAILY QUESTS PENDING"
        val message = "You have $incompleteCount incomplete quest(s) remaining today. Clear your training before midnight to avoid penalty!"
        showSystemNotification(context, title, message, notificationId = 1001)
    }

    fun showLevelUpNotification(context: Context, rankName: String) {
        val title = "SYSTEM NOTICE: HUNTER RANK ASCENSION"
        val message = "Congratulations! Your combat power has increased. Current Rank: $rankName!"
        showSystemNotification(context, title, message, notificationId = 1002)
    }

    fun showQuestCompletedNotification(context: Context, questTitle: String, expReward: Int, mpReward: Int) {
        val title = "QUEST COMPLETED: $questTitle"
        val message = "System Notice: You cleared '$questTitle'! Earned +$expReward EXP & +$mpReward MP. Claim your rewards in Quest Log!"
        showSystemNotification(context, title, message, notificationId = (System.currentTimeMillis() % 100000).toInt())
    }

    fun showSubObjectiveCompletedNotification(context: Context, subLabel: String) {
        val title = "OBJECTIVE COMPLETED"
        val message = "System Notice: You completed '$subLabel'! Keep advancing to clear the quest."
        showSystemNotification(context, title, message, notificationId = (System.currentTimeMillis() % 100000).toInt())
    }

    fun showStudyMilestoneNotification(context: Context, minsCompleted: Int) {
        val soundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)
        val title = "STUDY SESSION: $minsCompleted MINS COMPLETED!"
        val message = "Keep advancing, you are doing good!"
        
        if (!areNotificationsEnabled(context)) return

        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🧠 $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            
        if (isSoundEnabled(context)) {
            builder.setSound(soundUri)
            builder.setDefaults(NotificationCompat.DEFAULT_SOUND or NotificationCompat.DEFAULT_VIBRATE)
        }

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(2000 + minsCompleted, builder.build())
            }
        } catch (_: SecurityException) {
        }
    }

    fun showAlarmNotificationWithActions(
        context: Context,
        title: String,
        message: String,
        audioUri: String? = null
    ) {
        if (!areNotificationsEnabled(context)) return
        createNotificationChannel(context)

        val stopIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_STOP_ALARM
            putExtra("ALARM_AUDIO_URI", audioUri)
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            context, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = Intent(context, AlarmActionReceiver::class.java).apply {
            action = AlarmActionReceiver.ACTION_SNOOZE_ALARM
            putExtra("ALARM_AUDIO_URI", audioUri)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context, 2, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⏰ $title")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(mainPendingIntent)
            .addAction(0, "STOP", stopPendingIntent)
            .addAction(0, "SNOOZE (5 MIN)", snoozePendingIntent)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(AlarmActionReceiver.ALARM_NOTIFICATION_ID, builder.build())
            }
        } catch (_: SecurityException) {
        }
    }
}
