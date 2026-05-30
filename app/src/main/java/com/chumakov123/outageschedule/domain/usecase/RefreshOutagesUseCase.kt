package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.repository.BranchRepository
import com.chumakov123.outageschedule.domain.repository.OutageRepository

class RefreshOutagesUseCase(
    private val branchRepository: BranchRepository,
    private val outageRepository: OutageRepository
) {
    suspend fun refreshForSelectedUrls(selectedUrls: Set<String>) {
        if (selectedUrls.isEmpty()) return
        val selectedBranches = branchRepository.getBranches()
            .filter { it.url in selectedUrls }
        if (selectedBranches.isNotEmpty()) {
            outageRepository.refreshOutages(selectedBranches)
        }
    }

    suspend fun refreshAllBranches() {
        val branches = branchRepository.getBranches()
        if (branches.isNotEmpty()) {
            outageRepository.refreshOutages(branches)
        }
    }
}