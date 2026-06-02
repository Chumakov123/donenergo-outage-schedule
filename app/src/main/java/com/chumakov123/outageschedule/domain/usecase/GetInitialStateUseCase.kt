package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class InitialState(
    val isOnboardingCompleted: Boolean,
    val theme: AppTheme
)

class GetInitialStateUseCase(
    private val settings: AppSettingsRepository
) {
    operator fun invoke(): Flow<InitialState> {
        return combine(
            settings.isOnboardingCompletedFlow,
            settings.selectedThemeFlow
        ) { completed, theme ->
            InitialState(completed, theme)
        }
    }
}