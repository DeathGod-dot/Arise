package com.example.arise.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat

class AlarmActionReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_STOP_ALARM = "com.example.arise.ACTION_STOP_ALARM"
        const val ACTION_SNOOZE_ALARM = "com.example.arise.ACTION_SNOOZE_ALARM"
        const val ALARM_NOTIFICATION_ID = 9999
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Stop playing music
        AriseAlarmReceiver.stopAlarmMedia()

        // Cancel notification
        with(NotificationManagerCompat.from(context)) {
            cancel(ALARM_NOTIFICATION_ID)
        }

        val audioUri = intent?.getStringExtra("ALARM_AUDIO_URI")

        when (intent?.action) {
            ACTION_SNOOZE_ALARM -> {
                // Snooze for 5 minutes
                AlarmScheduler.scheduleSnoozeAlarm(context, minutes = 5, audioUri = audioUri)
            }
            ACTION_STOP_ALARM -> {
                // Stopped by user
            }
        }
    }
}
