package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AllOutagesState(
    val isLoading: Boolean = true,
    val outages: List<Outage> = emptyList(),
    val error: String? = null
)

class AllOutagesViewModel(
    private val outageRepository: OutageRepository,
    private val branchRepository: BranchRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AllOutagesState())
    val state: StateFlow<AllOutagesState> = _state

    init {
        observeSelectedBranches()
    }

    private fun observeSelectedBranches() {
        viewModelScope.launch(Dispatchers.IO) {
            val branchMap = runCatching {
                branchRepository.getBranches().associateBy { it.url }
            }.getOrElse {
                emptyMap()
            }

            settingsRepository.selectedBranchUrlsFlow.collectLatest { urls ->
                if (urls.isEmpty()) {
                    _state.value = AllOutagesState(
                        isLoading = false,
                        outages = emptyList()
                    )
                    return@collectLatest
                }

                val selectedBranches = urls.mapNotNull { url ->
                    branchMap[url]
                }

                _state.value = AllOutagesState(
                    isLoading = true,
                    outages = _state.value.outages,
                    error = null
                )

                launch {
                    runCatching {
                        outageRepository.refreshOutages(selectedBranches)
                    }.onFailure { error ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = error.message ?: "Ошибка загрузки отключений"
                        )
                    }
                }

                outageRepository.observeOutages(urls).collectLatest { outages ->
                    _state.value = AllOutagesState(
                        isLoading = false,
                        outages = outages,
                        error = null
                    )
                }
            }
        }
    }
}