package com.chumakov123.outageschedule.domain.model

data class Outage(
    val city: String,
    val address: String,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val reason: String?
)