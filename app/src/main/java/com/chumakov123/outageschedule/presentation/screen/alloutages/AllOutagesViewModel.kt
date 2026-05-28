package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
    val state: StateFlow<AllOutagesState> = _state.asStateFlow()

    init {
        observeOutages()
    }

    private fun observeOutages() {
        viewModelScope.launch(Dispatchers.IO) {
            val branchMap = runCatching {
                branchRepository.getBranches().associateBy { it.url }
            }.getOrDefault(emptyMap())

            @OptIn(ExperimentalCoroutinesApi::class)
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .flatMapLatest { urls ->
                    if (urls.isEmpty()) {
                        flowOf(
                            AllOutagesState(
                                isLoading = false,
                                outages = emptyList(),
                                error = null
                            )
                        )
                    } else {
                        val selectedBranches = urls.mapNotNull { url ->
                            branchMap[url]
                        }

                        flow {
                            emit(
                                AllOutagesState(
                                    isLoading = true,
                                    outages = _state.value.outages,
                                    error = null
                                )
                            )

                            runCatching {
                                outageRepository.refreshOutages(selectedBranches)
                            }.onFailure { throwable ->
                                emit(
                                    AllOutagesState(
                                        isLoading = false,
                                        outages = _state.value.outages,
                                        error = throwable.message ?: "Ошибка загрузки отключений"
                                    )
                                )
                            }

                            emitAll(
                                outageRepository.observeOutages(urls)
                                    .map { outages ->
                                        AllOutagesState(
                                            isLoading = false,
                                            outages = outages,
                                            error = null
                                        )
                                    }
                            )
                        }
                    }
                }
                .collect { newState ->
                    _state.value = newState
                }
        }
    }
}