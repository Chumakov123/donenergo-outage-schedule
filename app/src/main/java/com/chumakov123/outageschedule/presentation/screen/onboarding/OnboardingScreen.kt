package com.chumakov123.outageschedule.presentation.screen.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel = koinViewModel(),
    onFinish: () -> Unit
) {
    val state = viewModel.state.collectAsState().value

    LaunchedEffect(Unit) {
        viewModel.finishEvent.collect {
            onFinish()
        }
    }

    Column {

        Text("Выбор филиалов (минимум 1)")

        state.branches.forEach { branch ->

            val selected = state.selectedUrls.contains(branch.url)

            Text(
                text = if (selected) "✓ ${branch.name}" else branch.name,
                modifier = Modifier.clickable {
                    viewModel.toggle(branch)
                }
            )
        }

        Button(
            onClick = {
                viewModel.finish()
            },
            enabled = state.selectedUrls.isNotEmpty()
        ) {
            Text("Продолжить")
        }
    }
}