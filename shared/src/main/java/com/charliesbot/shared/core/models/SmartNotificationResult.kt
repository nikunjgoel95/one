package com.charliesbot.shared.core.models

import java.time.ZonedDateTime

/**
 * Result of smart notification calculation containing all notification details.
 * 
 * This bundles together the calculated notification times and the strategy used,
 * allowing the UI to display comprehensive information to users.
 * 
 * @property smartReminderTime When the "Time to Start Fasting" notification will fire
 * @property eatingWindowClosingTime When the "Eating Window Closing" notification will fire (null for 36h fasts)
 * @property strategy The strategy used to calculate these times (helps users understand the "why")
 */
data class SmartNotificationResult(
    val smartReminderTime: ZonedDateTime,
    val eatingWindowClosingTime: ZonedDateTime?,
    val strategy: NotificationStrategy
)

