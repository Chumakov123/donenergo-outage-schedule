package com.chumakov123.outageschedule.domain.model

data class PreparedNotification(
    val outage: Outage,
    val leadHours: Int,
    val notificationKey: String
)