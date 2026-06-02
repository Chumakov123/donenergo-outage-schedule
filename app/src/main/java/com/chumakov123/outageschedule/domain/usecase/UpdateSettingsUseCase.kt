package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.first

class UpdateSettingsUseCase(
    private val repository: AppSettingsRepository
) {
    suspend fun toggleBranch(url: String) {
        val current = repository.selectedBranchUrlsFlow.first().toMutableSet()
        if (current.contains(url)) {
            if (current.size > 1) {
                current.remove(url)
            }
        } else {
            current.add(url)
        }
        repository.setSelectedBranchUrls(current)
    }

    suspend fun setSyncInterval(hours: Int) {
        repository.setOutageSyncIntervalHours(hours)
    }

    suspend fun toggleNotificationLeadHour(hours: Int) {
        val current = repository.notificationLeadHoursFlow.first().toMutableSet()
        if (current.contains(hours)) {
            if (current.size > 1) {
                current.remove(hours)
            }
        } else {
            current.add(hours)
        }
        repository.setNotificationLeadHours(current)
    }

    suspend fun setTheme(theme: AppTheme) {
        repository.setSelectedTheme(theme)
    }
}