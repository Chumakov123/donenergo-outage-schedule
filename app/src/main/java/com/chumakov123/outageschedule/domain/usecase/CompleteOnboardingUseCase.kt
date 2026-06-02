package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository

class CompleteOnboardingUseCase(
    private val settings: AppSettingsRepository
) {
    suspend operator fun invoke(selectedUrls: Set<String>) {
        settings.setSelectedBranchUrls(selectedUrls)
        settings.setOnboardingCompleted(true)
    }
}