package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.debug.DebugActions
import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.usecase.GetBranchesUseCase
import com.chumakov123.outageschedule.domain.usecase.GetLocationSuggestionsUseCase
import com.chumakov123.outageschedule.domain.usecase.GetNotificationPermissionUseCase
import com.chumakov123.outageschedule.domain.usecase.ObserveSettingsUseCase
import com.chumakov123.outageschedule.domain.usecase.UpdateSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsState(
    val branches: List<Branch> = emptyList(),
    val selectedUrls: Set<String> = emptySet(),
    val citySuggestionsByBranchUrl: Map<String, List<String>> = emptyMap(),
    val syncIntervalHours: Int = 24,
    val notificationLeadHours: Set<Int> = setOf(12, 24, 48),
    val isNotificationPermissionGranted: Boolean = false,
    val selectedTheme: AppTheme = AppTheme.DARK
)

class SettingsViewModel(
    private val getNotificationPermissionUseCase: GetNotificationPermissionUseCase,
    private val getBranchesUseCase: GetBranchesUseCase,
    private val suggestionsUseCase: GetLocationSuggestionsUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val updateSettingsUseCase: UpdateSettingsUseCase,
    private val debugActions: DebugActions
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        loadBranches()
        observeSettings()
        observeLocalities()
        checkNotificationPermission()
    }

    private fun loadBranches() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = getBranchesUseCase()
            _state.update { it.copy(branches = branches) }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.selectedBranchUrls.collectLatest { urls ->
                _state.update { it.copy(selectedUrls = urls) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.syncIntervalHours.collectLatest { hours ->
                _state.update { it.copy(syncIntervalHours = hours) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.notificationLeadHours.collectLatest { hours ->
                _state.update { it.copy(notificationLeadHours = hours) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.selectedTheme.collectLatest { theme ->
                _state.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    private fun observeLocalities() {
        viewModelScope.launch(Dispatchers.IO) {
            suggestionsUseCase.observeLocalities().collectLatest { localities ->
                val cities = localities
                    .groupBy { it.branchUrl }
                    .mapValues { (_, items) ->
                        items.asSequence()
                            .map { it.city.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .take(3)
                            .toList()
                    }
                _state.update { it.copy(citySuggestionsByBranchUrl = cities) }
            }
        }
    }

    fun checkNotificationPermission() {
        val isGranted = getNotificationPermissionUseCase()
        _state.update { it.copy(isNotificationPermissionGranted = isGranted) }
    }

    fun toggle(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsUseCase.toggleBranch(branch.url)
        }
    }

    fun setSyncInterval(hours: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsUseCase.setSyncInterval(hours)
        }
    }

    fun toggleNotificationLeadHour(hours: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsUseCase.toggleNotificationLeadHour(hours)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            updateSettingsUseCase.setTheme(theme)
        }
    }

    fun sendTestNotification() {
        debugActions.sendTestNotification()
    }
}
