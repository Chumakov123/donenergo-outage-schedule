package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.debug.DebugActions
import com.chumakov123.outageschedule.domain.model.AppTheme
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.util.NotificationPermissionChecker
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
    private val permissionChecker: NotificationPermissionChecker,
    private val branchRepository: BranchRepository,
    private val localityRepository: BranchLocalityRepository,
    private val settingsRepository: AppSettingsRepository,
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
            val branches = branchRepository.getBranches()
            _state.update { it.copy(branches = branches) }
        }
    }

    private fun observeSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow.collectLatest { urls ->
                _state.update { it.copy(selectedUrls = urls) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.outageSyncIntervalHoursFlow.collectLatest { hours ->
                _state.update { it.copy(syncIntervalHours = hours) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.notificationLeadHoursFlow.collectLatest { hours ->
                _state.update { it.copy(notificationLeadHours = hours) }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedThemeFlow.collectLatest { theme ->
                _state.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    private fun observeLocalities() {
        viewModelScope.launch(Dispatchers.IO) {
            localityRepository.observeLocalities().collectLatest { localities ->
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
        val isGranted = permissionChecker.areNotificationsEnabled()
        _state.update { it.copy(isNotificationPermissionGranted = isGranted) }
    }

    fun toggle(branch: Branch) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _state.value.selectedUrls.toMutableSet()

            if (current.contains(branch.url)) {
                if (current.size == 1) return@launch
                current.remove(branch.url)
            } else {
                current.add(branch.url)
            }

            settingsRepository.setSelectedBranchUrls(current)
        }
    }

    fun setSyncInterval(hours: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setOutageSyncIntervalHours(hours)
        }
    }

    fun toggleNotificationLeadHour(hours: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = _state.value.notificationLeadHours.toMutableSet()

            if (current.contains(hours)) {
                if (current.size == 1) return@launch
                current.remove(hours)
            } else {
                current.add(hours)
            }

            settingsRepository.setNotificationLeadHours(current)
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setSelectedTheme(theme)
        }
    }

    fun sendTestNotification() {
        debugActions.sendTestNotification()
    }
}
