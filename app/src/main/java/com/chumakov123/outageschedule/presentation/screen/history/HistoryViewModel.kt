package com.chumakov123.outageschedule.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.usecase.GetHistoryUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryState(
    val rawItems: List<Outage> = emptyList(),
    val filteredItems: List<Outage> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HistoryState())
    val state: StateFlow<HistoryState> = _state

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            getHistoryUseCase().collectLatest { items ->
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
        val filtered = getHistoryUseCase.filterBySearch(rawItems, searchQuery)
        return copy(filteredItems = filtered)
    }
}