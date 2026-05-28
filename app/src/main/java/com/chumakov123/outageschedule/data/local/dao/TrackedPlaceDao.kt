package com.chumakov123.outageschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chumakov123.outageschedule.data.local.entity.TrackedPlaceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackedPlaceDao {

    @Query("SELECT * FROM tracked_places ORDER BY title, city, street, house")
    fun observePlaces(): Flow<List<TrackedPlaceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(place: TrackedPlaceEntity): Long

    @Delete
    suspend fun delete(place: TrackedPlaceEntity)

    @Query("DELETE FROM tracked_places WHERE id = :id")
    suspend fun deleteById(id: Long)
}