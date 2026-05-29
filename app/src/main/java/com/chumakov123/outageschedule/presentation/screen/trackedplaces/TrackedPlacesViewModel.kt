package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class TrackedPlacesState(
    val places: List<TrackedPlace> = emptyList(),
    val title: String = "",
    val city: String = "",
    val street: String = "",
    val house: String = "",
    val error: String? = null
)

class TrackedPlacesViewModel(
    private val repository: TrackedPlaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrackedPlacesState())
    val state: StateFlow<TrackedPlacesState> = _state

    init {
        observePlaces()
    }

    private fun observePlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.observePlaces().collectLatest { places ->
                _state.value = _state.value.copy(
                    places = places,
                    error = null
                )
            }
        }
    }

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value, error = null)
    }

    fun onCityChange(value: String) {
        _state.value = _state.value.copy(city = value, error = null)
    }

    fun onStreetChange(value: String) {
        _state.value = _state.value.copy(street = value, error = null)
    }

    fun onHouseChange(value: String) {
        _state.value = _state.value.copy(house = value, error = null)
    }

    fun addPlace() {
        val current = _state.value

        val city = current.city.trim()
        val street = current.street.trim()

        if (city.isBlank() && street.isBlank()) {
            _state.value = current.copy(
                error = "Нужно заполнить хотя бы один из первых двух полей"
            )
            return
        }

        val title = current.title.trim().ifBlank {
            listOf(city, street, current.house.trim())
                .filter { it.isNotBlank() }
                .joinToString(", ")
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.addPlace(
                TrackedPlace(
                    title = title,
                    city = city,
                    street = street,
                    house = current.house.trim()
                )
            )

            _state.value = TrackedPlacesState()
        }
    }

    fun deletePlace(place: TrackedPlace) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deletePlace(place.id)
        }
    }
}