package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            item {
                Text(
                    text = "Интервал обновления",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                listOf(6, 12, 24).forEach { hours ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setSyncInterval(hours) }
                            .padding(vertical = Spacing.Small)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${hours} часов",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = if (state.syncIntervalHours == hours) {
                                    "Выбрано"
                                } else {
                                    "Нажмите, чтобы выбрать"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Филиалы",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            item {
                Text(
                    text = "Минимум один филиал должен быть выбран",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(state.branches) { branch ->
                val selected = state.selectedUrls.contains(branch.url)
                val suggestions = state.citySuggestionsByBranchUrl[branch.url].orEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggle(branch) }
                        .padding(vertical = Spacing.Small)
                ) {
                    Text(
                        text = if (selected) "✓ ${branch.name}" else branch.name,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = if (suggestions.isEmpty()) {
                            "Подсказки загружаются..."
                        } else {
                            suggestions.joinToString(" • ")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}