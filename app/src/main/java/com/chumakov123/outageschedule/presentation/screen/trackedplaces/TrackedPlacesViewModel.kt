package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.repository.AppSettingsRepository
import com.chumakov123.outageschedule.domain.repository.BranchLocalityRepository
import com.chumakov123.outageschedule.domain.repository.TrackedPlaceRepository
import com.chumakov123.outageschedule.domain.trackedplace.AddressNormalizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackedPlacesState(
    val places: List<TrackedPlace> = emptyList(),
    val title: String = "",
    val city: String = "",
    val street: String = "",
    val house: String = "",
    val error: String? = null,
    val citySuggestions: List<String> = emptyList(),
    val streetSuggestions: List<String> = emptyList(),
    val suggestionsLoading: Boolean = true,

    val editingPlaceId: Long? = null,
    val editTitle: String = "",
    val editCity: String = "",
    val editStreet: String = "",
    val editHouse: String = "",
    val editError: String? = null
)

class TrackedPlacesViewModel(
    private val repository: TrackedPlaceRepository,
    private val localityRepository: BranchLocalityRepository,
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TrackedPlacesState())
    val state: StateFlow<TrackedPlacesState> = _state

    private var selectedBranchUrls: Set<String> = emptySet()
    private var selectedLocalities: List<BranchLocality> = emptyList()

    init {
        observePlaces()
        observeSuggestionsSource()
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

    private fun observeSuggestionsSource() {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                settingsRepository.selectedBranchUrlsFlow,
                localityRepository.observeLocalities()
            ) { urls, localities ->
                urls to localities
            }.collectLatest { (urls, localities) ->
                selectedBranchUrls = urls
                selectedLocalities = localities.filter { it.branchUrl in urls }

                refreshSuggestions()
            }
        }
    }

    private fun refreshSuggestions() {
        val current = _state.value

        _state.value = current.copy(
            citySuggestions = buildCitySuggestions(current.city),
            streetSuggestions = buildStreetSuggestions(current.city, current.street),
            suggestionsLoading = false
        )
    }

    private fun buildCitySuggestions(query: String): List<String> {
        val q = AddressNormalizer.compact(query)

        return selectedLocalities
            .asSequence()
            .map { it.city.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { city ->
                q.isBlank() || AddressNormalizer.compact(city).contains(q)
            }
            .take(5)
            .toList()
    }

    private fun buildStreetSuggestions(cityQuery: String, streetQuery: String): List<String> {
        val cityQ = AddressNormalizer.compact(cityQuery)
        val streetQ = AddressNormalizer.compact(streetQuery)

        return selectedLocalities
            .asSequence()
            .filter { locality ->
                cityQ.isBlank() || AddressNormalizer.compact(locality.city).contains(cityQ)
            }
            .mapNotNull { it.street?.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .filter { street ->
                streetQ.isBlank() || AddressNormalizer.compact(street).contains(streetQ)
            }
            .take(5)
            .toList()
    }

    fun onTitleChange(value: String) {
        _state.value = _state.value.copy(title = value, error = null)
    }

    fun onCityChange(value: String) {
        _state.value = _state.value.copy(city = value, error = null)
        refreshSuggestions()
    }

    fun onStreetChange(value: String) {
        _state.value = _state.value.copy(street = value, error = null)
        refreshSuggestions()
    }

    fun onHouseChange(value: String) {
        _state.value = _state.value.copy(house = value, error = null)
    }

    fun onCitySuggestionClick(value: String) {
        _state.value = _state.value.copy(city = value, error = null)
        refreshSuggestions()
    }

    fun onStreetSuggestionClick(value: String) {
        _state.value = _state.value.copy(street = value, error = null)
        refreshSuggestions()
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

    fun startEditing(place: TrackedPlace) {
        _state.value = _state.value.copy(
            editingPlaceId = place.id,
            editTitle = place.title,
            editCity = place.city,
            editStreet = place.street,
            editHouse = place.house,
            editError = null
        )
    }

    fun cancelEditing() {
        _state.value = _state.value.copy(editingPlaceId = null, editError = null)
    }

    fun onEditTitleChange(value: String) {
        _state.update { it.copy(editTitle = value, editError = null) }
    }

    fun onEditCityChange(value: String) {
        _state.update { it.copy(editCity = value, editError = null) }
    }

    fun onEditStreetChange(value: String) {
        _state.update { it.copy(editStreet = value, editError = null) }
    }

    fun onEditHouseChange(value: String) {
        _state.update { it.copy(editHouse = value, editError = null) }
    }

    fun saveEditedPlace() {
        val current = _state.value
        val id = current.editingPlaceId ?: return
        val city = current.editCity.trim()
        val street = current.editStreet.trim()

        if (city.isBlank() && street.isBlank()) {
            _state.value = current.copy(editError = "Заполните хотя бы город или улицу")
            return
        }

        val title = current.editTitle.trim().ifBlank {
            listOf(city, street, current.editHouse.trim())
                .filter { it.isNotBlank() }
                .joinToString(", ")
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlace(
                TrackedPlace(
                    id = id,
                    title = title,
                    city = city,
                    street = street,
                    house = current.editHouse.trim(),
                    isEnabled = true
                )
            )
            _state.update { it.copy(editingPlaceId = null) }
        }
    }

    fun togglePlaceEnabled(place: TrackedPlace, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePlace(place.copy(isEnabled = enabled))
        }
    }
}