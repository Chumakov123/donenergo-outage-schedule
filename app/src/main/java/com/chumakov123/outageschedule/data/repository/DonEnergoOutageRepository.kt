package com.chumakov123.outageschedule.data.repository

import androidx.room.withTransaction
import com.chumakov123.outageschedule.data.local.dao.BranchLocalityDao
import com.chumakov123.outageschedule.data.local.dao.OutageDao
import com.chumakov123.outageschedule.data.local.database.AppDatabase
import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import com.chumakov123.outageschedule.data.local.mapper.toDomain
import com.chumakov123.outageschedule.data.local.mapper.toEntity
import com.chumakov123.outageschedule.data.remote.datasource.OutageRemoteDataSource
import com.chumakov123.outageschedule.data.remote.parser.OutageHtmlParser
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.trackedplace.AddressNormalizer
import com.chumakov123.outageschedule.domain.trackedplace.AddressSegmentParser
import com.chumakov123.outageschedule.domain.util.OutageDateTimeParser
import com.chumakov123.outageschedule.domain.util.OutageStatusCalculator
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.time.LocalDateTime

class DonEnergoOutageRepository(
    private val remote: OutageRemoteDataSource,
    private val parser: OutageHtmlParser,
    private val dao: OutageDao,
    private val database: AppDatabase
) : OutageRepository {

    private val localityDao: BranchLocalityDao = database.branchLocalityDao()

    override suspend fun fetchOutages(branchUrl: String): List<Outage> {
        val html = remote.fetchHtml(branchUrl)
        val doc = Jsoup.parse(html)
        return parser.parse(doc).mapNotNull { parsed ->
            if (parsed.startDate != null && parsed.endDate != null && parsed.address != null) {
                Outage(
                    city = parsed.city,
                    address = parsed.address,
                    startDate = parsed.startDate,
                    endDate = parsed.endDate,
                    startTime = parsed.startTime,
                    endTime = parsed.endTime,
                    reason = parsed.reason,
                    note = parsed.note,
                    branchUrl = branchUrl
                )
            } else {
                null
            }
        }
    }

    override suspend fun refreshOutages(branches: List<Branch>) = coroutineScope {
        cleanupHistory(daysToKeep = 3)
        for (branch in branches) {
            launch {
                try {
                    val html = remote.fetchHtml(branch.url)
                    val doc = Jsoup.parse(html)
                    val parsedOutages = parser.parse(doc)

                    val nowMillis = System.currentTimeMillis()
                    val nowDateTime = LocalDateTime.now()

                    val existingNonFinished = dao.getNonFinishedByBranch(branch.url)
                    val finalizedEntities = existingNonFinished.mapNotNull { entity ->
                        val start = OutageDateTimeParser.parse(entity.startDate, entity.startTime)
                        val end = OutageDateTimeParser.parse(entity.endDate, entity.endTime)
                        val newStatus = OutageStatusCalculator.calculate(nowDateTime, start, end)
                        
                        if (newStatus == OutageStatus.FINISHED) {
                            entity.copy(status = OutageStatus.FINISHED.name, fetchedAt = nowMillis)
                        } else null
                    }

                    val domainOutages = parsedOutages.mapNotNull { parsed ->
                        if (parsed.startDate != null && parsed.endDate != null  && parsed.address != null) {
                            val outage = Outage(
                                city = parsed.city,
                                address = parsed.address,
                                startDate = parsed.startDate,
                                endDate = parsed.endDate,
                                startTime = parsed.startTime,
                                endTime = parsed.endTime,
                                reason = parsed.reason,
                                note = parsed.note,
                                branchUrl = branch.url,
                                branchName = branch.name
                            )
                            val start = OutageDateTimeParser.parse(outage.startDate, outage.startTime)
                            val end = OutageDateTimeParser.parse(outage.endDate, outage.endTime)
                            outage.copy(status = OutageStatusCalculator.calculate(nowDateTime, start, end))
                        } else {
                            null
                        }
                    }

                    val localities = buildLocalityIndex(branch, parsedOutages, nowMillis)

                    database.withTransaction {
                        if (finalizedEntities.isNotEmpty()) {
                            dao.insertAll(finalizedEntities)
                        }

                        dao.deleteCurrentByBranchUrl(branch.url)
                        localityDao.deleteByBranchUrl(branch.url)

                        dao.insertAll(
                            domainOutages.map { outage ->
                                outage.toEntity(
                                    branchUrl = branch.url,
                                    branchName = branch.name,
                                    fetchedAt = nowMillis
                                )
                            }
                        )

                        localityDao.insertAll(localities)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override suspend fun getUpcomingOutages(branchUrls: Set<String>): List<Outage> {
        if (branchUrls.isEmpty()) return emptyList()
        return dao.getUpcomingOutages(branchUrls.toList()).map { it.toDomain() }
    }

    override fun observeOutages(branchUrls: Set<String>): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())
        return dao.observeOutages(branchUrls.toList()).map { list -> list.map { it.toDomain() } }
    }

    override fun observeHistory(branchUrls: Set<String>): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())
        return dao.observeHistory(branchUrls.toList()).map { list -> list.map { it.toDomain() } }
    }

    override fun observeRecentHistory(branchUrls: Set<String>, days: Int): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())
        val threshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return dao.observeRecentHistory(branchUrls.toList(), threshold).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun cleanupHistory(daysToKeep: Int) {
        val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        dao.cleanupOldHistory(threshold)
    }

    private fun buildLocalityIndex(
        branch: Branch,
        parsedOutages: List<OutageHtmlParser.ParsedOutage>,
        now: Long
    ): List<BranchLocalityEntity> {
        val result = linkedMapOf<String, BranchLocalityEntity>()

        fun add(city: String, street: String?) {
            val cleanCity = city.trim()
            if (cleanCity.isBlank()) return

            val cleanStreet = street?.trim()?.takeIf { it.isNotBlank() }
            val key = buildString {
                append(branch.url)
                append("|")
                append(AddressNormalizer.compact(cleanCity))
                append("|")
                append(AddressNormalizer.compact(cleanStreet.orEmpty()))
            }

            if (result.containsKey(key)) return

            result[key] = BranchLocalityEntity(
                id = key,
                branchUrl = branch.url,
                branchName = branch.name,
                city = cleanCity,
                street = cleanStreet,
                normalizedCity = AddressNormalizer.compact(cleanCity),
                normalizedStreet = cleanStreet?.let { AddressNormalizer.compact(it) },
                lastSeenAt = now
            )
        }

        parsedOutages.forEach { outage ->
            add(outage.city, null)

            outage.address
                ?.let { AddressSegmentParser.splitCandidates(it) }
                .orEmpty()
                .forEach { segment ->
                    val street = segment.streetText.trim()
                    if (street.isNotBlank()) {
                        add(outage.city, street)
                    }
                }
        }

        return result.values.toList()
    }
}
