package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.R
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.component.SectionHeader
import com.chumakov123.outageschedule.presentation.component.SubHeader
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackedPlacesScreen(
    openFormOnEnter: Boolean = false,
    viewModel: TrackedPlacesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(openFormOnEnter) {
        if (openFormOnEnter) {
            viewModel.openForm()
        }
    }

    ScreenContainer {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = stringResource(R.string.tracked_places_title),
                    icon = Icons.Default.Home,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                IconButton(onClick = viewModel::onToggleForm) {
                    Icon(
                        imageVector = if (state.isFormVisible) Icons.Default.Close else Icons.Default.AddHome,
                        contentDescription = stringResource(if (state.isFormVisible) R.string.outages_content_desc_collapse else R.string.tracked_places_content_desc_add),
                        tint = if (state.isFormVisible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    item {
                        this@Column.AnimatedVisibility(visible = state.isFormVisible) {
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(Spacing.Medium),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                                ) {
                                    OutlinedTextField(
                                        value = state.title,
                                        onValueChange = viewModel::onTitleChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(stringResource(R.string.tracked_places_label_name)) },
                                        leadingIcon = { Icon(Icons.Default.Place, null) },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = state.city,
                                        onValueChange = viewModel::onCityChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(stringResource(R.string.tracked_places_label_city)) },
                                        leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                                        singleLine = true
                                    )
                                    if (state.citySuggestions.isNotEmpty()) {
                                        SuggestionsRow(state.citySuggestions, viewModel::onCitySuggestionClick)
                                    }

                                    OutlinedTextField(
                                        value = state.street,
                                        onValueChange = viewModel::onStreetChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(stringResource(R.string.tracked_places_label_street)) },
                                        leadingIcon = { Icon(Icons.Default.Map, null) },
                                        singleLine = true
                                    )
                                    if (state.streetSuggestions.isNotEmpty()) {
                                        SuggestionsRow(state.streetSuggestions, viewModel::onStreetSuggestionClick)
                                    }

                                    OutlinedTextField(
                                        value = state.house,
                                        onValueChange = viewModel::onHouseChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text(stringResource(R.string.tracked_places_label_house)) },
                                        leadingIcon = { Icon(Icons.Default.Home, null) },
                                        singleLine = true
                                    )

                                    if (state.error != null) {
                                        Text(
                                            state.error.asString(),
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Button(
                                        onClick = viewModel::addPlace,
                                        enabled = state.city.isNotBlank() || state.street.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(stringResource(R.string.tracked_places_button_save))
                                    }
                                }
                            }
                        }
                    }

                    if (state.places.isNotEmpty()) {
                        val activePlaces = state.places.filter { it.isEnabled }
                        if (activePlaces.isNotEmpty()) {
                            stickyHeader {
                                SubHeader(stringResource(R.string.tracked_places_header_active), Icons.Default.NotificationsActive)
                            }
                            items(activePlaces, key = { it.id }) { place ->
                                TrackedPlaceItem(
                                    modifier = Modifier.animateItem(),
                                    place = place,
                                    onToggle = { viewModel.togglePlaceEnabled(place, it) },
                                    onEdit = { viewModel.startEditing(place) },
                                    onDelete = { viewModel.onDeleteClick(place) }
                                )
                            }
                        }

                        val inactivePlaces = state.places.filter { !it.isEnabled }
                        if (inactivePlaces.isNotEmpty()) {
                            stickyHeader {
                                SubHeader(stringResource(R.string.tracked_places_header_disabled), Icons.Default.NotificationsOff)
                            }
                            items(inactivePlaces, key = { it.id }) { place ->
                                TrackedPlaceItem(
                                    modifier = Modifier.animateItem(),
                                    place = place,
                                    onToggle = { viewModel.togglePlaceEnabled(place, it) },
                                    onEdit = { viewModel.startEditing(place) },
                                    onDelete = { viewModel.onDeleteClick(place) }
                                )
                            }
                        }

                        item { Spacer(Modifier.size(Spacing.Large)) }
                    }
                }

                if (state.places.isEmpty() && !state.isFormVisible) {
                    EmptyPlacesState(
                        onAddClick = viewModel::onToggleForm,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }

    if (state.editingPlaceId != null) {
        EditPlaceDialog(state, viewModel)
    }

    if (state.deletingPlace != null) {
        DeleteConfirmationDialog(
            place = state.deletingPlace,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete
        )
    }
}

