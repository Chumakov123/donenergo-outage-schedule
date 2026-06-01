package com.chumakov123.outageschedule.data.remote.parser

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class OutageHtmlParser {

    data class ParsedOutage(
        val city: String,
        val address: String?,
        val startDate: String?,
        val endDate: String?,
        val startTime: String?,
        val endTime: String?,
        val reason: String?,
        val note: String?
    )

    fun parse(document: Document): List<ParsedOutage> {
        val result = mutableListOf<ParsedOutage>()
        val rows = document.select("table.table_site1 tbody tr")

        for (row in rows) {
            if (row.select("th").isNotEmpty()) continue

            val cols = row.select("td")
            if (cols.size < 8) continue

            val city = cols[1].meaningfulTextOrNull(requireLetter = true) ?: continue
            val address = cols[2].meaningfulTextOrNull(requireLetter = true)

            val startDate = cols[3].meaningfulTextOrNull(requireDigit = true)
            val endDate = cols[4].meaningfulTextOrNull(requireDigit = true)

            val startTime = cols[5].meaningfulTextOrNull(requireDigit = true)?.replace("=", ":")
            val endTime = cols[6].meaningfulTextOrNull(requireDigit = true)?.replace("=", ":")

            val reason = cols[7].meaningfulTextOrNull()
            val note = if (cols.size > 8) cols[8].meaningfulTextOrNull() else null

            result.add(
                ParsedOutage(
                    city = city,
                    address = address,
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason,
                    note = note
                )
            )
        }

        return result
    }

    private fun Element.meaningfulTextOrNull(
        requireLetter: Boolean = false,
        requireDigit: Boolean = false
    ): String? {
        val text = text()
            .replace('\u00A0', ' ')
            .trim()

        if (text.isBlank()) return null
        if (!text.containsLetterOrDigit()) return null
        if (requireLetter && !text.containsLetter()) return null
        if (requireDigit && !text.containsDigit()) return null

        return text
    }

    private fun String.containsLetterOrDigit(): Boolean = any { it.isLetterOrDigit() }
    private fun String.containsLetter(): Boolean = any { it.isLetter() }
    private fun String.containsDigit(): Boolean = any { it.isDigit() }
}