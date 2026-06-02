package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository

class SetOnlyTrackedPlacesUseCase(
    private val repository: AppSettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setOnlyTrackedPlaces(enabled)
}