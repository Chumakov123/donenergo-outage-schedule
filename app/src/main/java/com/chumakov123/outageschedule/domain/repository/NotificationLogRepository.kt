package com.chumakov123.outageschedule.domain.repository

interface NotificationLogRepository {
    suspend fun wasSent(key: String): Boolean
    suspend fun markSent(
        key: String,
        outageId: String,
        leadHours: Int,
        sentAt: Long = System.currentTimeMillis()
    )
}