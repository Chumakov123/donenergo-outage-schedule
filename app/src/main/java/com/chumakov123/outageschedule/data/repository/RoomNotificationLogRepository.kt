package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.local.dao.SentNotificationDao
import com.chumakov123.outageschedule.data.local.entity.SentNotificationEntity
import com.chumakov123.outageschedule.domain.repository.NotificationLogRepository

class RoomNotificationLogRepository(
    private val dao: SentNotificationDao
) : NotificationLogRepository {

    override suspend fun wasSent(key: String): Boolean {
        return dao.countByKey(key) > 0
    }

    override suspend fun markSent(
        key: String,
        outageId: String,
        leadHours: Int,
        sentAt: Long
    ) {
        dao.insert(
            SentNotificationEntity(
                notificationKey = key,
                outageId = outageId,
                leadHours = leadHours,
                sentAt = sentAt
            )
        )
    }
}