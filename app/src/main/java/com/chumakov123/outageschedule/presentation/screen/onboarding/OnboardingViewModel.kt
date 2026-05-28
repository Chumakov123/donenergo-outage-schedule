package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.branch.BranchSelector
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

data class OnboardingState(
    val branches: List<Branch> = emptyList(),
    val selectedUrls: Set<String> = emptySet()
)

class OnboardingViewModel(
    private val branchRepository: BranchRepository,
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state= _state.asStateFlow()

    private val _finishEvent = MutableSharedFlow<Unit>()
    val finishEvent = _finishEvent.asSharedFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {

            val branches = branchRepository.getBranches()
            val default = BranchSelector.findDefault(branches)

            val saved = settings.selectedBranchUrlsFlow
                .firstOrNull()
                ?: emptySet()

            val initial = saved.ifEmpty {
                setOf(default.url)
            }

            _state.value = OnboardingState(
                branches = branches,
                selectedUrls = initial
            )
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
            _state.value = _state.value.copy(selectedUrls = current)
        }
    }

    fun finish() {
        viewModelScope.launch(Dispatchers.IO) {

            settings.setSelectedBranchUrls(
                _state.value.selectedUrls
            )

            settings.setOnboardingCompleted(true)

            _finishEvent.emit(Unit)
        }
    }
}