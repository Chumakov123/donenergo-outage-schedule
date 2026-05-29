package com.chumakov123.outageschedule.presentation.screen.trackedplaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
            Text(
                text = "Отслеживаемые места",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Можно оставить только первое или только второе поле",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                label = { Text("Населённый пункт или район") }
            )

            OutlinedTextField(
                value = state.street,
                onValueChange = viewModel::onStreetChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Улица, СНТ, объект") }
            )

            OutlinedTextField(
                value = state.house,
                onValueChange = viewModel::onHouseChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Дом, литера, дробь") }
            )

            if (state.error != null) {
                Text(state.error)
            }

            Button(onClick = viewModel::addPlace) {
                Text("Добавить")
            }

            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

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
                            Text(
                                "${place.city}${if (place.city.isNotBlank() && place.street.isNotBlank()) ", " else ""}${place.street}${if (place.house.isNotBlank()) ", ${place.house}" else ""}"
                            )
                        }

                        Button(onClick = { viewModel.deletePlace(place) }) {
                            Text("Удалить")
                        }
                    }
                }
            }
        }
    }
}