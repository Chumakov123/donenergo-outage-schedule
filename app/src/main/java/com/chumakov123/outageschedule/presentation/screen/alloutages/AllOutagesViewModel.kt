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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AllOutagesState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val rawOutages: List<Outage> = emptyList(),
    val outages: List<Outage> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isSearchVisible: Boolean = false,
    val isSearchEnabled: Boolean = false,
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
        observeData()
    }

    private fun refreshOnSelectionChange() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .collectLatest { urls ->
                    if (urls.isEmpty()) {
                        _state.update { it.copy(isLoading = false) }
                        return@collectLatest
                    }
                    refresh(urls)
                }
        }
    }

    fun onRefresh() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow.collectLatest { urls ->
                refresh(urls)
            }
        }
    }

    private suspend fun refresh(urls: Set<String>) {
        if (urls.isEmpty()) return
        _state.update { it.copy(isRefreshing = true) }
        runCatching {
            val branchMap = branchRepository.getBranches().associateBy { it.url }
            val branches = urls.mapNotNull { branchMap[it] }
            if (branches.isNotEmpty()) {
                outageRepository.refreshOutages(branches)
            }
        }.onFailure { throwable ->
            _state.update { it.copy(error = throwable.message) }
        }
        _state.update { it.copy(isLoading = false, isRefreshing = false) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .flatMapLatest { urls ->
                    if (urls.isEmpty()) flowOf(emptyList())
                    else outageRepository.observeOutages(urls)
                }
                .collectLatest { outages ->
                    _state.update { it.copy(rawOutages = outages).applyFilter() }
                }
        }

        viewModelScope.launch(Dispatchers.IO) {
            trackedPlaceRepository.observePlaces().collectLatest { places ->
                _state.update { it.copy(trackedPlaces = places).applyFilter() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.onlyTrackedPlacesFlow.collectLatest { onlyTracked ->
                _state.update { it.copy(onlyTrackedPlaces = onlyTracked).applyFilter() }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query).applyFilter() }
    }

    fun toggleSearch() {
        _state.update { 
            if (!it.isSearchEnabled && !it.isSearchVisible) return@update it
            val newVisible = !it.isSearchVisible
            it.copy(
                isSearchVisible = newVisible,
                searchQuery = if (!newVisible) "" else it.searchQuery
            ).applyFilter()
        }
    }

    private fun AllOutagesState.applyFilter(): AllOutagesState {
        val activePlaces = trackedPlaces.filter { it.isEnabled }
        
        val baseList = if (onlyTrackedPlaces) {
            TrackedPlaceMatcher.filter(rawOutages, activePlaces)
        } else {
            rawOutages
        }

        val canSearch = baseList.isNotEmpty()

        val effectiveSearchVisible = if (!canSearch) false else isSearchVisible
        val effectiveSearchQuery = if (!effectiveSearchVisible) "" else searchQuery

        var filtered = baseList
        if (effectiveSearchQuery.isNotBlank()) {
            val q = effectiveSearchQuery.trim().lowercase()
            filtered = filtered.filter { 
                it.address.lowercase().contains(q) || 
                it.city.lowercase().contains(q) ||
                it.reason?.lowercase()?.contains(q) == true
            }
        }

        val emptyMessage = when {
            onlyTrackedPlaces && trackedPlaces.isEmpty() ->
                "Нет отслеживаемых мест. Добавьте их в разделе «Места»."
            onlyTrackedPlaces && activePlaces.isEmpty() ->
                "Все отслеживаемые места отключены. Включите их или добавьте новые."
            filtered.isEmpty() && effectiveSearchQuery.isNotBlank() ->
                "По запросу «$effectiveSearchQuery» ничего не найдено."
            filtered.isEmpty() && onlyTrackedPlaces ->
                "Для ваших мест активных или будущих отключений не найдено."
            filtered.isEmpty() && rawOutages.isEmpty() ->
                "Список отключений пуст."
            else -> null
        }

        return copy(
            outages = filtered,
            isSearchVisible = effectiveSearchVisible,
            isSearchEnabled = canSearch,
            searchQuery = effectiveSearchQuery,
            emptyFilterMessage = emptyMessage
        )
    }

    fun toggleFilter(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setOnlyTrackedPlaces(enabled)
        }
    }
}