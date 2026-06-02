package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveSettingsUseCase(
    repository: AppSettingsRepository
) {
    val selectedBranchUrls: Flow<Set<String>> = repository.selectedBranchUrlsFlow
    val syncIntervalHours: Flow<Int> = repository.outageSyncIntervalHoursFlow
    val notificationLeadHours: Flow<Set<Int>> = repository.notificationLeadHoursFlow
    val selectedTheme: Flow<AppTheme> = repository.selectedThemeFlow
    val onlyTrackedPlaces: Flow<Boolean> = repository.onlyTrackedPlacesFlow
}