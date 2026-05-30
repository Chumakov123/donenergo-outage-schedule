package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.OutageEntity
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.OutageStatus

private fun buildOutageId(
    branchUrl: String,
    city: String,
    address: String,
    startDate: String,
    endDate: String,
    startTime: String?,
    endTime: String?
): String {
    return listOf(
        branchUrl,
        city,
        address,
        startDate,
        endDate,
        startTime.orEmpty(),
        endTime.orEmpty()
    ).joinToString("|")
}

fun OutageEntity.toDomain(): Outage {
    return Outage(
        city = city,
        address = address,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        reason = reason,
        note = note,
        branchName = branchName,
        branchUrl = branchUrl,
        status = runCatching { OutageStatus.valueOf(status) }.getOrDefault(OutageStatus.UPCOMING)
    )
}

fun Outage.toEntity(
    branchUrl: String,
    branchName: String,
    fetchedAt: Long
): OutageEntity {
    return OutageEntity(
        id = buildOutageId(
            branchUrl = branchUrl,
            city = city,
            address = address,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime
        ),
        branchUrl = branchUrl,
        branchName = branchName,
        city = city,
        address = address,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        reason = reason,
        note = note,
        status = status.name,
        fetchedAt = fetchedAt
    )
}

internal fun buildOutageId(outage: Outage): String {
    return listOf(
        outage.branchUrl,
        outage.city,
        outage.address,
        outage.startDate,
        outage.endDate,
        outage.startTime.orEmpty(),
        outage.endTime.orEmpty()
    ).joinToString("|")
}