package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.OutageEntity
import com.chumakov123.outageschedule.domain.model.Outage

fun OutageEntity.toDomain(): Outage {
    return Outage(
        city = city,
        address = address,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        reason = reason,
        branchName = branchName
    )
}

fun Outage.toEntity(
    branchUrl: String,
    branchName: String,
    fetchedAt: Long
): OutageEntity {
    return OutageEntity(
        branchUrl = branchUrl,
        branchName = branchName,
        city = city,
        address = address,
        startDate = startDate,
        endDate = endDate,
        startTime = startTime,
        endTime = endTime,
        reason = reason,
        fetchedAt = fetchedAt
    )
}