package com.chumakov123.outageschedule.data.repository

import com.chumakov123.outageschedule.data.local.dao.TrackedPlaceDao
import com.chumakov123.outageschedule.data.local.mapper.toDomain
import com.chumakov123.outageschedule.data.local.mapper.toEntity
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTrackedPlaceRepository(
    private val dao: TrackedPlaceDao
) : TrackedPlaceRepository {

    override fun observePlaces(): Flow<List<TrackedPlace>> {
        return dao.observePlaces().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun addPlace(place: TrackedPlace): Long {
        return dao.insert(place.toEntity())
    }

    override suspend fun updatePlace(place: TrackedPlace) {
        dao.update(place.toEntity())
    }

    override suspend fun deletePlace(id: Long) {
        dao.deleteById(id)
    }
}