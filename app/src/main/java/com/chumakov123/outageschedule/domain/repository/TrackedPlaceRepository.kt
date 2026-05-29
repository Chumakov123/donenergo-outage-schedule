package com.chumakov123.outageschedule.domain.repository

import com.chumakov123.outageschedule.domain.model.TrackedPlace
import kotlinx.coroutines.flow.Flow

interface TrackedPlaceRepository {
    fun observePlaces(): Flow<List<TrackedPlace>>
    suspend fun addPlace(place: TrackedPlace): Long
    suspend fun updatePlace(place: TrackedPlace)   // новый метод
    suspend fun deletePlace(id: Long)
}