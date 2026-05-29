package com.chumakov123.outageschedule.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    val isOnboardingCompletedFlow: Flow<Boolean>

    val selectedBranchUrlsFlow: Flow<Set<String>>

    val onlyTrackedPlacesFlow: Flow<Boolean>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setSelectedBranchUrls(urls: Set<String>)

    suspend fun setOnlyTrackedPlaces(enabled: Boolean)
}