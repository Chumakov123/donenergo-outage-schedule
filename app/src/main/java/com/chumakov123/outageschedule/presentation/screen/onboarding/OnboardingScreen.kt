package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    Column {
        Text("Выбор филиалов (минимум 1)")
        Spacer(modifier = Modifier.height(Spacing.Small))

        state.branches.forEach { branch ->
            val isSelected = state.selectedUrls.contains(branch.url)
            val suggestions = state.citySuggestionsByBranchUrl[branch.url].orEmpty()

            Column(
                modifier = Modifier.clickable { viewModel.toggle(branch) }
            ) {
                Text(
                    text = if (isSelected) "✓ ${branch.name}" else branch.name,
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

                Spacer(modifier = Modifier.height(Spacing.Small))
            }
        }

        Button(
            onClick = { viewModel.finish(onFinish) },
            enabled = state.selectedUrls.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
}