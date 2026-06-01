package com.chumakov123.outageschedule.data.remote.parser

import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

class OutageHtmlParser {

    data class ParsedOutage(
        val city: String,
        val address: String,
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

            val city = cols[1].meaningfulTextOrNull() ?: continue
            val address = cols[2].meaningfulTextOrNull() ?: continue

            val startDate = cols[3].meaningfulTextOrNull()
            val endDate = cols[4].meaningfulTextOrNull()

            val startTime = cols[5].meaningfulTextOrNull()?.replace("=", ":")
            val endTime = cols[6].meaningfulTextOrNull()?.replace("=", ":")

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

    private fun Element.meaningfulTextOrNull(): String? {
        val text = text()
            .replace('\u00A0', ' ')
            .trim()

        return text.takeIf { it.containsLetterOrDigit() }
    }

    private fun String.containsLetterOrDigit(): Boolean {
        return any { it.isLetterOrDigit() }
    }
}