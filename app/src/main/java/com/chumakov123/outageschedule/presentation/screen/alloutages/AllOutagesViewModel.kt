package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.presentation.navigation.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AllOutagesViewModel(
    private val repository: OutageRepository
) : ViewModel() {

    private val _state = MutableStateFlow<List<Outage>>(emptyList())
    val state = _state.asStateFlow()

    init {
        load()
    }

    private fun load() {

        val branch = AppState.selectedBranch
            ?: return

        viewModelScope.launch(Dispatchers.IO) {

            val data = repository.fetchOutages(
                branch.url
            )

            _state.value = data
        }
    }
}