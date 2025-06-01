package com.charliesbot.onewearos.presentation.today

import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.notifications.NotificationScheduler
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class WearTodayViewModelTest {

    private lateinit var fastingDataRepository: FastingDataRepository
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var viewModel: WearTodayViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mocked flows from repository
    private lateinit var isFastingFlow: MutableStateFlow<Boolean>
    private lateinit var startTimeInMillisFlow: MutableStateFlow<Long>
    private lateinit var fastingGoalIdFlow: MutableStateFlow<String> // Added for completeness, though not directly used by elapsedTime

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        fastingDataRepository = mockk()
        notificationScheduler = mockk(relaxed = true) // relaxed as its methods are not core to elapsedTime

        isFastingFlow = MutableStateFlow(false)
        startTimeInMillisFlow = MutableStateFlow(0L)
        fastingGoalIdFlow = MutableStateFlow("16:8") // Default goal, matches ViewModel

        every { fastingDataRepository.isFasting } returns isFastingFlow
        every { fastingDataRepository.startTimeInMillis } returns startTimeInMillisFlow
        every { fastingDataRepository.fastingGoalId } returns fastingGoalIdFlow

        viewModel = WearTodayViewModel(notificationScheduler, fastingDataRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `elapsedTime is initially 0L`() = testScope.runTest {
        assertEquals(0L, viewModel.elapsedTime.first())
    }

    @Test
    fun `elapsedTime updates when fasting starts`() = testScope.runTest {
        assertEquals(0L, viewModel.elapsedTime.value) // Check initial value from StateFlow

        val currentTime = System.currentTimeMillis()
        isFastingFlow.value = true
        startTimeInMillisFlow.value = currentTime

        advanceTimeBy(50) // Small delay for flow collection
        assertEquals(0L, viewModel.elapsedTime.value)


        advanceTimeBy(1000L) // Advance time by 1 second
        assertEquals(1000L, viewModel.elapsedTime.value)

        advanceTimeBy(1000L) // Advance time by another second
        assertEquals(2000L, viewModel.elapsedTime.value)
    }

    @Test
    fun `elapsedTime resets to 0L when fasting stops`() = testScope.runTest {
        val currentTime = System.currentTimeMillis()
        isFastingFlow.value = true
        startTimeInMillisFlow.value = currentTime

        advanceTimeBy(2050L) // Fast for 2 seconds + buffer
        assertEquals(2000L, viewModel.elapsedTime.value)

        isFastingFlow.value = false // Stop fasting
        advanceTimeBy(50) // allow flow collection
        assertEquals(0L, viewModel.elapsedTime.value)
    }

    @Test
    fun `elapsedTime remains 0L if fasting starts with invalid startTime`() = testScope.runTest {
        isFastingFlow.value = true
        startTimeInMillisFlow.value = 0L // Invalid start time

        advanceTimeBy(1000L)
        assertEquals(0L, viewModel.elapsedTime.value)
    }

    @Test
    fun `elapsedTime updates correctly if startTime changes while fasting`() = testScope.runTest {
        val initialStartTime = System.currentTimeMillis()
        isFastingFlow.value = true
        startTimeInMillisFlow.value = initialStartTime

        advanceTimeBy(2050L)
        assertEquals(2000L, viewModel.elapsedTime.value)

        val newStartTime = initialStartTime + 1000L
        startTimeInMillisFlow.value = newStartTime

        advanceTimeBy(1050L)
        assertEquals(2100L, viewModel.elapsedTime.value)
    }
}
