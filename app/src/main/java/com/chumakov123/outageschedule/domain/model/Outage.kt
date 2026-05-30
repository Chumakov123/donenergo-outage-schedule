package com.chumakov123.outageschedule.domain.model

data class Outage(
    val city: String,
    val address: String,
    val startDate: String?,
    val endDate: String?,
    val startTime: String?,
    val endTime: String?,
    val reason: String?,
    val note: String? = null,
    val branchName: String = "",
    val branchUrl: String = "",
    val status: OutageStatus = OutageStatus.UPCOMING
) {
    fun buildId(): String = listOf(
        branchUrl, city, address,
        startDate.orEmpty(), endDate.orEmpty(),
        startTime.orEmpty(), endTime.orEmpty()
    ).joinToString("|")
}