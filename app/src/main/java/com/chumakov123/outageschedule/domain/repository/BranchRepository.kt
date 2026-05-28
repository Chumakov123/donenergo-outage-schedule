package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.Branch

interface BranchRepository {
    suspend fun getBranches(): List<Branch>
}