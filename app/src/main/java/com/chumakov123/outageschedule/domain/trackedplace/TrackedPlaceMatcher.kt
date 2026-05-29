package com.chumakov123.outageschedule.domain.trackedplace

import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.TrackedPlace

object TrackedPlaceMatcher {

    fun filter(
        outages: List<Outage>,
        places: List<TrackedPlace>
    ): List<Outage> {
        if (places.isEmpty()) return outages

        return outages.filter { outage ->
            places.any { place -> matches(place, outage) }
        }
    }

    private fun matches(place: TrackedPlace, outage: Outage): Boolean {
        val placeCity = AddressNormalizer.normalizeComparable(place.city)
        val placeStreet = AddressNormalizer.normalizeComparable(place.street)
        val outageCity = AddressNormalizer.normalizeComparable(outage.city)

        if (placeCity.isNotBlank() && !containsEitherWay(outageCity, placeCity)) {
            return false
        }

        val segments = AddressSegmentParser.splitCandidates(outage.address)

        if (segments.isEmpty()) {
            val normalizedAddress = AddressNormalizer.normalizeComparable(outage.address)

            val streetOk = placeStreet.isBlank() ||
                    normalizedAddress.contains(placeStreet) ||
                    placeStreet.contains(normalizedAddress)

            if (!streetOk) return false

            return HouseMatcher.matches(
                requestHouse = place.house,
                candidateExpressions = emptyList(),
                fallbackText = outage.address
            )
        }

        return segments.any { segment ->
            val normalizedStreet = AddressNormalizer.normalizeComparable(segment.streetText)

            val streetOk = placeStreet.isBlank() ||
                    normalizedStreet.contains(placeStreet) ||
                    placeStreet.contains(normalizedStreet)

            if (!streetOk) return@any false

            HouseMatcher.matches(
                requestHouse = place.house,
                candidateExpressions = segment.houseExpressions,
                fallbackText = segment.raw
            )
        }
    }

    private fun containsEitherWay(source: String, query: String): Boolean {
        return source.contains(query) || query.contains(source)
    }
}