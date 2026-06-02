package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.usecase.CompleteOnboardingUseCase
import com.chumakov123.outageschedule.domain.usecase.GetBranchesUseCase
import com.chumakov123.outageschedule.domain.usecase.GetLocationSuggestionsUseCase
import com.chumakov123.outageschedule.domain.usecase.ObserveSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class OnboardingState(
    val branches: List<Branch> = emptyList(),
    val selectedUrls: Set<String> = emptySet(),
    val citySuggestionsByBranchUrl: Map<String, List<String>> = emptyMap(),
    val streetSuggestionsByBranchUrl: Map<String, List<String>> = emptyMap()
)

class OnboardingViewModel(
    private val getBranchesUseCase: GetBranchesUseCase,
    private val suggestionsUseCase: GetLocationSuggestionsUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val completeOnboardingUseCase: CompleteOnboardingUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    init {
        loadBranches()
        loadSavedSelection()
        observeLocalities()
    }

    private fun loadBranches() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = getBranchesUseCase()
            _state.update { it.copy(branches = branches) }

            if (_state.value.selectedUrls.isEmpty()) {
                val default = getBranchesUseCase.getDefaultBranch(branches)
                _state.update { it.copy(selectedUrls = setOf(default.url)) }
            }
        }
    }

    private fun loadSavedSelection() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedUrls = observeSettingsUseCase.selectedBranchUrls.first()
            if (savedUrls.isNotEmpty()) {
                _state.update { it.copy(selectedUrls = savedUrls) }
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

                val streets = localities
                    .groupBy { it.branchUrl }
                    .mapValues { (_, items) ->
                        items.asSequence()
                            .mapNotNull { it.street?.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .take(3)
                            .toList()
                    }

                _state.update {
                    it.copy(
                        citySuggestionsByBranchUrl = cities,
                        streetSuggestionsByBranchUrl = streets
                    )
                }
            }
        }
    }

    fun toggle(branch: Branch) {
        val current = _state.value.selectedUrls.toMutableSet()

        if (current.contains(branch.url)) {
            current.remove(branch.url)
        } else {
            current.add(branch.url)
        }

        if (current.isNotEmpty()) {
            _state.update { it.copy(selectedUrls = current) }
        }
    }

    fun finish(onFinish: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                completeOnboardingUseCase(_state.value.selectedUrls)
            }
            onFinish()
        }
    }
}
