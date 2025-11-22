package com.charliesbot.onewearos.presentation.feature.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charliesbot.shared.core.data.repositories.preferencesRepository.PreferencesRepository
import com.charliesbot.shared.core.domain.usecase.CalculateSmartNotificationTimeUseCase
import com.charliesbot.shared.core.domain.usecase.GetSmartNotificationDetailsUseCase
import com.charliesbot.shared.core.domain.usecase.ScheduleSmartNotificationsUseCase
import com.charliesbot.shared.core.models.NotificationStrategy
import com.charliesbot.shared.core.models.SmartNotificationResult
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
    private val getSmartNotificationDetailsUseCase: GetSmartNotificationDetailsUseCase,
    private val scheduleSmartNotificationsUseCase: ScheduleSmartNotificationsUseCase
) : AndroidViewModel(application) {

    private val _notificationDetails = MutableStateFlow<SmartNotificationResult?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.getSmartNotificationsEnabled(),
        preferencesRepository.getVibrationEnabled(),
        preferencesRepository.getBedtime(),
        _notificationDetails
    ) { smartEnabled, vibrationEnabled, bedtime, details ->
        val formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        
        SettingsUiState(
            smartNotificationsEnabled = smartEnabled,
            vibrationEnabled = vibrationEnabled,
            bedtime = bedtime,
            notificationStrategy = when (details?.strategy) {
                NotificationStrategy.MOVING_AVERAGE -> "based on your routine"
                NotificationStrategy.BEDTIME_BASED -> "based on bedtime"
                NotificationStrategy.DEFAULT -> "default"
                null -> "calculating..."
            },
            smartReminderTime = details?.smartReminderTime?.toLocalTime()?.format(formatter) ?: "8:00 PM",
            eatingWindowTime = details?.eatingWindowClosingTime?.toLocalTime()?.format(formatter),
            is36HourFast = details?.eatingWindowClosingTime == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )
    
    init {
        // Calculate notification details on initialization
        calculateNotificationDetails()
    }
    
    private fun calculateNotificationDetails() {
        viewModelScope.launch {
            try {
                val details = getSmartNotificationDetailsUseCase()
                _notificationDetails.value = details
            } catch (e: Exception) {
                // Keep default values on error
            }
        }
    }

    fun toggleSmartNotifications() {
        viewModelScope.launch {
            val currentValue = preferencesRepository.getSmartNotificationsEnabled().stateIn(viewModelScope).value
            val newValue = !currentValue
            preferencesRepository.setSmartNotificationsEnabled(newValue)
            
            // Reschedule notifications and recalculate details if enabled
            if (newValue) {
                scheduleSmartNotificationsUseCase()
                calculateNotificationDetails()
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
            // Recalculate and reschedule notifications with new bedtime
            calculateNotificationDetails()
            if (preferencesRepository.getSmartNotificationsEnabled().stateIn(viewModelScope).value) {
                scheduleSmartNotificationsUseCase()
            }
        }
    }
}

