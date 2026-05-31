package com.chumakov123.outageschedule.presentation.screen.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.component.SectionHeader
import com.chumakov123.outageschedule.presentation.component.SubHeader
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
                                    text = "Каждые $hours ${hours.hoursLabel()}",
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    val onPermissionClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }

                    SettingsRow(
                        onClick = onPermissionClick,
                        content = {
                            Column {
                                Text(
                                    text = "Разрешить уведомления",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (state.isNotificationPermissionGranted) "Разрешено" else "Требуется разрешение",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (state.isNotificationPermissionGranted)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        trailingContent = {
                            Checkbox(
                                checked = state.isNotificationPermissionGranted,
                                onCheckedChange = { onPermissionClick() }
                            )
                        }
                    )
                }
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

private fun Int.hoursLabel(): String {
    val mod10 = this % 10
    val mod100 = this % 100

    return when {
        mod100 in 11..14 -> "часов"
        mod10 == 1 -> "час"
        mod10 in 2..4 -> "часа"
        else -> "часов"
    }
}