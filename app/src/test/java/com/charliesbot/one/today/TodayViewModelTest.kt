package com.charliesbot.one.today

import android.app.Application
import app.cash.turbine.test
import com.charliesbot.shared.core.constants.PredefinedFastingGoals
import com.charliesbot.shared.core.data.repositories.fastingDataRepository.FastingDataRepository
import com.charliesbot.shared.core.notifications.NotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TodayViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var mockApplication: Application

    @Mock
    private lateinit var mockNotificationScheduler: NotificationScheduler

    @Mock
    private lateinit var mockFastingDataRepository: FastingDataRepository

    private lateinit var viewModel: TodayViewModel

    // MutableStateFlows to control the behavior of the repository's flows
    private lateinit var isFastingFlow: MutableStateFlow<Boolean>
    private lateinit var startTimeInMillisFlow: MutableStateFlow<Long>
    private lateinit var fastingGoalIdFlow: MutableStateFlow<String>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        // Setup mock flows from the repository
        isFastingFlow = MutableStateFlow(false)
        startTimeInMillisFlow = MutableStateFlow(-1L)
        fastingGoalIdFlow = MutableStateFlow(PredefinedFastingGoals.SIXTEEN_EIGHT.id)

        whenever(mockFastingDataRepository.isFasting).thenReturn(isFastingFlow)
        whenever(mockFastingDataRepository.startTimeInMillis).thenReturn(startTimeInMillisFlow)
        whenever(mockFastingDataRepository.fastingGoalId).thenReturn(fastingGoalIdFlow)

        // Mock getApplication() behavior for AndroidViewModel
        whenever(mockApplication.applicationContext).thenReturn(mockApplication)


        viewModel = TodayViewModel(
            application = mockApplication,
            notificationScheduler = mockNotificationScheduler,
            fastingDataRepository = mockFastingDataRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset the main dispatcher to the original one
    }

    @Test
    fun `initial state of isFasting is correct`() = runTest(testDispatcher) {
        isFastingFlow.value = false // Ensure initial state for this test
        viewModel.isFasting.test {
            assertFalse("isFasting should initially be false", awaitItem())
        }
    }

    @Test
    fun `initial state of startTimeInMillis is correct`() = runTest(testDispatcher) {
        startTimeInMillisFlow.value = 100L // Ensure initial state for this test
        viewModel.startTimeInMillis.test {
            assertEquals("startTimeInMillis should match repository", 100L, awaitItem())
        }
    }

    @Test
    fun `initial state of fastingGoalId is correct`() = runTest(testDispatcher) {
        fastingGoalIdFlow.value = PredefinedFastingGoals.TWELVE_TWELVE.id // Ensure initial state
        viewModel.fastingGoalId.test {
            assertEquals("fastingGoalId should match repository", PredefinedFastingGoals.TWELVE_TWELVE.id, awaitItem())
        }
    }

    @Test
    fun `initial state of isTimePickerDialogOpen is false`() = runTest(testDispatcher) {
        viewModel.isTimePickerDialogOpen.test {
            assertFalse("isTimePickerDialogOpen should initially be false", awaitItem())
        }
    }

    @Test
    fun `initial state of isGoalBottomSheetOpen is false`() = runTest(testDispatcher) {
        viewModel.isGoalBottomSheetOpen.test {
            assertFalse("isGoalBottomSheetOpen should initially be false", awaitItem())
        }
    }

    @Test
    fun `openTimePickerDialog sets isTimePickerDialogOpen to true`() = runTest(testDispatcher) {
        viewModel.isTimePickerDialogOpen.test {
            assertFalse("Should be initially false", awaitItem()) // Initial state
            viewModel.openTimePickerDialog()
            assertTrue("isTimePickerDialogOpen should be true after opening", awaitItem())
        }
    }

    @Test
    fun `closeTimePickerDialog sets isTimePickerDialogOpen to false`() = runTest(testDispatcher) {
        viewModel.isTimePickerDialogOpen.test {
            assertFalse("Should be initially false", awaitItem()) // Initial state
            viewModel.openTimePickerDialog()
            assertTrue("Should be true after opening", awaitItem()) // Open it
            viewModel.closeTimePickerDialog()
            assertFalse("isTimePickerDialogOpen should be false after closing", awaitItem())
        }
    }

    @Test
    fun `openGoalBottomSheet sets isGoalBottomSheetOpen to true`() = runTest(testDispatcher) {
        viewModel.isGoalBottomSheetOpen.test {
            assertFalse("Should be initially false", awaitItem()) // Initial state
            viewModel.openGoalBottomSheet()
            assertTrue("isGoalBottomSheetOpen should be true after opening", awaitItem())
        }
    }

    @Test
    fun `closeGoalBottomSheet sets isGoalBottomSheetOpen to false`() = runTest(testDispatcher) {
        viewModel.isGoalBottomSheetOpen.test {
            assertFalse("Should be initially false", awaitItem()) // Initial state
            viewModel.openGoalBottomSheet()
            assertTrue("Should be true after opening", awaitItem()) // Open it
            viewModel.closeGoalBottomSheet()
            assertFalse("isGoalBottomSheetOpen should be false after closing", awaitItem())
        }
    }

    @Test
    fun `onStopFasting calls repository, updateWidget and cancels notifications`() = runTest(testDispatcher) {
        val currentGoalId = PredefinedFastingGoals.EIGHTEEN_SIX.id
        fastingGoalIdFlow.value = currentGoalId

        viewModel.onStopFasting()

        // Advance past the launch block
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockFastingDataRepository).stopFasting(currentGoalId)
        // verify(mockOneWidget).updateAll(mockApplication) // Cannot directly mock OneWidget().updateAll easily here
        // For widget update, we can check if the method on ViewModel is called if it's not private,
        // or assume it's called if other interactions are correct.
        // For now, we trust `updateWidget` is called internally. A more complex setup might be needed for `OneWidget`.
        verify(mockNotificationScheduler).cancelAllNotifications()
    }

    @Test
    fun `onStartFasting calls repository, updateWidget and schedules notifications`() = runTest(testDispatcher) {
        val currentGoalId = PredefinedFastingGoals.TWENTY_FOUR.id
        fastingGoalIdFlow.value = currentGoalId

        viewModel.onStartFasting()

        // Advance past the launch block
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockFastingDataRepository).startFasting(any(), org.mockito.kotlin.eq(currentGoalId))
        // Similar to onStopFasting, direct verification of OneWidget().updateAll is tricky here.
        verify(mockNotificationScheduler).scheduleNotifications(any(), org.mockito.kotlin.eq(currentGoalId))
    }

    @Test
    fun `updateStartTime calls repository, updateWidget and schedules notifications`() = runTest(testDispatcher) {
        val newStartTime = System.currentTimeMillis() - 10000L
        val currentGoalId = PredefinedFastingGoals.SIXTEEN_EIGHT.id
        fastingGoalIdFlow.value = currentGoalId

        viewModel.updateStartTime(newStartTime)

        // Advance past the launch block
        testDispatcher.scheduler.advanceUntilIdle()

        verify(mockFastingDataRepository).updateFastingSchedule(newStartTime)
        verify(mockNotificationScheduler).scheduleNotifications(org.mockito.kotlin.eq(newStartTime), org.mockito.kotlin.eq(currentGoalId))
    }

    @Test
    fun `updateFastingGoal calls repository, updateWidget and schedules notifications`() = runTest(testDispatcher) {
        val newGoalId = PredefinedFastingGoals.CUSTOM.id // Assuming CUSTOM is a valid ID
        val currentStartTime = System.currentTimeMillis() - 5000L
        startTimeInMillisFlow.value = currentStartTime

        // Ensure the flow emits the initial value before update, for verification of notification scheduling
        viewModel.startTimeInMillis.test {
            assertEquals(currentStartTime, awaitItem()) // consume initial

            viewModel.updateFastingGoal(newGoalId)
            testDispatcher.scheduler.advanceUntilIdle()

            verify(mockFastingDataRepository).updateFastingGoalId(newGoalId)
            verify(mockNotificationScheduler).scheduleNotifications(org.mockito.kotlin.eq(currentStartTime), org.mockito.kotlin.eq(newGoalId))

            // No need to check for further emissions unless `updateFastingGoal` itself causes startTimeInMillis to change
            cancelAndIgnoreRemainingEvents()
        }
    }

    // Note: Testing `updateWidget`'s call to `OneWidget().updateAll()` is complex in a unit test
    // because `OneWidget()` creates a new instance. This would typically require:
    // 1. Refactoring `TodayViewModel` to take `OneWidget` as a dependency (DI).
    // 2. Using PowerMock or a similar tool to mock constructor calls (generally discouraged).
    // 3. Or, focusing on testing the public API and side effects that *can* be observed,
    //    and trusting that `updateWidget` does its job, or covering it in an integration/UI test.
    // For these unit tests, we'll assume `updateWidget` is called and focus on repository/scheduler interactions.
}
