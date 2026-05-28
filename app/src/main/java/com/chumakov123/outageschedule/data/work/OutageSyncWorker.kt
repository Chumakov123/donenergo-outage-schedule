package com.chumakov123.outageschedule.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OutageSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val settingsRepository: AppSettingsRepository by inject()
    private val branchRepository: BranchRepository by inject()
    private val outageRepository: OutageRepository by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            val selectedUrls = settingsRepository.selectedBranchUrlsFlow.first()

            if (selectedUrls.isEmpty()) {
                return Result.success()
            }

            val allBranches = branchRepository.getBranches()
            val selectedBranches = allBranches.filter { it.url in selectedUrls }

            if (selectedBranches.isEmpty()) {
                return Result.success()
            }

            outageRepository.refreshOutages(selectedBranches)
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}