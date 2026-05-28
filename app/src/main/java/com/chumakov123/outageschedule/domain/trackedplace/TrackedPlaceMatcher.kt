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
        if (!containsNormalized(outage.city, place.city)) return false
        if (!containsNormalized(outage.address, place.street)) return false

        val house = place.house.trim()
        if (house.isNotEmpty() && !containsNormalized(outage.address, house)) return false

        return true
    }

    private fun containsNormalized(source: String, query: String): Boolean {
        if (query.isBlank()) return true
        return normalize(source).contains(normalize(query))
    }

    private fun normalize(text: String): String {
        return text.lowercase()
            .replace(Regex("[^\\p{L}\\p{Nd}]"), "")
    }
}