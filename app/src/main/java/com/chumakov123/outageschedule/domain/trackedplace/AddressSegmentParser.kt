package com.chumakov123.outageschedule.domain.trackedplace

import java.util.Locale

data class AddressSegment(
    val raw: String,
    val streetText: String,
    val houseExpressions: List<String>
)

object AddressSegmentParser {

    private val leadingTypes = setOf(
        "улица",
        "переулок",
        "проспект",
        "площадь",
        "шоссе",
        "снт",
        "дол",
        "дод",
        "микрорайон",
        "город",
        "поселок",
        "село",
        "станица",
        "слобода",
        "хутор"
    )

    private val houseExact = Regex("^\\d+[a-zа-я]?(?:/\\d+)?$")
    private val houseRange = Regex("^\\d+[a-zа-я]?(?:/\\d+)?\\s*[-–—]\\s*\\d+[a-zа-я]?(?:/\\d+)?$")

    fun splitCandidates(address: String): List<AddressSegment> {
        return address
            .split(';')
            .mapNotNull { parseSegment(it) }
    }

    fun parseSegment(segment: String): AddressSegment? {
        val cleaned = segment.trim().replace(Regex("\\s+"), " ")
        if (cleaned.isBlank()) return null

        val tokens = cleaned.split(" ").filter { it.isNotBlank() }
        if (tokens.isEmpty()) return null

        var start = 0
        if (isLeadingTypeToken(tokens[0])) {
            start = 1
        }

        var end = tokens.size
        val houses = mutableListOf<String>()

        while (end > start) {
            val token = stripTrailingPunctuation(tokens[end - 1])
            if (!isHouseListToken(token)) break

            houses += splitHouseList(token)
            end--
        }

        val street = tokens.subList(start, end).joinToString(" ").trim()

        return AddressSegment(
            raw = cleaned,
            streetText = street,
            houseExpressions = houses.asReversed()
        )
    }

    private fun isLeadingTypeToken(token: String): Boolean {
        val normalized = AddressNormalizer.normalizeComparable(token)
        return normalized in leadingTypes
    }

    private fun stripTrailingPunctuation(token: String): String {
        return token.trim().trim(',', '.', ';')
    }

    private fun splitHouseList(token: String): List<String> {
        return token
            .split(',')
            .map { it.trim().trim(',', '.', ';') }
            .filter { it.isNotBlank() }
    }

    private fun isHouseListToken(token: String): Boolean {
        val parts = splitHouseList(token)
        return parts.isNotEmpty() && parts.all { isHouseExpression(it) }
    }

    private fun isHouseExpression(token: String): Boolean {
        val value = token.lowercase(Locale.getDefault()).replace('ё', 'е').trim()
        return houseExact.matches(value) || houseRange.matches(value)
    }
}