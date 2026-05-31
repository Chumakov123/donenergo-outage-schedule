package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.PreparedNotification
import com.chumakov123.outageschedule.domain.repository.NotificationLogRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
import com.chumakov123.outageschedule.domain.util.OutageDateTimeParser
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime

class PrepareOutageNotificationsUseCase(
    private val outageRepository: OutageRepository,
    private val trackedPlaceRepository: TrackedPlaceRepository,
    private val notificationLogRepository: NotificationLogRepository
) {
    suspend fun prepare(
        selectedUrls: Set<String>,
        leadHours: Set<Int>,
        now: LocalDateTime = LocalDateTime.now()
    ): List<PreparedNotification> {
        if (selectedUrls.isEmpty() || leadHours.isEmpty()) return emptyList()

        val outages = outageRepository.getUpcomingOutages(selectedUrls)
        val trackedPlaces = trackedPlaceRepository.observePlaces().first().filter { it.isEnabled }
        if (trackedPlaces.isEmpty()) return emptyList()

        val result = mutableListOf<PreparedNotification>()

        for (outage in outages) {
            val start = OutageDateTimeParser.parse(outage.startDate, outage.startTime) ?: continue
            val minutesLeft = Duration.between(now, start).toMinutes()

            val matchedLead = leadHours.firstOrNull { lead ->
                val targetMinutes = lead * 60L
                minutesLeft in (targetMinutes - 59L)..targetMinutes
            } ?: continue

            val matches = TrackedPlaceMatcher.findAllMatches(outage, trackedPlaces)
            if (matches.isEmpty()) continue

            val notificationKey = "${outage.buildId()}|$matchedLead"

            if (notificationLogRepository.wasSent(notificationKey)) continue

            result.add(
                PreparedNotification(
                    outage = outage,
                    leadHours = matchedLead,
                    notificationKey = notificationKey,
                    matchedPlaceTitles = matches.map { it.place.title }
                )
            )
        }

        return result
    }
}