package com.charliesbot.one.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charliesbot.one.sync.NotificationScheduleSyncManager
import com.charliesbot.shared.core.constants.AppConstants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Daily worker that recalculates notification schedule and syncs to watch.
 * 
 * This worker runs once per day to:
 * 1. Recalculate optimal notification times based on latest fasting history
 * 2. Sync the updated schedule to the watch via Wearable Data Layer
 * 
 * This ensures the watch always has up-to-date notification times without
 * needing to maintain its own database.
 */
class DailyNotificationSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val notificationScheduleSyncManager: NotificationScheduleSyncManager by inject()

    override suspend fun doWork(): Result {
        Log.d(AppConstants.LOG_TAG, "DailyNotificationSyncWorker: Starting daily sync to watch")
        
        return try {
            // Recalculate and sync to watch
            notificationScheduleSyncManager.syncNotificationSchedule()
            Log.d(AppConstants.LOG_TAG, "DailyNotificationSyncWorker: Successfully synced to watch")
            Result.success()
        } catch (e: Exception) {
            Log.e(AppConstants.LOG_TAG, "DailyNotificationSyncWorker: Error syncing to watch", e)
            Result.retry()
        }
    }
}

