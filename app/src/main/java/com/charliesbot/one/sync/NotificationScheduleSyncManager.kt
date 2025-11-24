package com.charliesbot.one.sync

import android.util.Log
import com.charliesbot.shared.core.constants.AppConstants.LOG_TAG
import com.charliesbot.shared.core.constants.DataLayerConstants
import com.charliesbot.shared.core.domain.usecase.GetSmartNotificationDetailsUseCase
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.PutDataMapRequest
import kotlinx.coroutines.tasks.await

/**
 * Manages syncing calculated notification schedules from phone to watch.
 * 
 * The phone calculates optimal notification times based on:
 * - Fasting history (moving average of 7+ fasts)
 * - User's bedtime setting
 * - Default fallback (8 PM)
 * 
 * This calculated schedule is then synced to the watch via Wearable Data Layer,
 * keeping the watch app lightweight without needing its own database.
 */
class NotificationScheduleSyncManager(
    private val dataClient: DataClient,
    private val getSmartNotificationDetailsUseCase: GetSmartNotificationDetailsUseCase
) {
    /**
     * Calculate notification schedule and sync to watch.
     * Should be called when:
     * - User completes a fast (history changes)
     * - User changes bedtime
     * - User changes fasting goal
     * - Daily recalculation
     */
    suspend fun syncNotificationSchedule() {
        try {
            val details = getSmartNotificationDetailsUseCase()
            
            val dataMap = PutDataMapRequest.create(DataLayerConstants.NOTIFICATION_SCHEDULE_PATH).apply {
                dataMap.putLong(
                    DataLayerConstants.SMART_REMINDER_TIME_MILLIS_KEY,
                    details.smartReminderTime.toInstant().toEpochMilli()
                )
                dataMap.putLong(
                    DataLayerConstants.EATING_WINDOW_TIME_MILLIS_KEY,
                    details.eatingWindowClosingTime?.toInstant()?.toEpochMilli() ?: 0L
                )
                dataMap.putString(
                    DataLayerConstants.NOTIFICATION_STRATEGY_KEY,
                    details.strategy.name
                )
                dataMap.putLong(
                    DataLayerConstants.SYNC_TIMESTAMP_KEY,
                    System.currentTimeMillis()
                )
            }
            
            dataClient.putDataItem(dataMap.asPutDataRequest()).await()
            Log.d(LOG_TAG, "NotificationScheduleSync: Successfully synced schedule to watch")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "NotificationScheduleSync: Failed to sync schedule to watch", e)
        }
    }
}

