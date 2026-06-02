package com.chumakov123.outageschedule.di

import androidx.room.Room
import com.chumakov123.outageschedule.BuildConfig
import com.chumakov123.outageschedule.data.debug.NoOpDebugActions
import com.chumakov123.outageschedule.data.debug.RealDebugActions
import com.chumakov123.outageschedule.data.local.database.AppDatabase
import com.chumakov123.outageschedule.data.notification.AndroidNotificationPermissionChecker
import com.chumakov123.outageschedule.data.notification.AndroidOutageNotifier
import com.chumakov123.outageschedule.data.remote.datasource.BranchRemoteDataSource
import com.chumakov123.outageschedule.data.remote.datasource.OutageRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.BranchIndexParser
import com.chumakov123.outageschedule.data.remote.parser.OutageHtmlParser
import com.chumakov123.outageschedule.data.repository.DataStoreAppSettingsRepository
import com.chumakov123.outageschedule.data.repository.DonEnergoBranchRepository
import com.chumakov123.outageschedule.data.repository.DonEnergoOutageRepository
import com.chumakov123.outageschedule.data.repository.RoomBranchLocalityRepository
import com.chumakov123.outageschedule.data.repository.RoomNotificationLogRepository
import com.chumakov123.outageschedule.data.repository.RoomTrackedPlaceRepository
import com.chumakov123.outageschedule.domain.debug.DebugActions
import com.chumakov123.outageschedule.domain.notification.OutageNotifier
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.NotificationLogRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.usecase.*
import com.chumakov123.outageschedule.domain.util.NotificationPermissionChecker
import com.chumakov123.outageschedule.presentation.navigation.AppEntryViewModel
import com.chumakov123.outageschedule.presentation.screen.alloutages.AllOutagesViewModel
import com.chumakov123.outageschedule.presentation.screen.onboarding.OnboardingViewModel
import com.chumakov123.outageschedule.presentation.screen.settings.SettingsViewModel
import com.chumakov123.outageschedule.presentation.screen.trackedplaces.TrackedPlacesViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dataModule = module {

    single { BranchRemoteDataSource() }
    single { BranchIndexParser() }

    single<BranchRepository> {
        DonEnergoBranchRepository(
            remote = get(),
            parser = get()
        )
    }

    single { OutageRemoteDataSource() }
    single { OutageHtmlParser() }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "outage_schedule.db"
        )
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single { get<AppDatabase>().outageDao() }
    single { get<AppDatabase>().trackedPlaceDao() }
    single { get<AppDatabase>().branchLocalityDao() }
    single { get<AppDatabase>().sentNotificationDao() }

    single<OutageRepository> {
        DonEnergoOutageRepository(
            remote = get(),
            parser = get(),
            dao = get(),
            database = get()
        )
    }

    single<TrackedPlaceRepository> {
        RoomTrackedPlaceRepository(
            dao = get()
        )
    }

    single<BranchLocalityRepository> {
        RoomBranchLocalityRepository(
            dao = get()
        )
    }

    single<NotificationLogRepository> {
        RoomNotificationLogRepository(
            dao = get()
        )
    }

    single<AppSettingsRepository> {
        DataStoreAppSettingsRepository(
            context = androidContext()
        )
    }

    single<OutageNotifier> { AndroidOutageNotifier(androidContext()) }
    single<NotificationPermissionChecker> { AndroidNotificationPermissionChecker(androidContext()) }

    single<DebugActions> {
        if (BuildConfig.DEBUG) {
            RealDebugActions(outageNotifier = get())
        } else {
            NoOpDebugActions()
        }
    }
}

val domainModule = module {
    factoryOf(::RefreshOutagesUseCase)
    factoryOf(::PrepareOutageNotificationsUseCase)
    factoryOf(::GetOutagesUseCase)
    factoryOf(::GetLocationSuggestionsUseCase)
    factoryOf(::GetBranchesUseCase)
    factoryOf(::GetNotificationPermissionUseCase)
    factoryOf(::GetInitialStateUseCase)
    factoryOf(::ObserveSettingsUseCase)
    factoryOf(::UpdateSettingsUseCase)
    factoryOf(::SetOnlyTrackedPlacesUseCase)
    factoryOf(::ObserveTrackedPlacesUseCase)
    factoryOf(::AddTrackedPlaceUseCase)
    factoryOf(::UpdateTrackedPlaceUseCase)
    factoryOf(::DeleteTrackedPlaceUseCase)
    factoryOf(::CompleteOnboardingUseCase)
}

val presentationModule = module {
    viewModelOf(::AppEntryViewModel)
    viewModelOf(::AllOutagesViewModel)
    viewModelOf(::OnboardingViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::TrackedPlacesViewModel)
}
