package com.chumakov123.outageschedule.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chumakov123.outageschedule.data.local.dao.OutageDao
import com.chumakov123.outageschedule.data.local.entity.OutageEntity

@Database(
    entities = [OutageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outageDao(): OutageDao
}