package com.chumakov123.outageschedule.data.work

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.data.notification.NotificationChannels
import com.chumakov123.outageschedule.domain.notification.OutageNotifier
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
    private val outageNotifier: OutageNotifier by inject()

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
                outageNotifier.show(
                    outage = prep.outage,
                    leadHours = prep.leadHours,
                    placeTitles = prep.matchedPlaceTitles
                )
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
}