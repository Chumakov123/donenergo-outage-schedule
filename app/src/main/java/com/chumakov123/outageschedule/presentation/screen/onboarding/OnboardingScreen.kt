package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {

            item {
                Text(
                    text = "Выбор филиалов (минимум 1)",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            items(state.branches) { branch ->

                val isSelected =
                    state.selectedUrls.contains(branch.url)

                val suggestions =
                    state.citySuggestionsByBranchUrl[branch.url]
                        .orEmpty()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.toggle(branch)
                        }
                        .padding(vertical = Spacing.Small)
                ) {

                    Text(
                        text = if (isSelected) {
                            "✓ ${branch.name}"
                        } else {
                            branch.name
                        },
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

            item {
                Button(
                    onClick = {
                        viewModel.finish(onFinish)
                    },
                    enabled = state.selectedUrls.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Продолжить")
                }
            }
        }
    }
}