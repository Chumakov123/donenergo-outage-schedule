package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AllOutagesViewModel(
    private val outageRepository: OutageRepository,
    private val branchRepository: BranchRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<List<Outage>>(emptyList())
    val state = _state.asStateFlow()

    init {
        observeSelectedBranches()
    }

    private fun observeSelectedBranches() {
        viewModelScope.launch(Dispatchers.IO) {
            val branches = branchRepository.getBranches()
            val branchByUrl = branches.associateBy { it.url }

            settingsRepository.selectedBranchUrlsFlow.collectLatest { urls ->
                if (urls.isEmpty()) {
                    _state.value = emptyList()
                    return@collectLatest
                }

                val merged = mutableListOf<Outage>()

                urls.forEach { url ->
                    val branch = branchByUrl[url]
                    val outages = outageRepository.fetchOutages(url)

                    merged += outages.map { outage ->
                        outage.copy(
                            branchName = branch?.name ?: url
                        )
                    }
                }

                _state.value = merged
            }
        }
    }
}