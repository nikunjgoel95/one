package com.charliesbot.onewearos.presentation.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.gms.wearable.DataMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Lightweight repository for storing notification schedule synced from phone.
 * 
 * The phone calculates optimal notification times and syncs them via Wearable Data Layer.
 * This repository stores the synced data in DataStore for display and scheduling.
 * 
 * This approach keeps the watch app lightweight - no Room database needed!
 */
class NotificationScheduleRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object PrefKeys {
        val SMART_REMINDER_TIME = longPreferencesKey("smart_reminder_time_millis")
        val EATING_WINDOW_TIME = longPreferencesKey("eating_window_time_millis")
        val NOTIFICATION_STRATEGY = stringPreferencesKey("notification_strategy")
        val SYNC_TIMESTAMP = longPreferencesKey("sync_timestamp")
    }

    /**
     * Get the smart reminder notification time in milliseconds.
     * This is when the "Time to Start Fasting" notification will fire.
     */
    fun getSmartReminderTime(): Flow<Long> {
        return dataStore.data.map { prefs ->
            prefs[PrefKeys.SMART_REMINDER_TIME] ?: getDefaultSmartReminderTime()
        }
    }

    /**
     * Get the eating window closing notification time in milliseconds.
     * Returns null if not applicable (e.g., for 36h fasts) or 0 if not synced yet.
     */
    fun getEatingWindowTime(): Flow<Long?> {
        return dataStore.data.map { prefs ->
            val time = prefs[PrefKeys.EATING_WINDOW_TIME] ?: 0L
            if (time == 0L) null else time
        }
    }

    /**
     * Get the notification strategy name (MOVING_AVERAGE, BEDTIME_BASED, DEFAULT).
     */
    fun getStrategy(): Flow<String> {
        return dataStore.data.map { prefs ->
            prefs[PrefKeys.NOTIFICATION_STRATEGY] ?: "DEFAULT"
        }
    }

    /**
     * Save notification schedule data received from phone via Wearable Data Layer.
     */
    suspend fun saveFromDataLayer(dataMap: DataMap) {
        dataStore.edit { prefs ->
            prefs[PrefKeys.SMART_REMINDER_TIME] = dataMap.getLong("smart_reminder_time_millis")
            prefs[PrefKeys.EATING_WINDOW_TIME] = dataMap.getLong("eating_window_time_millis")
            prefs[PrefKeys.NOTIFICATION_STRATEGY] = dataMap.getString("notification_strategy") ?: "DEFAULT"
            prefs[PrefKeys.SYNC_TIMESTAMP] = dataMap.getLong("sync_timestamp")
        }
    }

    private fun getDefaultSmartReminderTime(): Long {
        // Default to 8 PM today
        val now = java.time.ZonedDateTime.now()
        val eightPM = now.toLocalDate().atTime(20, 0).atZone(now.zone)
        return if (eightPM.isAfter(now)) {
            eightPM.toInstant().toEpochMilli()
        } else {
            eightPM.plusDays(1).toInstant().toEpochMilli()
        }
    }
}

