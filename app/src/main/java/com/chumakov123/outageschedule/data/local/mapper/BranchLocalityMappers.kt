package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.trackedplace.AddressNormalizer

fun BranchLocalityEntity.toDomain(): BranchLocality {
    return BranchLocality(
        branchUrl = branchUrl,
        branchName = branchName,
        city = city
    )
}

fun BranchLocality.toEntity(lastSeenAt: Long): BranchLocalityEntity {
    val normalized = AddressNormalizer.compact(city)

    return BranchLocalityEntity(
        id = "$branchUrl|$normalized",
        branchUrl = branchUrl,
        branchName = branchName,
        city = city.trim(),
        normalizedCity = normalized,
        lastSeenAt = lastSeenAt
    )
}