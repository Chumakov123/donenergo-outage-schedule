package com.chumakov123.outageschedule.presentation.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PlaceholderScreen(
    title: String
) {
    ScreenContainer {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}