package com.chumakov123.outageschedule.data.remote.parser

import com.chumakov123.outageschedule.domain.model.Branch
import org.jsoup.nodes.Document

class BranchIndexParser {

    fun parse(document: Document): List<Branch> {

        val header = document.selectFirst("h2.red-text")
            ?: return emptyList()

        val result = mutableListOf<Branch>()

        for (element in header.nextElementSiblings()) {

            if (element.tagName().equals("h2", ignoreCase = true)) {
                break
            }

            if (
                element.tagName().equals("a", ignoreCase = true) &&
                element.hasClass("link")
            ) {

                val name = element.text().trim()
                val href = element.attr("href").trim()

                if (
                    name.isNotEmpty() &&
                    href.isNotEmpty()
                ) {
                    result += Branch(
                        name = name,
                        url = href
                    )
                }
            }
        }

        return result
    }
}