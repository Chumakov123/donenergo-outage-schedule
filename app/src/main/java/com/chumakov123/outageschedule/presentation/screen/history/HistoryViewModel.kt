package com.chumakov123.outageschedule.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
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

data class HistoryState(
    val rawItems: List<Outage> = emptyList(),
    val filteredItems: List<Outage> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val outageRepository: OutageRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    init {
        observeHistory()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow
                .distinctUntilChanged()
                .flatMapLatest { urls ->
                    if (urls.isEmpty()) flowOf(emptyList())
                    else outageRepository.observeHistory(urls)
                }
                .collectLatest { items ->
                    _state.update { 
                        it.copy(
                            rawItems = items,
                            isLoading = false
                        ).applyFilter()
                    }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query).applyFilter() }
    }

    private fun HistoryState.applyFilter(): HistoryState {
        val filtered = if (searchQuery.isBlank()) {
            rawItems
        } else {
            val q = searchQuery.trim().lowercase()
            rawItems.filter { 
                it.address.lowercase().contains(q) || 
                it.city.lowercase().contains(q) ||
                it.reason?.lowercase()?.contains(q) == true
            }
        }
        return copy(filteredItems = filtered)
    }
}