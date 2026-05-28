package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.remote.datasource.OutageRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.OutageHtmlParser
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import org.jsoup.Jsoup

class DonEnergoOutageRepository(
    private val remote: OutageRemoteDataSource,
    private val parser: OutageHtmlParser
) : OutageRepository {

    override suspend fun fetchOutages(
        branchUrl: String
    ): List<Outage> {

        val html = remote.fetchHtml(branchUrl)
        val doc = Jsoup.parse(html)

        return parser.parse(doc)
    }
}