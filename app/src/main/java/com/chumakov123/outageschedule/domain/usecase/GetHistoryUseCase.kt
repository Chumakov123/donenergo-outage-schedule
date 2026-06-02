package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class GetHistoryUseCase(
    private val outageRepository: OutageRepository,
    private val settingsRepository: AppSettingsRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Outage>> {
        return settingsRepository.selectedBranchUrlsFlow
            .flatMapLatest { urls ->
                if (urls.isEmpty()) flowOf(emptyList())
                else outageRepository.observeHistory(urls)
            }
    }

    fun filterBySearch(items: List<Outage>, query: String): List<Outage> {
        if (query.isBlank()) return items
        val q = query.trim().lowercase()
        return items.filter {
            it.address.lowercase().contains(q) ||
                    it.city.lowercase().contains(q) ||
                    it.reason?.lowercase()?.contains(q) == true
        }
    }
}