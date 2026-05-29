package com.chumakov123.outageschedule.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InitialDataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val branchRepository: BranchRepository by inject()
    private val outageRepository: OutageRepository by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            val branches = branchRepository.getBranches()

            if (branches.isEmpty()) {
                Result.success()
            } else {
                outageRepository.refreshOutages(branches)
                Result.success()
            }
        }.getOrElse {
            Result.retry()
        }
    }
}