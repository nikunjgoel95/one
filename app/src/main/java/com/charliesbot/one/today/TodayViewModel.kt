package com.charliesbot.one.today

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charliesbot.one.widgets.OneWidget
import com.charliesbot.shared.core.constants.PredefinedFastingGoals
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

class TodayViewModel(
    application: Application,
    private val notificationScheduler: NotificationScheduler,
    private val fastingDataRepository: FastingDataRepository
) : AndroidViewModel(application) {
    val isFasting: StateFlow<Boolean> = fastingDataRepository.isFasting.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = false
    )
    val startTimeInMillis: StateFlow<Long> = fastingDataRepository.startTimeInMillis.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = -1
    )
    val fastingGoalId: StateFlow<String> = fastingDataRepository.fastingGoalId.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = PredefinedFastingGoals.SIXTEEN_EIGHT.id,
    )
    private val _isTimePickerDialogOpen = MutableStateFlow(false)
    val isTimePickerDialogOpen: StateFlow<Boolean> = _isTimePickerDialogOpen

    private val _isGoalBottomSheetOpen = MutableStateFlow(false)
    val isGoalBottomSheetOpen: StateFlow<Boolean> = _isGoalBottomSheetOpen

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

    fun openTimePickerDialog() {
        _isTimePickerDialogOpen.value = true
    }

    fun closeTimePickerDialog() {
        _isTimePickerDialogOpen.value = false
    }

    fun openGoalBottomSheet() {
        _isGoalBottomSheetOpen.value = true
    }

    fun closeGoalBottomSheet() {
        _isGoalBottomSheetOpen.value = false
    }

    suspend fun updateWidget() {
        OneWidget().updateAll(getApplication<Application>().applicationContext)
    }

    fun onStopFasting() {
        viewModelScope.launch {
            fastingDataRepository.stopFasting()
            updateWidget()
            notificationScheduler.cancelAllNotifications()
        }
    }

    fun onStartFasting() {
        val startTimeMillis = System.currentTimeMillis()
        viewModelScope.launch {
            fastingDataRepository.startFasting(startTimeMillis)
            updateWidget()
            notificationScheduler.scheduleNotifications(startTimeMillis, fastingGoalId.value)
        }
    }

    fun updateStartTime(timeInMillis: Long) {
        viewModelScope.launch {
            fastingDataRepository.updateFastingSchedule(timeInMillis)
            notificationScheduler.scheduleNotifications(timeInMillis, fastingGoalId.value)
        }
    }

    fun updateFastingGoal(fastingGoalId: String) {
        viewModelScope.launch {
            fastingDataRepository.updateFastingGoalId(fastingGoalId)
            notificationScheduler.scheduleNotifications(startTimeInMillis.value, fastingGoalId)
        }
    }
}