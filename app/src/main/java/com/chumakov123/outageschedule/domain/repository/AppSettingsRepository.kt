package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.AppTheme
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {

    val isOnboardingCompletedFlow: Flow<Boolean>

    val selectedBranchUrlsFlow: Flow<Set<String>>

    val onlyTrackedPlacesFlow: Flow<Boolean>

    val outageSyncIntervalHoursFlow: Flow<Int>

    val notificationLeadHoursFlow: Flow<Set<Int>>

    val selectedThemeFlow: Flow<AppTheme>

    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun setSelectedBranchUrls(urls: Set<String>)

    suspend fun setOnlyTrackedPlaces(enabled: Boolean)

    suspend fun setOutageSyncIntervalHours(hours: Int)

    suspend fun setNotificationLeadHours(hours: Set<Int>)

    suspend fun setSelectedTheme(theme: AppTheme)
}