package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
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
    val onlyTrackedPlaces: Boolean = true,
    val emptyFilterMessage: String? = null,
    val emptyFilterIcon: ImageVector? = null,
    val showTrackedPlacesAction: Boolean = false,
    val trackedPlaces: List<TrackedPlace> = emptyList()
)

class AllOutagesViewModel(
    private val getOutagesUseCase: GetOutagesUseCase,
    private val refreshOutagesUseCase: RefreshOutagesUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val setOnlyTrackedPlacesUseCase: SetOnlyTrackedPlacesUseCase,
    private val observeTrackedPlacesUseCase: ObserveTrackedPlacesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AllOutagesState())
    val state: StateFlow<AllOutagesState> = _state

    private val lastRefreshTimes = mutableMapOf<String, Long>()
    private val REFRESH_THRESHOLD = 5 * 60 * 1000L

    init {
        refreshOnSelectionChange()
        observeData()
    }

    private fun refreshOnSelectionChange() {
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.selectedBranchUrls
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
            val urls = observeSettingsUseCase.selectedBranchUrls.first()
            refresh(urls)
        }
    }

    private suspend fun refresh(urls: Set<String>) {
        if (urls.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        
        val staleUrls = urls.filter { url ->
            val lastTime = lastRefreshTimes[url] ?: 0L
            currentTime - lastTime >= REFRESH_THRESHOLD
        }

        if (staleUrls.isEmpty() && !_state.value.isLoading) {
            _state.update { it.copy(isRefreshing = true) }
            delay(600)
            _state.update { it.copy(isRefreshing = false) }
            return
        }

        _state.update { it.copy(isRefreshing = true) }
        runCatching {
            val urlsToFetch = urls.filter { url ->
                _state.value.isLoading || (currentTime - (lastRefreshTimes[url] ?: 0L) >= REFRESH_THRESHOLD)
            }.toSet()

            if (urlsToFetch.isNotEmpty()) {
                refreshOutagesUseCase.refreshForSelectedUrls(urlsToFetch)
                val now = System.currentTimeMillis()
                urlsToFetch.forEach { lastRefreshTimes[it] = now }
            }
        }.onFailure { throwable ->
            _state.update { it.copy(error = throwable.message) }
        }
        _state.update { it.copy(isLoading = false, isRefreshing = false) }
    }

    private fun observeData() {
        viewModelScope.launch(Dispatchers.IO) {
            getOutagesUseCase().collectLatest { outages ->
                _state.update { it.copy(rawOutages = outages).applyFilter() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            observeTrackedPlacesUseCase().collectLatest { places ->
                _state.update { it.copy(trackedPlaces = places).applyFilter() }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.onlyTrackedPlaces.collectLatest { onlyTracked ->
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
        val filtered = getOutagesUseCase.filterBySearch(rawOutages, searchQuery)
        
        val canSearch = rawOutages.isNotEmpty()
        val effectiveSearchVisible = if (!canSearch) false else isSearchVisible
        val effectiveSearchQuery = if (!effectiveSearchVisible) "" else searchQuery

        val activePlaces = trackedPlaces.filter { it.isEnabled }
        val showTrackedPlacesAction = onlyTrackedPlaces && (
                trackedPlaces.isEmpty() || activePlaces.isEmpty()
                )

        val (emptyMessage, emptyIcon) = when {
            onlyTrackedPlaces && trackedPlaces.isEmpty() ->
                "Нет отслеживаемых адресов. Добавьте их в разделе «Мои адреса»" to Icons.Default.LocationOn
            onlyTrackedPlaces && activePlaces.isEmpty() ->
                "Нет активных отслеживаемых адресов. Включите их в разделе «Мои адреса»" to Icons.Default.LocationOff
            filtered.isEmpty() && effectiveSearchQuery.isNotBlank() ->
                "По запросу «$effectiveSearchQuery» ничего не найдено" to Icons.Default.Search
            filtered.isEmpty() && onlyTrackedPlaces ->
                "По вашим адресам отключения не планируются" to Icons.Default.CheckCircle
            filtered.isEmpty() && rawOutages.isEmpty() ->
                "Отключения не планируются" to Icons.Default.CheckCircle
            else -> null to null
        }

        return copy(
            outages = filtered,
            isSearchVisible = effectiveSearchVisible,
            isSearchEnabled = canSearch,
            searchQuery = effectiveSearchQuery,
            emptyFilterMessage = emptyMessage,
            emptyFilterIcon = emptyIcon,
            showTrackedPlacesAction = showTrackedPlacesAction
        )
    }

    fun toggleFilter(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            setOnlyTrackedPlacesUseCase(enabled)
        }
    }
}
