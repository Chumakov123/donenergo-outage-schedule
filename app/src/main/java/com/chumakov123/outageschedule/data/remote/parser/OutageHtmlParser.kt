package com.chumakov123.outageschedule.data.remote.parser

import org.jsoup.nodes.Document

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

            val city = cols[1].text().trim()
            val address = cols[2].text().trim()

            if (city.matches(Regex("\\d+"))) continue
            if (address.matches(Regex("\\d+"))) continue

            val startDate = cols[3].text().trim().takeIf { it.isNotEmpty() }
            val endDate = cols[4].text().trim().takeIf { it.isNotEmpty() }

            val startTime = cols[5].text().trim().replace("=", ":")
            val endTime = cols[6].text().trim().replace("=", ":")

            val reason = cols[7].text().trim()
            val note = if (cols.size > 8) cols[8].text().trim().takeIf { it.isNotEmpty() } else null

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
}