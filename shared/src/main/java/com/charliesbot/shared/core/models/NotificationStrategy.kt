package com.charliesbot.shared.core.models

/**
 * Represents the strategy used to calculate smart notification times.
 * This helps users understand why they're receiving notifications at specific times.
 */
enum class NotificationStrategy {
    /**
     * Notifications calculated from moving average of past 7+ daily fasts.
     * This is the most personalized strategy based on user's historical fasting routine.
     */
    MOVING_AVERAGE,
    
    /**
     * Notifications calculated from user's configured bedtime (bedtime - 6 hours).
     * Used when user has set a bedtime but doesn't have enough fasting history yet.
     */
    BEDTIME_BASED,
    
    /**
     * Default 8 PM notification time.
     * Used as fallback when no history or bedtime is available.
     */
    DEFAULT
}

