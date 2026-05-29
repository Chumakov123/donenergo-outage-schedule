package com.chumakov123.outageschedule.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chumakov123.outageschedule.data.local.entity.SentNotificationEntity

@Dao
interface SentNotificationDao {

    @Query("SELECT COUNT(*) FROM sent_notifications WHERE notificationKey = :key")
    suspend fun countByKey(key: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SentNotificationEntity)
}