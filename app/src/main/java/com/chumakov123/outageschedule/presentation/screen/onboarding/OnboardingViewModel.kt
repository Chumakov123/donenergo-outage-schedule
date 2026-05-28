package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.branch.BranchSelector
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val branches: List<Branch> = emptyList(),
    val selected: Branch? = null
)

class OnboardingViewModel(
    private val branchRepository: BranchRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = branchRepository.getBranches()
            val default = BranchSelector.findDefault(branches)

            _state.value = OnboardingState(
                branches = branches,
                selected = default
            )
        }
    }

    fun select(branch: Branch) {
        _state.value = _state.value.copy(selected = branch)
    }

    fun skip() {
        // пока просто оставляем default
    }
}