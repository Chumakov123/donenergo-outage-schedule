package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsState(
    val branches: List<Branch> = emptyList(),
    val selectedUrls: Set<String> = emptySet(),
    val citySuggestionsByBranchUrl: Map<String, List<String>> = emptyMap(),
    val syncIntervalHours: Int = 6
)

class SettingsViewModel(
    private val branchRepository: BranchRepository,
    private val localityRepository: BranchLocalityRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = branchRepository.getBranches()

            combine(
                settingsRepository.selectedBranchUrlsFlow,
                settingsRepository.outageSyncIntervalHoursFlow,
                localityRepository.observeLocalities()
            ) { urls, intervalHours, localities ->

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

                SettingsState(
                    branches = branches,
                    selectedUrls = urls,
                    citySuggestionsByBranchUrl = cities,
                    syncIntervalHours = intervalHours
                )
            }.collectLatest {
                _state.value = it
            }
        }
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
}