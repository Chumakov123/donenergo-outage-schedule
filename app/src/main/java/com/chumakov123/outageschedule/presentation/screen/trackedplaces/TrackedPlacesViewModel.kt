package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chumakov123.outageschedule.R
import com.chumakov123.outageschedule.domain.model.BranchLocality
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.usecase.*
import com.chumakov123.outageschedule.presentation.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TrackedPlacesState(
    val places: List<TrackedPlace> = emptyList(),
    val isFormVisible: Boolean = false,
    val title: String = "",
    val city: String = "",
    val street: String = "",
    val house: String = "",
    val error: UiText? = null,
    val citySuggestions: List<String> = emptyList(),
    val streetSuggestions: List<String> = emptyList(),
    val suggestionsLoading: Boolean = true,

    val editingPlaceId: Long? = null,
    val editTitle: String = "",
    val editCity: String = "",
    val editStreet: String = "",
    val editHouse: String = "",
    val editError: UiText? = null,
    
    val deletingPlace: TrackedPlace? = null
)

class TrackedPlacesViewModel(
    private val observeTrackedPlacesUseCase: ObserveTrackedPlacesUseCase,
    private val addTrackedPlaceUseCase: AddTrackedPlaceUseCase,
    private val updateTrackedPlaceUseCase: UpdateTrackedPlaceUseCase,
    private val deleteTrackedPlaceUseCase: DeleteTrackedPlaceUseCase,
    private val observeSettingsUseCase: ObserveSettingsUseCase,
    private val suggestionsUseCase: GetLocationSuggestionsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TrackedPlacesState())
    val state: StateFlow<TrackedPlacesState> = _state

    private var selectedBranchUrls: Set<String> = emptySet()
    private var allLocalities: List<BranchLocality> = emptyList()

    init {
        observePlaces()
        observeSuggestionsSource()
    }

    private fun observePlaces() {
        viewModelScope.launch(Dispatchers.IO) {
            observeTrackedPlacesUseCase().collectLatest { places ->
                _state.update { it.copy(places = places, error = null) }
            }
        }
    }

    private fun observeSuggestionsSource() {
        viewModelScope.launch(Dispatchers.IO) {
            observeSettingsUseCase.selectedBranchUrls.collectLatest { urls ->
                selectedBranchUrls = urls
                refreshSuggestions()
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            suggestionsUseCase.observeLocalities().collectLatest { localities ->
                allLocalities = localities
                refreshSuggestions()
            }
        }
    }

    private fun refreshSuggestions() {
        _state.update { current ->
            val isEditing = current.editingPlaceId != null
            val cityQuery = if (isEditing) current.editCity else current.city
            val streetQuery = if (isEditing) current.editStreet else current.street

            current.copy(
                citySuggestions = suggestionsUseCase.getCitySuggestions(
                    allLocalities, selectedBranchUrls, cityQuery
                ),
                streetSuggestions = suggestionsUseCase.getStreetSuggestions(
                    allLocalities, selectedBranchUrls, cityQuery, streetQuery
                ),
                suggestionsLoading = false
            )
        }
    }

    fun openForm() {
        _state.update { current ->
            if (current.isFormVisible) current
            else current.copy(isFormVisible = true, error = null)
        }
        refreshSuggestions()
    }

    fun onToggleForm() {
        _state.update { it.copy(isFormVisible = !it.isFormVisible, error = null) }
        if (_state.value.isFormVisible) {
            refreshSuggestions()
        }
    }

    fun onTitleChange(value: String) {
        _state.update { it.copy(title = value, error = null) }
    }

    fun onCityChange(value: String) {
        _state.update { it.copy(city = value, error = null) }
        refreshSuggestions()
    }

    fun onStreetChange(value: String) {
        _state.update { it.copy(street = value, error = null) }
        refreshSuggestions()
    }

    fun onHouseChange(value: String) {
        _state.update { it.copy(house = value, error = null) }
    }

    fun onCitySuggestionClick(value: String) {
        val isEditing = _state.value.editingPlaceId != null
        if (isEditing) {
            _state.update { it.copy(editCity = value, editError = null) }
        } else {
            _state.update { it.copy(city = value, error = null) }
        }
        refreshSuggestions()
    }

    fun onStreetSuggestionClick(value: String) {
        val isEditing = _state.value.editingPlaceId != null
        if (isEditing) {
            _state.update { it.copy(editStreet = value, editError = null) }
        } else {
            _state.update { it.copy(street = value, error = null) }
        }
        refreshSuggestions()
    }

    fun addPlace() {
        val current = _state.value

        val city = current.city.trim()
        val street = current.street.trim()

        if (city.isBlank() && street.isBlank()) {
            _state.update { it.copy(error = UiText.StringResource(R.string.tracked_places_error_empty_fields)) }
            return
        }

        val title = current.title.trim().ifBlank {
            listOf(city, street, current.house.trim())
                .filter { it.isNotBlank() }
                .joinToString(", ")
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                addTrackedPlaceUseCase(
                    TrackedPlace(
                        title = title,
                        city = city,
                        street = street,
                        house = current.house.trim()
                    )
                )

                _state.update { it.copy(
                    isFormVisible = false,
                    title = "",
                    city = "",
                    street = "",
                    house = "",
                    error = null
                ) }
                refreshSuggestions()
            } catch (e: Exception) {
                _state.update { it.copy(error = UiText.StringResource(R.string.tracked_places_error_save_failed, e.message ?: "")) }
            }
        }
    }

    fun onDeleteClick(place: TrackedPlace) {
        _state.update { it.copy(deletingPlace = place) }
    }

    fun cancelDelete() {
        _state.update { it.copy(deletingPlace = null) }
    }

    fun confirmDelete() {
        val place = _state.value.deletingPlace ?: return
        viewModelScope.launch(Dispatchers.IO) {
            deleteTrackedPlaceUseCase(place.id)
            _state.update { it.copy(deletingPlace = null) }
        }
    }

    fun startEditing(place: TrackedPlace) {
        _state.update { it.copy(
            editingPlaceId = place.id,
            editTitle = place.title,
            editCity = place.city,
            editStreet = place.street,
            editHouse = place.house,
            editError = null
        ) }
        refreshSuggestions()
    }

    fun cancelEditing() {
        _state.update { it.copy(editingPlaceId = null, editError = null) }
        refreshSuggestions()
    }

    fun onEditTitleChange(value: String) {
        _state.update { it.copy(editTitle = value, editError = null) }
    }

    fun onEditCityChange(value: String) {
        _state.update { it.copy(editCity = value, editError = null) }
        refreshSuggestions()
    }

    fun onEditStreetChange(value: String) {
        _state.update { it.copy(editStreet = value, editError = null) }
        refreshSuggestions()
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
            _state.update { it.copy(editError = UiText.StringResource(R.string.tracked_places_error_empty_fields)) }
            return
        }

        val title = current.editTitle.trim().ifBlank {
            listOf(city, street, current.editHouse.trim())
                .filter { it.isNotBlank() }
                .joinToString(", ")
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                updateTrackedPlaceUseCase(
                    TrackedPlace(
                        id = id,
                        title = title,
                        city = city,
                        street = street,
                        house = current.editHouse.trim(),
                        isEnabled = true
                    )
                )
                _state.update { it.copy(editingPlaceId = null, editError = null) }
                refreshSuggestions()
            } catch (e: Exception) {
                _state.update { it.copy(editError = UiText.StringResource(R.string.tracked_places_error_save_failed, e.message ?: "")) }
            }
        }
    }

    fun togglePlaceEnabled(place: TrackedPlace, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            updateTrackedPlaceUseCase(place.copy(isEnabled = enabled))
        }
    }
}