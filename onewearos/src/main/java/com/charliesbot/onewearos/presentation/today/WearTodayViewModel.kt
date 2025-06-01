package com.charliesbot.onewearos.presentation.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.notifications.NotificationScheduler
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class WearTodayViewModel(
    private val notificationScheduler: NotificationScheduler,
    private val fastingDataRepository: FastingDataRepository
) : ViewModel() {
    val isFasting: StateFlow<Boolean> = fastingDataRepository.isFasting.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = false
    )
    val fastingGoalId: StateFlow<String> = fastingDataRepository.fastingGoalId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = "16:8" // Default or fetch from constants
    )
    val startTimeInMillis: StateFlow<Long> = fastingDataRepository.startTimeInMillis.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = -1
    )

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    init {
        viewModelScope.launch {
            // Combine isFasting and startTimeInMillis flows
            combine(isFasting, startTimeInMillis) { fasting, startTime ->
                Pair(fasting, startTime)
            }.collectLatest { (fasting, startTime) ->
                if (fasting && startTime > 0) {
                    while (isActive) {
                        _elapsedTime.value = System.currentTimeMillis() - startTime
                        delay(1000L)
                    }
                } else {
                    _elapsedTime.value = 0L
                }
            }
        }
    }

    fun onStartFasting() {
        val startTimeMillis = System.currentTimeMillis()
        viewModelScope.launch {
            fastingDataRepository.startFasting(startTimeMillis)
            // TODO: Ensure notificationScheduler.scheduleNotifications can correctly use fastingGoalId.value if needed
            // For now, assuming it might take just startTime or has its own way to get goal.
            // This was: notificationScheduler.scheduleNotifications(startTimeInMillis.value)
            // It should ideally be: notificationScheduler.scheduleNotifications(startTimeMillis, fastingGoalId.value)
            // However, NotificationScheduler interface might need an update.
            // Sticking to original signature for now to avoid breaking other parts outside current scope.
            notificationScheduler.scheduleNotifications(startTimeMillis)
        }
    }

    fun onStopFasting() {
        viewModelScope.launch {
            fastingDataRepository.stopFasting()
            notificationScheduler.cancelAllNotifications()
        }
    }
}
