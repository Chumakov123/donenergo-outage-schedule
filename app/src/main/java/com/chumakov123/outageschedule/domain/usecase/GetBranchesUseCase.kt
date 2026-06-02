package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.branch.BranchSelector
import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.repository.BranchRepository

class GetBranchesUseCase(
    private val branchRepository: BranchRepository
) {
    suspend operator fun invoke(): List<Branch> = branchRepository.getBranches()

    fun getDefaultBranch(branches: List<Branch>): Branch = BranchSelector.findDefault(branches)
}