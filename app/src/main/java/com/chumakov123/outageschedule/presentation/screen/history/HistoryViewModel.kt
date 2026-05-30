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
                    _state.value = items
                }
        }
    }
}