package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                SectionHeader(
                    title = "Настройки",
                    icon = Icons.Default.Settings,
                    modifier = Modifier.padding(top = Spacing.Small)
                )
            }

            // Раздел: Интервал обновления
            item {
                SubHeader(
                    title = "Интервал обновления",
                    icon = Icons.Default.Sync
                )
            }

            item {
                listOf(6, 12, 24).forEach { hours ->
                    val isSelected = state.syncIntervalHours == hours
                    SettingsRow(
                        onClick = { viewModel.setSyncInterval(hours) },
                        content = {
                            Column {
                                Text(
                                    text = "Каждые $hours часов",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (isSelected) "Выбрано" else "Нажмите для выбора",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setSyncInterval(hours) }
                            )
                        }
                    )
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = Spacing.Medium)) }

            // Раздел: Уведомления
            item {
                SubHeader(
                    title = "Уведомления",
                    icon = Icons.Default.NotificationsActive
                )
            }

            item {
                Text(
                    text = "За какое время предупреждать об отключении:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.Small)
                )
            }

            item {
                listOf(12, 24, 48).forEach { hours ->
                    val isChecked = state.notificationLeadHours.contains(hours)
                    SettingsRow(
                        onClick = { viewModel.toggleNotificationLeadHour(hours) },
                        content = {
                            Text(
                                text = "За $hours часов",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        trailingContent = {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleNotificationLeadHour(hours) }
                            )
                        }
                    )
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = Spacing.Medium)) }

            // Раздел: Филиалы
            item {
                SubHeader(
                    title = "Филиалы",
                    icon = Icons.Default.Business
                )
            }

            item {
                Text(
                    text = "Выберите филиалы для отслеживания отключений:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.Small)
                )
            }

            items(state.branches) { branch ->
                val isSelected = state.selectedUrls.contains(branch.url)
                val suggestions = state.citySuggestionsByBranchUrl[branch.url].orEmpty()

                SettingsRow(
                    onClick = { viewModel.toggle(branch) },
                    content = {
                        Column {
                            Text(
                                text = branch.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (suggestions.isNotEmpty()) {
                                Text(
                                    text = suggestions.joinToString(", "),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    trailingContent = {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { viewModel.toggle(branch) }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.padding(horizontal = Spacing.Medium)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(Modifier.size(Spacing.Small))
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(horizontal = Spacing.Medium, vertical = 8.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(Spacing.Small))
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun SettingsRow(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            content()
        }
        trailingContent()
    }
}