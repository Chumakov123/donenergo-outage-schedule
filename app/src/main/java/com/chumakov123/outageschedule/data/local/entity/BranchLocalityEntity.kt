package com.chumakov123.outageschedule.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "branch_localities")
data class BranchLocalityEntity(
    @PrimaryKey
    val id: String,
    val branchUrl: String,
    val branchName: String,
    val city: String,
    val street: String?,
    val normalizedCity: String,
    val normalizedStreet: String?,
    val lastSeenAt: Long
)