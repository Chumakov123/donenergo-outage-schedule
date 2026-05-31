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
import androidx.compose.material.icons.filled.AddLocationAlt
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.component.SectionHeader
import com.chumakov123.outageschedule.presentation.component.SubHeader
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackedPlacesScreen(
    viewModel: TrackedPlacesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        Column(modifier = Modifier.fillMaxSize()) {
            // Кнопка развертывания формы
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Мои адреса",
                    icon = Icons.Default.Home,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                TextButton(onClick = viewModel::onToggleForm) {
                    Icon(if (state.isFormVisible) Icons.Default.AddLocationAlt else Icons.Default.AddHome, null)
                    Spacer(Modifier.size(4.dp))
                    Text(if (state.isFormVisible) "Свернуть" else "Добавить")
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    // Анимированная форма добавления
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
                                        label = { Text("Название (Дом, Работа...)") },
                                        leadingIcon = { Icon(Icons.Default.Place, null) },
                                        singleLine = true
                                    )

                                    OutlinedTextField(
                                        value = state.city,
                                        onValueChange = viewModel::onCityChange,
                                        modifier = Modifier.fillMaxWidth(),
                                        label = { Text("Населённый пункт") },
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
                                        label = { Text("Улица") },
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
                                        label = { Text("Номер дома") },
                                        leadingIcon = { Icon(Icons.Default.Home, null) },
                                        singleLine = true
                                    )

                                    if (state.error != null) {
                                        Text(
                                            state.error,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Button(
                                        onClick = viewModel::addPlace,
                                        enabled = state.city.isNotBlank() || state.street.isNotBlank(),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Сохранить адрес")
                                    }
                                }
                            }
                        }
                    }

                    if (state.places.isNotEmpty()) {
                        // Группировка: Активные
                        val activePlaces = state.places.filter { it.isEnabled }
                        if (activePlaces.isNotEmpty()) {
                            stickyHeader {
                                SubHeader("Активные уведомления", Icons.Default.NotificationsActive)
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

                        // Группировка: Выключенные
                        val inactivePlaces = state.places.filter { !it.isEnabled }
                        if (inactivePlaces.isNotEmpty()) {
                            stickyHeader {
                                SubHeader("Выключено", Icons.Default.NotificationsOff)
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
    ElevatedCard(
        modifier = modifier.fillMaxWidth().padding(horizontal = Spacing.Medium, vertical = 4.dp).animateContentSize(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(Spacing.Medium), verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = place.isEnabled, onCheckedChange = onToggle)
            Spacer(Modifier.size(Spacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                val fullAddress = buildString {
                    append(place.city)
                    if (place.city.isNotBlank() && place.street.isNotBlank()) append(", ")
                    append(place.street)
                    if (place.house.isNotBlank()) append(", ${place.house}")
                }
                if (place.title.isNotBlank() && place.title != fullAddress) {
                    Text(place.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(fullAddress, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text(fullAddress, style = MaterialTheme.typography.titleMedium)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) }
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
        Text("Список пуст", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Добавьте адреса, чтобы получать уведомления об отключениях.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.size(Spacing.Large))
        Button(onClick = onAddClick) { Text("Добавить адрес") }
    }
}

@Composable
private fun EditPlaceDialog(state: TrackedPlacesState, viewModel: TrackedPlacesViewModel) {
    AlertDialog(
        onDismissRequest = { viewModel.cancelEditing() },
        title = { Text("Редактирование") },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                OutlinedTextField(
                    value = state.editTitle,
                    onValueChange = viewModel::onEditTitleChange,
                    label = { Text("Название (Дом, Работа...)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Place, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.editCity,
                    onValueChange = viewModel::onEditCityChange,
                    label = { Text("Населённый пункт") },
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
                    label = { Text("Улица") },
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
                    label = { Text("Номер дома") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Home, null) },
                    singleLine = true
                )
                
                if (state.editError != null) {
                    Text(state.editError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = { 
            TextButton(
                onClick = viewModel::saveEditedPlace,
                enabled = state.editCity.isNotBlank() || state.editStreet.isNotBlank()
            ) { 
                Text("Сохранить", fontWeight = FontWeight.Bold) 
            } 
        },
        dismissButton = { TextButton(onClick = viewModel::cancelEditing) { Text("Отмена") } }
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
        title = { Text("Удаление адреса") },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        text = { Text("Вы уверены, что хотите удалить адрес \"${place.title}\"?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Удалить", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}