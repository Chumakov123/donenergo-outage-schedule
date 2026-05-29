package com.chumakov123.outageschedule.domain.trackedplace

object HouseMatcher {

    fun matches(
        requestHouse: String,
        candidateExpressions: List<String>,
        fallbackText: String
    ): Boolean {
        val request = requestHouse.trim()
        if (request.isBlank()) return true

        val requestParts = request
            .split(',')
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (candidateExpressions.isNotEmpty()) {
            return requestParts.any { requestPart ->
                candidateExpressions.any { candidatePart ->
                    matchesExpression(requestPart, candidatePart)
                }
            }
        }

        val fallback = AddressNormalizer.compact(fallbackText)
        return requestParts.any { fallback.contains(AddressNormalizer.compact(it)) }
    }

    private fun matchesExpression(request: String, candidate: String): Boolean {
        val left = parse(request) ?: return false
        val right = parse(candidate) ?: return false

        return when {
            left is ParsedHouse.Exact && right is ParsedHouse.Exact ->
                left.normalized == right.normalized ||
                        (left.number == right.number &&
                                left.qualifier.isEmpty() &&
                                right.qualifier.isEmpty())

            left is ParsedHouse.Exact && right is ParsedHouse.Range ->
                left.number in right.from..right.to

            left is ParsedHouse.Range && right is ParsedHouse.Exact ->
                right.number in left.from..left.to

            left is ParsedHouse.Range && right is ParsedHouse.Range ->
                left.from <= right.to && right.from <= left.to

            else -> false
        }
    }

    private fun parse(token: String): ParsedHouse? {
        val value = token.lowercase().replace('ё', 'е').replace(Regex("\\s+"), "")

        RANGE.matchEntire(value)?.let { match ->
            val from = match.groupValues[1].toIntOrNull() ?: return null
            val to = match.groupValues[4].toIntOrNull() ?: return null
            return ParsedHouse.Range(from = minOf(from, to), to = maxOf(from, to))
        }

        EXACT.matchEntire(value)?.let { match ->
            val number = match.groupValues[1].toIntOrNull() ?: return null
            val letter = match.groupValues[2]
            val slash = match.groupValues[3]

            return ParsedHouse.Exact(
                number = number,
                qualifier = buildString {
                    append(letter)
                    if (slash.isNotBlank()) {
                        append("/")
                        append(slash)
                    }
                },
                normalized = value
            )
        }

        return null
    }

    private sealed class ParsedHouse {
        data class Exact(
            val number: Int,
            val qualifier: String,
            val normalized: String
        ) : ParsedHouse()

        data class Range(
            val from: Int,
            val to: Int
        ) : ParsedHouse()
    }

    private val EXACT = Regex("^(\\d+)([a-zа-я])?(?:/(\\d+))?$")
    private val RANGE = Regex("^(\\d+)([a-zа-я])?(?:/(\\d+))?\\s*[-–—]\\s*(\\d+)([a-zа-я])?(?:/(\\d+))?$")
}