package com.charliesbot.onewearos.presentation.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.charliesbot.onewearos.complication.ComplicationUpdateManager
import com.charliesbot.onewearos.presentation.data.NotificationScheduleRepository
import com.charliesbot.onewearos.presentation.data.WearStringProvider
import com.charliesbot.onewearos.presentation.domain.WatchScheduleSmartNotificationsUseCase
import com.charliesbot.onewearos.presentation.feature.settings.WearSettingsViewModel
import com.charliesbot.onewearos.presentation.notifications.NotificationWorker
import com.charliesbot.onewearos.presentation.notifications.OngoingActivityManager
import com.charliesbot.onewearos.presentation.services.LocalWatchFastingCallbacks
import com.charliesbot.onewearos.presentation.feature.today.WearTodayViewModel
import com.charliesbot.shared.core.abstraction.StringProvider
import com.charliesbot.shared.core.domain.usecase.ScheduleSmartNotificationsUseCase
import com.charliesbot.shared.core.notifications.NotificationScheduler
import com.charliesbot.shared.core.services.FastingEventCallbacks
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val wearAppModule = module {
    // Notification schedule repository (lightweight DataStore)
    single<NotificationScheduleRepository> {
        NotificationScheduleRepository(get<DataStore<Preferences>>())
    }
    
    // Watch-specific use case that reads synced times from DataStore (registered as its own type)
    single<WatchScheduleSmartNotificationsUseCase> {
        WatchScheduleSmartNotificationsUseCase(get(), get(), get())
    }

    viewModelOf(::WearTodayViewModel)
    viewModelOf(::WearSettingsViewModel)

    single<NotificationScheduler> {
        NotificationScheduler(
            context = androidContext(),
            workerClass = NotificationWorker::class.java,
        )
    }
    single<StringProvider> {
        WearStringProvider(androidContext())
    }
    single<ComplicationUpdateManager> {
        ComplicationUpdateManager(androidContext())
    }
    single<OngoingActivityManager> {
        OngoingActivityManager(
            context = androidContext(),
            fastingDataRepository = get()
        )
    }
    single { LocalWatchFastingCallbacks(get(), get(), get()) }
    single<FastingEventCallbacks> { get<LocalWatchFastingCallbacks>() }
}