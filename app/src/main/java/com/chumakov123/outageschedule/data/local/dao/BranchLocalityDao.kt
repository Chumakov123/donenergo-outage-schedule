package com.chumakov123.outageschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BranchLocalityDao {

    @Query("SELECT * FROM branch_localities ORDER BY branchName, city, street")
    fun observeAll(): Flow<List<BranchLocalityEntity>>

    @Query("DELETE FROM branch_localities WHERE branchUrl = :branchUrl")
    suspend fun deleteByBranchUrl(branchUrl: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BranchLocalityEntity>)
}