package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.presentation.navigation.AppState
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    Column {

        Text("Выбор филиала")

        state.branches.forEach { branch ->

            val isSelected = branch == state.selected

            Text(
                text = if (isSelected) {
                    "✓ ${branch.name}"
                } else {
                    branch.name
                },
                modifier = Modifier.clickable {
                    viewModel.select(branch)
                }
            )
        }

        Button(
            onClick = {

                AppState.selectedBranch = state.selected

                AppState.isOnboardingCompleted = true

                onFinish()
            }
        ) {
            Text("Продолжить")
        }
    }
}