package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.local.dao.BranchLocalityDao
import com.chumakov123.outageschedule.data.local.mapper.toDomain
import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBranchLocalityRepository(
    private val dao: BranchLocalityDao
) : BranchLocalityRepository {

    override fun observeLocalities(): Flow<List<BranchLocality>> {
        return dao.observeAll().map { list -> list.map { it.toDomain() } }
    }
}