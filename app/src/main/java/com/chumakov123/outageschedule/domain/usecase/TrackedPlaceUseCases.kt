package com.chumakov123.outageschedule.domain.usecase

import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import kotlinx.coroutines.flow.Flow

class ObserveTrackedPlacesUseCase(
    private val repository: TrackedPlaceRepository
) {
    operator fun invoke(): Flow<List<TrackedPlace>> = repository.observePlaces()
}

class AddTrackedPlaceUseCase(
    private val repository: TrackedPlaceRepository
) {
    suspend operator fun invoke(place: TrackedPlace) = repository.addPlace(place)
}

class UpdateTrackedPlaceUseCase(
    private val repository: TrackedPlaceRepository
) {
    suspend operator fun invoke(place: TrackedPlace) = repository.updatePlace(place)
}

class DeleteTrackedPlaceUseCase(
    private val repository: TrackedPlaceRepository
) {
    suspend operator fun invoke(id: Long) = repository.deletePlace(id)
}