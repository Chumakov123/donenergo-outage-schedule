package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Text("Отслеживаемые места")

            OutlinedTextField(
                value = state.title,
                onValueChange = viewModel::onTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Название") }
            )

            OutlinedTextField(
                value = state.city,
                onValueChange = viewModel::onCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Город") }
            )

            OutlinedTextField(
                value = state.street,
                onValueChange = viewModel::onStreetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Улица") }
            )

            OutlinedTextField(
                value = state.house,
                onValueChange = viewModel::onHouseChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Дом") }
            )

            if (state.error != null) {
                Text(state.error)
            }

            Button(
                onClick = viewModel::addPlace
            ) {
                Text("Добавить")
            }

            Divider()

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                items(state.places) { place ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.Small),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(place.title)
                            Text("${place.city}, ${place.street}${if (place.house.isNotBlank()) ", ${place.house}" else ""}")
                        }

                        Button(
                            onClick = { viewModel.deletePlace(place) }
                        ) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}