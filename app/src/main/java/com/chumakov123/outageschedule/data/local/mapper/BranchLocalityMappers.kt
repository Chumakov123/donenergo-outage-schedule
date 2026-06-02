package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.BranchLocalityEntity
import com.chumakov123.outageschedule.domain.model.BranchLocality

fun BranchLocalityEntity.toDomain(): BranchLocality {
    return BranchLocality(
        branchUrl = branchUrl,
        branchName = branchName,
        city = city,
        street = street
    )
}