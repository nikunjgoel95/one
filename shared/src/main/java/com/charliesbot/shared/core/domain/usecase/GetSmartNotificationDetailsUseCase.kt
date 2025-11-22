package com.charliesbot.shared.core.domain.usecase

import com.charliesbot.shared.core.constants.PredefinedFastingGoals
import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.models.SmartNotificationResult
import kotlinx.coroutines.flow.first
import java.time.ZonedDateTime

/**
 * Get comprehensive smart notification details including both notification times and strategy.
 * 
 * This use case combines:
 * - The notification strategy (why these times were chosen)
 * - Smart reminder time (when to start fasting)
 * - Eating window closing time (1h before start, if applicable)
 * 
 * Used by the Settings UI to show users exactly when and why they'll be notified.
 */
class GetSmartNotificationDetailsUseCase(
    private val calculateSmartNotificationTimeUseCase: CalculateSmartNotificationTimeUseCase,
    private val fastingDataRepository: FastingDataRepository
) {
    suspend operator fun invoke(): SmartNotificationResult {
        // Get the strategy used for calculation
        val strategy = calculateSmartNotificationTimeUseCase.getStrategy()
        
        // Get the calculated notification time
        val notificationTime = calculateSmartNotificationTimeUseCase.calculateNotificationTime()
        
        // Get user's current fasting goal
        val fastingGoalId = fastingDataRepository.fastingGoalId.first()
        
        // Calculate the smart reminder time (next occurrence)
        val now = ZonedDateTime.now()
        var smartReminderTime = now
            .toLocalDate()
            .atTime(notificationTime)
            .atZone(now.zone)
        
        // If the time has already passed today, schedule for tomorrow
        if (smartReminderTime.isBefore(now) || smartReminderTime.isEqual(now)) {
            smartReminderTime = smartReminderTime.plusDays(1)
        }
        
        // Calculate eating window closing time (1 hour before, skip for 36h fasts)
        val eatingWindowTime = if (!is36HourFast(fastingGoalId)) {
            smartReminderTime.minusHours(1)
        } else {
            null
        }
        
        return SmartNotificationResult(
            smartReminderTime = smartReminderTime,
            eatingWindowClosingTime = eatingWindowTime,
            strategy = strategy
        )
    }

    private fun is36HourFast(goalId: String): Boolean {
        return goalId == PredefinedFastingGoals.THIRTY_SIX_HOUR.id
    }
}

