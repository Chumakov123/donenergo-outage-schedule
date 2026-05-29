package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.BranchLocality
import kotlinx.coroutines.flow.Flow

interface BranchLocalityRepository {
    fun observeLocalities(): Flow<List<BranchLocality>>
}