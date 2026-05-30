package com.chumakov123.outageschedule.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chumakov123.outageschedule.data.local.dao.BranchLocalityDao
import com.chumakov123.outageschedule.data.local.dao.OutageDao
import com.chumakov123.outageschedule.data.local.dao.SentNotificationDao
import com.chumakov123.outageschedule.data.local.dao.TrackedPlaceDao
import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import com.chumakov123.outageschedule.data.local.entity.OutageEntity
import com.chumakov123.outageschedule.data.local.entity.SentNotificationEntity
import com.chumakov123.outageschedule.data.local.entity.TrackedPlaceEntity

@Database(
    entities = [
        OutageEntity::class,
        TrackedPlaceEntity::class,
        BranchLocalityEntity::class,
        SentNotificationEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outageDao(): OutageDao
    abstract fun trackedPlaceDao(): TrackedPlaceDao
    abstract fun branchLocalityDao(): BranchLocalityDao
    abstract fun sentNotificationDao(): SentNotificationDao
}