package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class GetOutagesUseCase(
    private val outageRepository: OutageRepository,
    private val settingsRepository: AppSettingsRepository,
    private val trackedPlaceRepository: TrackedPlaceRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Outage>> {
        return combine(
            settingsRepository.selectedBranchUrlsFlow,
            settingsRepository.onlyTrackedPlacesFlow,
            trackedPlaceRepository.observePlaces()
        ) { urls, onlyTracked, places ->
            Triple(urls, onlyTracked, places)
        }.flatMapLatest { (urls, onlyTracked, places) ->
            if (urls.isEmpty()) {
                flowOf(emptyList())
            } else {
                combine(
                    outageRepository.observeOutages(urls),
                    outageRepository.observeRecentHistory(urls, days = 3)
                ) { current, history ->
                    current + history
                }.map { outages ->
                    val activePlaces = places.filter { it.isEnabled }
                    if (onlyTracked) {
                        TrackedPlaceMatcher.filter(outages, activePlaces)
                    } else {
                        outages
                    }
                }
            }
        }
    }

    fun filterBySearch(outages: List<Outage>, query: String): List<Outage> {
        if (query.isBlank()) return outages
        val q = query.trim().lowercase()
        return outages.filter {
            it.address.lowercase().contains(q) ||
                    it.city.lowercase().contains(q) ||
                    it.reason?.lowercase()?.contains(q) == true
        }
    }
}