@Composable
private fun SuggestionsRow(suggestions: List<String>, onClick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.Small), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        items(suggestions) { suggestion ->
            AssistChip(onClick = { onClick(suggestion) }, label = { Text(suggestion) })
        }
    }
}

@Composable
private fun TrackedPlaceItem(
    modifier: Modifier = Modifier,
    place: TrackedPlace,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val fullAddress = buildString {
        append(place.city)
        if (place.city.isNotBlank() && place.street.isNotBlank()) append(", ")
        append(place.street)
        if (place.house.isNotBlank()) append(", ${place.house}")
    }

    val hasCustomTitle = place.title.isNotBlank() && place.title != fullAddress
    val displayTitle = if (hasCustomTitle) place.title else fullAddress
    val displaySubtitle = if (hasCustomTitle) fullAddress else null

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(start = Spacing.Medium, top = Spacing.Medium, end = Spacing.Small, bottom = Spacing.Small)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (displaySubtitle != null) {
                        Text(
                            text = displaySubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Switch(
                    checked = place.isEnabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.padding(start = Spacing.Small)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.tracked_places_content_desc_edit),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = stringResource(R.string.tracked_places_content_desc_delete),
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPlacesState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        Spacer(Modifier.size(Spacing.Medium))
        Text(stringResource(R.string.tracked_places_empty_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.tracked_places_empty_description), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(Spacing.Large))
        Button(onClick = onAddClick) { Text(stringResource(R.string.outages_action_add_address)) }
    }
}

@Composable
private fun EditPlaceDialog(state: TrackedPlacesState, viewModel: TrackedPlacesViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.cancelEditing() },
        title = { Text(stringResource(R.string.tracked_places_content_desc_edit)) },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                OutlinedTextField(
                    value = state.editTitle,
                    onValueChange = viewModel::onEditTitleChange,
                    label = { Text(stringResource(R.string.tracked_places_label_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.editCity,
                    onValueChange = viewModel::onEditCityChange,
                    label = { Text(stringResource(R.string.tracked_places_label_city)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                    singleLine = true
                )
                if (state.citySuggestions.isNotEmpty()) {
                    SuggestionsRow(state.citySuggestions, viewModel::onCitySuggestionClick)
                }

                OutlinedTextField(
                    value = state.editStreet,
                    onValueChange = viewModel::onEditStreetChange,
                    label = { Text(stringResource(R.string.tracked_places_label_street)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Map, null) },
                    singleLine = true
                )
                if (state.streetSuggestions.isNotEmpty()) {
                    SuggestionsRow(state.streetSuggestions, viewModel::onStreetSuggestionClick)
                }

                OutlinedTextField(
                    value = state.editHouse,
                    onValueChange = viewModel::onEditHouseChange,
                    label = { Text(stringResource(R.string.tracked_places_label_house)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    singleLine = true
                )
                
                if (state.editError != null) {
                    Text(state.editError.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = { 
            TextButton(
                onClick = viewModel::saveEditedPlace,
                enabled = state.editCity.isNotBlank() || state.editStreet.isNotBlank()
            ) { 
                Text(stringResource(R.string.tracked_places_button_save), fontWeight = FontWeight.Bold) 
            } 
        },
        dismissButton = { TextButton(onClick = viewModel::cancelEditing) { Text(stringResource(R.string.common_cancel)) } }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    place: TrackedPlace,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.tracked_places_dialog_delete_title)) },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        text = { Text(stringResource(R.string.tracked_places_dialog_delete_message, place.title)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}