package com.charliesbot.onewearos.presentation.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charliesbot.onewearos.presentation.data.NotificationScheduleRepository
import com.charliesbot.onewearos.presentation.domain.WatchScheduleSmartNotificationsUseCase
import com.charliesbot.shared.core.data.repositories.preferencesRepository.PreferencesRepository
import com.charliesbot.shared.core.models.NotificationStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

data class SettingsUiState(
    val smartNotificationsEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val bedtime: LocalTime? = null,
    val notificationStrategy: String = "default",
    val smartReminderTime: String = "8:00 PM",
    val eatingWindowTime: String? = "7:00 PM",
    val is36HourFast: Boolean = false
)

class WearSettingsViewModel(
    application: Application,
    private val preferencesRepository: PreferencesRepository,
    private val notificationScheduleRepository: NotificationScheduleRepository,
    private val scheduleSmartNotificationsUseCase: WatchScheduleSmartNotificationsUseCase
) : AndroidViewModel(application) {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.getSmartNotificationsEnabled(),
        preferencesRepository.getVibrationEnabled(),
        preferencesRepository.getBedtime(),
        notificationScheduleRepository.getSmartReminderTime(),
        notificationScheduleRepository.getEatingWindowTime(),
        notificationScheduleRepository.getStrategy()
    ) { values: Array<*> ->
        val smartEnabled = values[0] as Boolean
        val vibrationEnabled = values[1] as Boolean
        val bedtime = values[2] as LocalTime?
        val smartReminderMillis = values[3] as Long
        val eatingWindowMillis = values[4] as Long?
        val strategyName = values[5] as String
        
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        
        val smartReminderTime = java.time.Instant.ofEpochMilli(smartReminderMillis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalTime()
            .format(formatter)
        
        val eatingWindowTime = eatingWindowMillis?.let { millis ->
            java.time.Instant.ofEpochMilli(millis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalTime()
                .format(formatter)
        }
        
        val strategy = try {
            NotificationStrategy.valueOf(strategyName)
        } catch (e: Exception) {
            NotificationStrategy.DEFAULT
        }
        
        SettingsUiState(
            smartNotificationsEnabled = smartEnabled,
            vibrationEnabled = vibrationEnabled,
            bedtime = bedtime,
            notificationStrategy = when (strategy) {
                NotificationStrategy.MOVING_AVERAGE -> "based on your routine"
                NotificationStrategy.BEDTIME_BASED -> "based on bedtime"
                NotificationStrategy.DEFAULT -> "default"
            },
            smartReminderTime = smartReminderTime,
            eatingWindowTime = eatingWindowTime,
            is36HourFast = eatingWindowMillis == null || eatingWindowMillis == 0L
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    fun toggleSmartNotifications() {
        viewModelScope.launch {
            val currentValue = preferencesRepository.getSmartNotificationsEnabled().stateIn(viewModelScope).value
            val newValue = !currentValue
            preferencesRepository.setSmartNotificationsEnabled(newValue)
            
            // Reschedule notifications if enabled
            // Note: Phone will sync updated schedule, watch just uses it
            if (newValue) {
                scheduleSmartNotificationsUseCase()
            }
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            val currentValue = preferencesRepository.getVibrationEnabled().stateIn(viewModelScope).value
            preferencesRepository.setVibrationEnabled(!currentValue)
        }
    }

    fun setBedtime(time: LocalTime?) {
        viewModelScope.launch {
            preferencesRepository.setBedtime(time)
            // Note: This saves on watch only. User should set bedtime on phone
            // for it to be used in notification calculation.
            // Phone will recalculate and sync new schedule to watch.
            if (preferencesRepository.getSmartNotificationsEnabled().stateIn(viewModelScope).value) {
                scheduleSmartNotificationsUseCase()
            }
        }
    }
}

