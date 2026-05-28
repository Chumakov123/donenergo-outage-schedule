package com.chumakov123.outageschedule.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val outageRepository: OutageRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<List<Outage>>(emptyList())
    val state: StateFlow<List<Outage>> = _state

    init {
        observeHistory()
    }

    private fun observeHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.selectedBranchUrlsFlow.collectLatest { urls ->
                outageRepository.observeHistory(urls).collectLatest { items ->
                    _state.value = items
                }
            }
        }
    }
}