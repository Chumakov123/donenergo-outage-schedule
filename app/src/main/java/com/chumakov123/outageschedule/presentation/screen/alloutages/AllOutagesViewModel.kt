package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class AllOutagesState(
    val isLoading: Boolean = true,
    val outages: List<Outage> = emptyList(),
    val error: String? = null,
    val onlyTrackedPlaces: Boolean = false,
    val emptyFilterMessage: String? = null,
    val trackedPlaces: List<TrackedPlace> = emptyList()
)

class AllOutagesViewModel(
    private val outageRepository: OutageRepository,
    private val branchRepository: BranchRepository,
    private val settingsRepository: AppSettingsRepository,
    private val trackedPlaceRepository: TrackedPlaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AllOutagesState())
    val state: StateFlow<AllOutagesState> = _state

    init {
        refreshOnSelectionChange()
        observeFilteredOutages()
    }

    private fun refreshOnSelectionChange() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .collectLatest { urls ->
                    if (urls.isEmpty()) return@collectLatest

                    runCatching {
                        val branchMap = branchRepository.getBranches().associateBy { it.url }
                        val branches = urls.mapNotNull { branchMap[it] }

                        if (branches.isNotEmpty()) {
                            outageRepository.refreshOutages(branches)
                        }
                    }.onFailure { throwable ->
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = throwable.message ?: "Ошибка загрузки отключений"
                        )
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeFilteredOutages() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .flatMapLatest { urls ->
                    if (urls.isEmpty()) {
                        flowOf(
                            AllOutagesState(
                                isLoading = false,
                                outages = emptyList(),
                                error = null,
                                trackedPlaces = emptyList()
                            )
                        )
                    } else {
                        combine(
                            outageRepository.observeOutages(urls),
                            trackedPlaceRepository.observePlaces(),
                            settingsRepository.onlyTrackedPlacesFlow
                        ) { outages, places, onlyTracked ->
                            val activePlaces = places.filter { it.isEnabled }
                            val filtered = if (onlyTracked) {
                                TrackedPlaceMatcher.filter(outages, activePlaces)
                            } else {
                                outages
                            }

                            val emptyMessage = when {
                                onlyTracked && places.isEmpty() ->
                                    "Нет отслеживаемых мест. Добавьте их, чтобы фильтр работал."

                                onlyTracked && places.none { it.isEnabled } ->
                                    "Нет активных отслеживаемых мест. Включите их в разделе «Места»."

                                else -> null
                            }

                            AllOutagesState(
                                isLoading = false,
                                outages = filtered,
                                error = null,
                                onlyTrackedPlaces = onlyTracked,
                                emptyFilterMessage = emptyMessage,
                                trackedPlaces = places
                            )
                        }
                    }
                }
                .catch { throwable ->
                    emit(
                        AllOutagesState(
                            isLoading = false,
                            outages = emptyList(),
                            error = throwable.message ?: "Ошибка загрузки отключений"
                        )
                    )
                }
                .collectLatest { newState ->
                    val currentError = _state.value.error
                    _state.value = newState.copy(
                        error = newState.error ?: currentError
                    )
                }
        }
    }

    fun toggleFilter(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setOnlyTrackedPlaces(enabled)
        }
    }
}