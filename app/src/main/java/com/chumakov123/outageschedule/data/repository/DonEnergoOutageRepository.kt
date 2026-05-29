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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class DonEnergoOutageRepository(
    private val remote: OutageRemoteDataSource,
    private val parser: OutageHtmlParser,
    private val dao: OutageDao,
    private val database: AppDatabase
) : OutageRepository {

    private val localityDao: BranchLocalityDao = database.branchLocalityDao()
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.getDefault())

    override suspend fun fetchOutages(branchUrl: String): List<Outage> {
        val html = remote.fetchHtml(branchUrl)
        val doc = Jsoup.parse(html)
        return parser.parse(doc)
    }

    override suspend fun refreshOutages(branches: List<Branch>) {
        val now = System.currentTimeMillis()

        database.withTransaction {
            for (branch in branches) {
                val outages = fetchOutages(branch.url).map { outage ->
                    outage.copy(status = calculateStatus(outage))
                }

                val localities = buildLocalityIndex(branch, outages, now)

                dao.deleteCurrentByBranchUrl(branch.url)
                localityDao.deleteByBranchUrl(branch.url)

                dao.insertAll(
                    outages.map { outage ->
                        outage.toEntity(
                            branchUrl = branch.url,
                            branchName = branch.name,
                            fetchedAt = now
                        )
                    }
                )

                localityDao.insertAll(localities)
            }
        }
    }

    override fun observeOutages(branchUrls: Set<String>): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())
        return dao.observeOutages(branchUrls.toList()).map { list -> list.map { it.toDomain() } }
    }

    override fun observeHistory(branchUrls: Set<String>): Flow<List<Outage>> {
        if (branchUrls.isEmpty()) return flowOf(emptyList())
        return dao.observeHistory(branchUrls.toList()).map { list -> list.map { it.toDomain() } }
    }

    private fun buildLocalityIndex(
        branch: Branch,
        outages: List<Outage>,
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

        outages.forEach { outage ->
            add(outage.city, null)

            AddressSegmentParser.splitCandidates(outage.address).forEach { segment ->
                val street = segment.streetText.trim()
                if (street.isNotBlank()) {
                    add(outage.city, street)
                }
            }
        }

        return result.values.toList()
    }

    private fun calculateStatus(outage: Outage): OutageStatus {
        val now = LocalDateTime.now()
        val start = parseDateTime(outage.startDate, outage.startTime)
        val end = parseDateTime(outage.endDate, outage.endTime)

        return when {
            end != null && now.isAfter(end) -> OutageStatus.FINISHED
            start != null && now.isBefore(start) -> OutageStatus.UPCOMING
            start != null || end != null -> OutageStatus.ACTIVE
            else -> OutageStatus.UPCOMING
        }
    }

    private fun parseDateTime(date: String?, time: String?): LocalDateTime? {
        val d = date?.takeIf { it.isNotBlank() } ?: return null
        val t = time?.takeIf { it.isNotBlank() } ?: return null
        return runCatching { LocalDateTime.parse("$d $t", formatter) }.getOrNull()
    }
}