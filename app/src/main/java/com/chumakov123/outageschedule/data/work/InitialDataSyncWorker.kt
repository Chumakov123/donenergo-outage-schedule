package com.chumakov123.outageschedule.data.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.chumakov123.outageschedule.domain.usecase.RefreshOutagesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class InitialDataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val refreshOutagesUseCase: RefreshOutagesUseCase by inject()

    override suspend fun doWork(): Result {
        return runCatching {
            refreshOutagesUseCase.refreshAllBranches()
            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}