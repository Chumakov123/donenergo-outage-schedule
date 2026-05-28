package com.chumakov123.outageschedule.data.local.mapper

import com.chumakov123.outageschedule.data.local.entity.TrackedPlaceEntity
import com.chumakov123.outageschedule.domain.model.TrackedPlace

fun TrackedPlaceEntity.toDomain(): TrackedPlace {
    return TrackedPlace(
        id = id,
        title = title,
        city = city,
        street = street,
        house = house
    )
}

fun TrackedPlace.toEntity(): TrackedPlaceEntity {
    return TrackedPlaceEntity(
        id = id,
        title = title.trim(),
        city = city.trim(),
        street = street.trim(),
        house = house.trim()
    )
}