package com.chumakov123.outageschedule.data.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.data.notification.NotificationChannels
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.NotificationLogRepository
import com.chumakov123.outageschedule.domain.usecase.PrepareOutageNotificationsUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: AppSettingsRepository by inject()
    private val prepareUseCase: PrepareOutageNotificationsUseCase by inject()
    private val notificationLogRepository: NotificationLogRepository by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) return Result.success()
            }

            val selectedUrls = settingsRepository.selectedBranchUrlsFlow.first()
            val leadHours = settingsRepository.notificationLeadHoursFlow.first()
            if (selectedUrls.isEmpty() || leadHours.isEmpty()) return Result.success()

            NotificationChannels.ensure(applicationContext)

            val notifications = prepareUseCase.prepare(selectedUrls, leadHours)

            for (prep in notifications) {
                showNotification(prep.outage, prep.leadHours)
                notificationLogRepository.markSent(
                    key = prep.notificationKey,
                    outageId = prep.outage.buildId(),
                    leadHours = prep.leadHours
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
        } catch (_: SecurityException) { }
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
}