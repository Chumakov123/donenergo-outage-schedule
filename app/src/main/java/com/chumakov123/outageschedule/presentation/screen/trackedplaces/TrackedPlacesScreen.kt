package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
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
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrackedPlacesScreen(
    viewModel: TrackedPlacesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {

            item {
                Text(
                    text = "Отслеживаемые места",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = "Подсказки берутся из уже загруженных данных по выбранным филиалам.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedTextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Название") }
                )
            }

            item {
                OutlinedTextField(
                    value = state.city,
                    onValueChange = viewModel::onCityChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Населённый пункт или район") }
                )
            }

            items(state.citySuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onCitySuggestionClick(
                                suggestion
                            )
                        }
                        .padding(vertical = Spacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = state.street,
                    onValueChange = viewModel::onStreetChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Улица, СНТ, объект") }
                )
            }

            items(state.streetSuggestions) { suggestion ->
                Text(
                    text = suggestion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.onStreetSuggestionClick(
                                suggestion
                            )
                        }
                        .padding(vertical = Spacing.Small),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                OutlinedTextField(
                    value = state.house,
                    onValueChange = viewModel::onHouseChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Дом, литера, дробь") }
                )
            }

            if (state.error != null) {
                item {
                    Text(state.error)
                }
            }

            item {
                Button(
                    onClick = viewModel::addPlace,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Добавить")
                }
            }

            item {
                HorizontalDivider(
                    Modifier,
                    DividerDefaults.Thickness,
                    DividerDefaults.color
                )
            }

            items(state.places, key = { it.id }) { place ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = place.isEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.togglePlaceEnabled(place, enabled)
                        },
                        modifier = Modifier.padding(end = Spacing.Small)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = place.title,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = buildString {
                                append(place.city)
                                if (place.city.isNotBlank() && place.street.isNotBlank()) append(", ")
                                append(place.street)
                                if (place.house.isNotBlank()) append(", ${place.house}")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = { viewModel.startEditing(place) }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Редактировать"
                        )
                    }
                    IconButton(onClick = { viewModel.deletePlace(place) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Удалить"
                        )
                    }
                }
            }
        }
    }
    if (state.editingPlaceId != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEditing() },
            title = { Text("Редактировать место") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                    OutlinedTextField(
                        value = state.editTitle,
                        onValueChange = viewModel::onEditTitleChange,
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.editCity,
                        onValueChange = viewModel::onEditCityChange,
                        label = { Text("Населённый пункт") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.editStreet,
                        onValueChange = viewModel::onEditStreetChange,
                        label = { Text("Улица / СНТ / объект") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.editHouse,
                        onValueChange = viewModel::onEditHouseChange,
                        label = { Text("Дом, литера, дробь") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.editError != null) {
                        Text(
                            text = state.editError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.saveEditedPlace() }) {
                    Text("Сохранить")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEditing() }) {
                    Text("Отмена")
                }
            }
        )
    }
}