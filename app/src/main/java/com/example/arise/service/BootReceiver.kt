package com.example.arise.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("arise_settings", Context.MODE_PRIVATE)
            val alarmEnabled = prefs.getBoolean("alarmEnabled", false)
            if (alarmEnabled) {
                val hour = prefs.getInt("alarmHour", 7)
                val minute = prefs.getInt("alarmMinute", 0)
                AlarmScheduler.scheduleDailyAlarm(context, hour, minute)
            }
        }
    }
}
