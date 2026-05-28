package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.local.dao.OutageDao
import com.chumakov123.outageschedule.data.local.mapper.toDomain
import com.chumakov123.outageschedule.data.local.mapper.toEntity
import com.chumakov123.outageschedule.data.remote.datasource.OutageRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.OutageHtmlParser
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup

class DonEnergoOutageRepository(
    private val remote: OutageRemoteDataSource,
    private val parser: OutageHtmlParser,
    private val dao: OutageDao
) : OutageRepository {

    override suspend fun fetchOutages(branchUrl: String): List<Outage> {
        val html = remote.fetchHtml(branchUrl)
        val doc = Jsoup.parse(html)
        return parser.parse(doc)
    }

    override suspend fun refreshOutages(branches: List<Branch>) {
        val now = System.currentTimeMillis()

        for (branch in branches) {
            val outages = fetchOutages(branch.url)

            dao.deleteByBranchUrl(branch.url)

            dao.insertAll(
                outages.map { outage ->
                    outage.toEntity(
                        branchUrl = branch.url,
                        branchName = branch.name,
                        fetchedAt = now
                    )
                }
            )
        }
    }

    override fun observeOutages(branchUrls: Set<String>): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())

        return dao.observeOutages(branchUrls.toList())
            .map { list -> list.map { it.toDomain() } }
    }
}