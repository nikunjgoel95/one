package com.charliesbot.shared.core.constants

object DataLayerConstants {
    const val FASTING_PATH = "/fasting_state"
    const val IS_FASTING_KEY = "is_fasting"
    const val START_TIME_KEY = "start_time"
    const val UPDATE_TIMESTAMP_KEY = "update_timestamp"
    const val FASTING_GOAL_KEY = "fasting_goal"
    
    // Notification schedule sync (phone → watch)
    const val NOTIFICATION_SCHEDULE_PATH = "/notification_schedule"
    const val SMART_REMINDER_TIME_MILLIS_KEY = "smart_reminder_time_millis"
    const val EATING_WINDOW_TIME_MILLIS_KEY = "eating_window_time_millis"
    const val NOTIFICATION_STRATEGY_KEY = "notification_strategy"
    const val SYNC_TIMESTAMP_KEY = "sync_timestamp"
}