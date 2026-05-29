package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.trackedplace.AddressNormalizer

fun BranchLocalityEntity.toDomain(): BranchLocality {
    return BranchLocality(
        branchUrl = branchUrl,
        branchName = branchName,
        city = city,
        street = street
    )
}

fun BranchLocality.toEntity(lastSeenAt: Long): BranchLocalityEntity {
    val normalizedCity = AddressNormalizer.compact(city)
    val normalizedStreet = street?.let { AddressNormalizer.compact(it) }

    return BranchLocalityEntity(
        id = buildString {
            append(branchUrl)
            append("|")
            append(normalizedCity)
            append("|")
            append(normalizedStreet.orEmpty())
        },
        branchUrl = branchUrl,
        branchName = branchName,
        city = city.trim(),
        street = street?.trim()?.takeIf { it.isNotBlank() },
        normalizedCity = normalizedCity,
        normalizedStreet = normalizedStreet,
        lastSeenAt = lastSeenAt
    )
}