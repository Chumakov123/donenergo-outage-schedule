package com.chumakov123.outageschedule.domain.trackedplace

import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.TrackedPlace

data class TrackedPlaceMatch(
    val place: TrackedPlace,
    val matchedStreetText: String? = null
)

object TrackedPlaceMatcher {

    fun filter(
        outages: List<Outage>,
        places: List<TrackedPlace>
    ): List<Outage> {
        if (places.isEmpty()) return emptyList()

        return outages.filter { outage ->
            places.any { place -> matches(place, outage) }
        }
    }

    fun findBestMatch(
        outage: Outage,
        places: List<TrackedPlace>
    ): TrackedPlaceMatch? {
        return places.asSequence()
            .mapNotNull { place -> findMatch(place, outage) }
            .firstOrNull()
    }

    fun findAllMatches(
        outage: Outage,
        places: List<TrackedPlace>
    ): List<TrackedPlaceMatch> {
        return places.mapNotNull { place -> findMatch(place, outage) }
    }

    fun matches(place: TrackedPlace, outage: Outage): Boolean {
        return findMatch(place, outage) != null
    }

    private fun findMatch(place: TrackedPlace, outage: Outage): TrackedPlaceMatch? {
        val placeCity = AddressNormalizer.normalizeComparable(place.city)
        val placeStreet = AddressNormalizer.normalizeComparable(place.street)
        val outageCity = AddressNormalizer.normalizeComparable(outage.city)

        if (placeCity.isNotBlank() && !containsEitherWay(outageCity, placeCity)) {
            return null
        }

        val segments = AddressSegmentParser.splitCandidates(outage.address)

        if (segments.isEmpty()) {
            val normalizedAddress = AddressNormalizer.normalizeComparable(outage.address)

            val streetOk = placeStreet.isBlank() ||
                    normalizedAddress.contains(placeStreet) ||
                    placeStreet.contains(normalizedAddress)

            if (!streetOk) return null

            if (!HouseMatcher.matches(
                    requestHouse = place.house,
                    candidateExpressions = emptyList(),
                    fallbackText = outage.address
                )
            ) {
                return null
            }

            return TrackedPlaceMatch(
                place = place,
                matchedStreetText = place.street.takeIf { it.isNotBlank() }
            )
        }

        for (segment in segments) {
            val normalizedStreet = AddressNormalizer.normalizeComparable(segment.streetText)

            val streetOk = placeStreet.isBlank() ||
                    normalizedStreet.contains(placeStreet) ||
                    placeStreet.contains(normalizedStreet)

            if (!streetOk) continue

            if (!HouseMatcher.matches(
                    requestHouse = place.house,
                    candidateExpressions = segment.houseExpressions,
                    fallbackText = segment.raw
                )
            ) {
                continue
            }

            return TrackedPlaceMatch(
                place = place,
                matchedStreetText = segment.streetText.takeIf { it.isNotBlank() } ?: place.street.takeIf { it.isNotBlank() }
            )
        }

        return null
    }

    private fun containsEitherWay(source: String, query: String): Boolean {
        return source.contains(query) || query.contains(source)
    }
}