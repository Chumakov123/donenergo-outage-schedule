package com.chumakov123.outageschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chumakov123.outageschedule.data.local.entity.OutageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutageDao {

    @Query(
        """
        SELECT * FROM outages
        WHERE branchUrl IN (:branchUrls)
          AND status != 'FINISHED'
        ORDER BY branchName, city, address
        """
    )
    fun observeOutages(branchUrls: List<String>): Flow<List<OutageEntity>>

    @Query(
        """
        SELECT * FROM outages
        WHERE branchUrl IN (:branchUrls)
          AND status = 'FINISHED'
          AND fetchedAt >= :minTimestamp
        ORDER BY fetchedAt DESC, branchName, city, address
        """
    )
    fun observeRecentHistory(branchUrls: List<String>, minTimestamp: Long): Flow<List<OutageEntity>>

    @Query(
        """
        SELECT * FROM outages
        WHERE branchUrl = :branchUrl
          AND status != 'FINISHED'
        """
    )
    suspend fun getNonFinishedByBranch(branchUrl: String): List<OutageEntity>

    @Query(
        """
        SELECT * FROM outages
        WHERE branchUrl IN (:branchUrls)
          AND status = 'UPCOMING'
        ORDER BY branchName, city, address
        """
    )
    suspend fun getUpcomingOutages(branchUrls: List<String>): List<OutageEntity>

    @Query(
        """
        DELETE FROM outages
        WHERE branchUrl = :branchUrl
          AND status != 'FINISHED'
        """
    )
    suspend fun deleteCurrentByBranchUrl(branchUrl: String)

    @Query(
        """
        DELETE FROM outages
        WHERE status = 'FINISHED'
          AND fetchedAt < :threshold
        """
    )
    suspend fun cleanupOldHistory(threshold: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<OutageEntity>)
}
