package com.chumakov123.outageschedule.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_places")
data class TrackedPlaceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val city: String,
    val street: String,
    val house: String,
    val isEnabled: Boolean = true
)