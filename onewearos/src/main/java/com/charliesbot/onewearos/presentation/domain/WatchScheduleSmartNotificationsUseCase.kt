package com.charliesbot.onewearos.presentation.domain

import com.charliesbot.onewearos.presentation.data.NotificationScheduleRepository
import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.notifications.NotificationScheduler
import kotlinx.coroutines.flow.first

/**
 * Watch-specific implementation of scheduling smart notifications.
 * 
 * Unlike the phone version, this doesn't calculate notification times.
 * Instead, it reads pre-calculated times that were synced from the phone
 * via Wearable Data Layer and stored in DataStore.
 * 
 * This keeps the watch app lightweight - no database, no complex calculations!
 */
class WatchScheduleSmartNotificationsUseCase(
    private val notificationScheduleRepository: NotificationScheduleRepository,
    private val notificationScheduler: NotificationScheduler,
    private val fastingDataRepository: FastingDataRepository
) {
    suspend operator fun invoke() {
        // Read synced notification times from DataStore
        val smartReminderMillis = notificationScheduleRepository.getSmartReminderTime().first()
        val eatingWindowMillis = notificationScheduleRepository.getEatingWindowTime().first()
        val fastingGoalId = fastingDataRepository.fastingGoalId.first()
        
        // Schedule smart reminder notification
        notificationScheduler.scheduleSmartReminder(smartReminderMillis, fastingGoalId)
        
        // Schedule eating window closing notification (if applicable)
        if (eatingWindowMillis != null && eatingWindowMillis > 0L) {
            notificationScheduler.scheduleEatingWindowClosing(eatingWindowMillis, fastingGoalId)
        }
    }
}

