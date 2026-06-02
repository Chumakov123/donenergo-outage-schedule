package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.Branch
import com.chumakov123.outageschedule.domain.model.Outage
import kotlinx.coroutines.flow.Flow

interface OutageRepository {

    suspend fun fetchOutages(branchUrl: String): List<Outage>

    suspend fun refreshOutages(branches: List<Branch>)
    suspend fun getUpcomingOutages(branchUrls: Set<String>): List<Outage>

    fun observeOutages(branchUrls: Set<String>): Flow<List<Outage>>
    
    fun observeRecentHistory(branchUrls: Set<String>, days: Int): Flow<List<Outage>>

    suspend fun cleanupHistory(daysToKeep: Int)
}
