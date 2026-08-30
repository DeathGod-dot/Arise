package com.example.arise.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.arise.data.AriseRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate

@EntryPoint
@InstallIn(SingletonComponent::class)
interface DailyReminderWorkerEntryPoint {
    fun repository(): AriseRepository
}

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                DailyReminderWorkerEntryPoint::class.java
            )
            val incompleteCount = entryPoint.repository().getIncompleteQuestCount(LocalDate.now())
            if (incompleteCount > 0) {
                NotificationHelper.showQuestReminder(context, incompleteCount)
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
