package com.chumakov123.outageschedule.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.usecase.RefreshOutagesUseCase
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OutageSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: AppSettingsRepository by inject()
    private val refreshOutagesUseCase: RefreshOutagesUseCase by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            val selectedUrls = settingsRepository.selectedBranchUrlsFlow.first()
            refreshOutagesUseCase.refreshForSelectedUrls(selectedUrls)
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}