package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.branch.BranchSelector
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
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
    val citySuggestionsByBranchUrl: Map<String, List<String>> = emptyMap()
)

class OnboardingViewModel(
    private val branchRepository: BranchRepository,
    private val localityRepository: BranchLocalityRepository,
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    init {
        loadBranches()
        observeLocalities()
    }

    private fun loadBranches() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = branchRepository.getBranches()
            val default = BranchSelector.findDefault(branches)

            val saved = settings.selectedBranchUrlsFlow.first()
            val initial = saved.ifEmpty { setOf(default.url) }

            _state.update {
                it.copy(
                    branches = branches,
                    selectedUrls = initial
                )
            }
        }
    }

    private fun observeLocalities() {
        viewModelScope.launch(Dispatchers.IO) {
            localityRepository.observeLocalities().collectLatest { localities ->
                val suggestions = localities
                    .groupBy { it.branchUrl }
                    .mapValues { (_, items) ->
                        items.asSequence()
                            .map { it.city.trim() }
                            .filter { it.isNotBlank() }
                            .distinct()
                            .take(3)
                            .toList()
                    }

                _state.update {
                    it.copy(citySuggestionsByBranchUrl = suggestions)
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
                settings.setSelectedBranchUrls(
                    _state.value.selectedUrls
                )
                settings.setOnboardingCompleted(true)
            }
            onFinish()
        }
    }
}