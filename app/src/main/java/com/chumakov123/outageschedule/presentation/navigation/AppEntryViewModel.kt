package com.chumakov123.outageschedule.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.usecase.GetInitialStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AppEntryState(
    val isLoading: Boolean = true,
    val isOnboardingCompleted: Boolean = false,
    val theme: AppTheme = AppTheme.SYSTEM
)

class AppEntryViewModel(
    private val getInitialStateUseCase: GetInitialStateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AppEntryState())
    val state: StateFlow<AppEntryState> = _state

    init {
        viewModelScope.launch {
            getInitialStateUseCase().collectLatest { initial ->
                _state.value = AppEntryState(
                    isLoading = false,
                    isOnboardingCompleted = initial.isOnboardingCompleted,
                    theme = initial.theme
                )
            }
        }
    }
}
