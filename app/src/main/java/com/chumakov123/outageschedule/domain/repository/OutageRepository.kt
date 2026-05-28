package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.Outage

interface OutageRepository {

    suspend fun fetchOutages(
        branchUrl: String
    ): List<Outage>
}