package com.charliesbot.one.today

import android.app.Application
import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.notifications.NotificationScheduler
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
class TodayViewModelTest {

    private lateinit var fastingDataRepository: FastingDataRepository
    private lateinit var notificationScheduler: NotificationScheduler
    private lateinit var application: Application
    private lateinit var viewModel: TodayViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    // Mocked flows from repository
    private lateinit var isFastingFlow: MutableStateFlow<Boolean>
    private lateinit var startTimeInMillisFlow: MutableStateFlow<Long>
    private lateinit var fastingGoalIdFlow: MutableStateFlow<String>

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        application = mockk(relaxed = true)
        fastingDataRepository = mockk()
        notificationScheduler = mockk(relaxed = true) // relaxed as its methods are not core to elapsedTime

        isFastingFlow = MutableStateFlow(false)
        startTimeInMillisFlow = MutableStateFlow(0L)
        fastingGoalIdFlow = MutableStateFlow("16:8") // Default goal

        every { fastingDataRepository.isFasting } returns isFastingFlow
        every { fastingDataRepository.startTimeInMillis } returns startTimeInMillisFlow
        every { fastingDataRepository.fastingGoalId } returns fastingGoalIdFlow

        viewModel = TodayViewModel(application, notificationScheduler, fastingDataRepository)
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
        // Initial state: not fasting
        assertEquals(0L, viewModel.elapsedTime.value)

        val currentTime = System.currentTimeMillis()
        isFastingFlow.value = true
        startTimeInMillisFlow.value = currentTime

        // Let the viewModel's collection and loop start
        advanceTimeBy(50) // Small delay for flow collection
        assertEquals(0L, viewModel.elapsedTime.value) // Initially time diff is 0 or very small

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

        advanceTimeBy(50) // flow collection
        advanceTimeBy(2000L) // Fast for 2 seconds
        assertEquals(2000L, viewModel.elapsedTime.value)

        isFastingFlow.value = false // Stop fasting
        advanceTimeBy(50) // allow flow collection to update
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

        advanceTimeBy(2050L) // Fast for 2 seconds (2000ms + 50ms buffer for collection)
        assertEquals(2000L, viewModel.elapsedTime.value)

        // Simulate a scenario where startTime is updated (e.g., user manually changes it)
        val newStartTime = initialStartTime + 1000L // Effectively, fast started 1s later
        startTimeInMillisFlow.value = newStartTime

        advanceTimeBy(1050L) // Advance another second from the new perspective (1000ms + 50ms buffer)
        // Elapsed time should be relative to newStartTime: (current - newStart)
        // (initialStartTime + 2050 + 1050) - (initialStartTime + 1000) = 3100 - 1000 = 2100.
        // However, the loop runs every 1s.
        // After newStartTime set, the next tick will be ~1s after that.
        // Total time passed in reality: 2050 + 1050 = 3100ms from initial start.
        // Effective start time: initialStartTime + 1000ms
        // So, effective duration: 3100 - 1000 = 2100ms.
        // The value should be close to 2100, accounting for 1s ticks.
        // Let's check for a value around 2000, as the timer ticks every second.
        // (initialStartTime + 3100) - (initialStartTime + 1000) = 2100
        // The timer will next update to what System.currentTimeMillis() - newStartTime is.
        // System.currentTimeMillis() is now initialStartTime + 3100
        // So, (initialStartTime + 3100) - (initialStartTime + 1000) = 2100
         assertEquals(2100L, viewModel.elapsedTime.value)
    }
}
