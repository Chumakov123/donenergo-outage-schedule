package com.chumakov123.outageschedule.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outages")
data class OutageEntity(
    @PrimaryKey
    val id: String,
    val branchUrl: String,
    val branchName: String,
    val city: String,
    val address: String,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val reason: String?,
    val status: String,
    val fetchedAt: Long
)