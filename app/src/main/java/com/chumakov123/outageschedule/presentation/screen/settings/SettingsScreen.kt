package com.chumakov123.outageschedule.presentation.screen.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Text(
                text = "Филиалы",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Минимум один филиал должен быть выбран",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            state.branches.forEach { branch ->
                val selected = state.selectedUrls.contains(branch.url)

                Text(
                    text = if (selected) "✓ ${branch.name}" else branch.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggle(branch) }
                        .padding(vertical = Spacing.Small),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}