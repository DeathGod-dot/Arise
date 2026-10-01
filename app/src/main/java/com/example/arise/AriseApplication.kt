package com.example.arise

import android.app.Application
import com.example.arise.data.FirestoreSyncRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AriseApplication : Application() {
    @Inject
    lateinit var firestoreSyncRepository: FirestoreSyncRepository

    override fun onCreate() {
        super.onCreate()
        
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.example.arise.service.DailyReminderWorker>(
            24, java.util.concurrent.TimeUnit.HOURS
        ).build()
        
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReminderWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
