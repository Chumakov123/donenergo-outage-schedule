package com.chumakov123.outageschedule.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sent_notifications")
data class SentNotificationEntity(
    @PrimaryKey
    val notificationKey: String,
    val outageId: String,
    val leadHours: Int,
    val sentAt: Long
)