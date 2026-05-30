package com.chumakov123.outageschedule.data.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.data.local.mapper.buildOutageId
import com.chumakov123.outageschedule.data.notification.NotificationChannels
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.NotificationLogRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.util.OutageDateTimeParser
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.Duration
import java.time.LocalDateTime
import java.util.Locale

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: AppSettingsRepository by inject()
    private val outageRepository: OutageRepository by inject()
    private val trackedPlaceRepository: TrackedPlaceRepository by inject()
    private val notificationLogRepository: NotificationLogRepository by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!granted) {
                    return Result.success()
                }
            }

            val selectedUrls = settingsRepository.selectedBranchUrlsFlow.first()
            val leadHours = settingsRepository.notificationLeadHoursFlow.first()
            val trackedPlaces = trackedPlaceRepository.observePlaces().first().filter { it.isEnabled }

            if (selectedUrls.isEmpty() || leadHours.isEmpty() || trackedPlaces.isEmpty()) {
                return Result.success()
            }

            NotificationChannels.ensure(applicationContext)

            val outages = outageRepository.getUpcomingOutages(selectedUrls)   // List<Outage>
            val now = LocalDateTime.now()

            for (outage in outages) {
                val start = OutageDateTimeParser.parse(outage.startDate, outage.startTime) ?: continue
                val minutesLeft = Duration.between(now, start).toMinutes()

                val matchedLead = leadHours.firstOrNull { lead ->
                    val targetMinutes = lead * 60L
                    minutesLeft in (targetMinutes - 59L)..targetMinutes
                } ?: continue

                if (!matchesAnyTrackedPlace(outage, trackedPlaces)) {
                    continue
                }

                val outageId = buildOutageId(outage)
                val notificationKey = "$outageId|$matchedLead"

                if (notificationLogRepository.wasSent(notificationKey)) {
                    continue
                }

                showNotification(outage, matchedLead)

                notificationLogRepository.markSent(
                    key = notificationKey,
                    outageId = outageId,
                    leadHours = matchedLead
                )
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun showNotification(outage: Outage, leadHours: Int) {
        val title = "Предстоящее отключение"
        val city = outage.city.trim()
        val address = outage.address.trim()
        val text = if (city.isNotBlank()) {
            "$city, $address. Через $leadHours ч."
        } else {
            "$address. Через $leadHours ч."
        }

        val notification = NotificationCompat.Builder(
            applicationContext,
            NotificationChannels.OUTAGE_ALERTS_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(applicationContext)
                .notify(buildNotificationId(outage, leadHours), notification)
        } catch (_: SecurityException) {
            // Permission may have been revoked between the check and notify().
        }
    }

    private fun matchesAnyTrackedPlace(
        outage: Outage,
        places: List<TrackedPlace>
    ): Boolean {
        return places.any { place -> matchesTrackedPlace(outage, place) }
    }

    private fun matchesTrackedPlace(
        outage: Outage,
        place: TrackedPlace
    ): Boolean {
        val outageCity = compact(outage.city)
        val trackedCity = compact(place.city)

        if (outageCity != trackedCity) {
            return false
        }

        val outageAddress = compact(outage.address)
        val street = compact(place.street)
        val house = compact(place.house)

        if (street.isNotBlank() && !outageAddress.contains(street)) {
            return false
        }

        if (house.isNotBlank() && !outageAddress.contains(house)) {
            return false
        }

        return true
    }

    private fun buildNotificationId(outage: Outage, leadHours: Int): Int {
        return listOf(
            outage.city,
            outage.address,
            outage.startDate.orEmpty(),
            outage.endDate.orEmpty(),
            outage.startTime.orEmpty(),
            outage.endTime.orEmpty(),
            leadHours.toString()
        ).joinToString("|").hashCode()
    }

    private fun compact(value: String?): String {
        return value
            .orEmpty()
            .lowercase(Locale.getDefault())
            .replace(Regex("\\s+"), "")
            .replace(Regex("[^\\p{L}\\p{Nd}]"), "")
    }
}