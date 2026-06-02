package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.trackedplace.AddressNormalizer
import kotlinx.coroutines.flow.Flow

class GetLocationSuggestionsUseCase(
    private val localityRepository: BranchLocalityRepository
) {
    fun observeLocalities(): Flow<List<BranchLocality>> = localityRepository.observeLocalities()

    fun getCitySuggestions(
        localities: List<BranchLocality>,
        selectedBranchUrls: Set<String>,
        query: String,
        limit: Int = 5
    ): List<String> {
        val q = AddressNormalizer.compact(query)
        return localities.asSequence()
            .filter { it.branchUrl in selectedBranchUrls }
            .map { it.city.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { city ->
                q.isBlank() || AddressNormalizer.compact(city).contains(q)
            }
            .take(limit)
            .toList()
    }

    fun getStreetSuggestions(
        localities: List<BranchLocality>,
        selectedBranchUrls: Set<String>,
        cityQuery: String,
        streetQuery: String,
        limit: Int = 5
    ): List<String> {
        val cityQ = AddressNormalizer.compact(cityQuery)
        val streetQ = AddressNormalizer.compact(streetQuery)

        return localities.asSequence()
            .filter { it.branchUrl in selectedBranchUrls }
            .filter { locality ->
                cityQ.isBlank() || AddressNormalizer.compact(locality.city).contains(cityQ)
            }
            .mapNotNull { it.street?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { street ->
                streetQ.isBlank() || AddressNormalizer.compact(street).contains(streetQ)
            }
            .take(limit)
            .toList()
    }
}