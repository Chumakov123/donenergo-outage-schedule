package com.chumakov123.outageschedule.data.remote.parser

import com.chumakov123.outageschedule.domain.model.Outage
import org.jsoup.nodes.Document

class OutageHtmlParser {

    fun parse(document: Document): List<Outage> {

        val result = mutableListOf<Outage>()

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

            result.add(
                Outage(
                    city = city,
                    address = address,
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    reason = reason
                )
            )
        }

        return result
    }
}