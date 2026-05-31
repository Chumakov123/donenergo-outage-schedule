package com.chumakov123.outageschedule.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class AppEntryState(
    val isLoading: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM
)

class AppEntryViewModel(
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppEntryState())
    val state: StateFlow<AppEntryState> = _state

    init {
        viewModelScope.launch {
            combine(
                settings.isOnboardingCompletedFlow,
                settings.selectedThemeFlow
            ) { completed, theme ->
                AppEntryState(
                    isLoading = false,
                    isOnboardingCompleted = completed,
                    theme = theme
                )
            }.collect {
                _state.value = it
            }
        }
    }
}