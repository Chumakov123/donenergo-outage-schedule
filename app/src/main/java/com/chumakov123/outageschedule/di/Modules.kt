package com.chumakov123.outageschedule.di

import com.chumakov123.outageschedule.data.remote.datasource.BranchRemoteDataSource
import com.chumakov123.outageschedule.data.remote.datasource.OutageRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.BranchIndexParser
import com.chumakov123.outageschedule.data.remote.parser.OutageHtmlParser
import com.chumakov123.outageschedule.data.repository.DataStoreAppSettingsRepository
import com.chumakov123.outageschedule.data.repository.DonEnergoBranchRepository
import com.chumakov123.outageschedule.data.repository.DonEnergoOutageRepository
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.presentation.navigation.AppEntryViewModel
import com.chumakov123.outageschedule.presentation.screen.alloutages.AllOutagesViewModel
import com.chumakov123.outageschedule.presentation.screen.onboarding.OnboardingViewModel
import org.koin.android.ext.koin.androidContext
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

    single<OutageRepository> {
        DonEnergoOutageRepository(
            remote = get(),
            parser = get()
        )
    }

    single<AppSettingsRepository> {
        DataStoreAppSettingsRepository(
            context = androidContext()
        )
    }
}

val domainModule = module {
    // use cases
}

val presentationModule = module {
    viewModelOf(::AppEntryViewModel)
    viewModelOf(::AllOutagesViewModel)
    viewModelOf(::OnboardingViewModel)
}