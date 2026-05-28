package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AllOutagesViewModel(
    private val repository: OutageRepository,
    private val settings: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<List<Outage>>(emptyList())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch(Dispatchers.IO) {

            val urls = settings.selectedBranchUrlsFlow.first()

            if (urls.isEmpty()) return@launch

            val all = urls.flatMap { url ->
                repository.fetchOutages(url)
            }

            _state.value = all
        }
    }
}