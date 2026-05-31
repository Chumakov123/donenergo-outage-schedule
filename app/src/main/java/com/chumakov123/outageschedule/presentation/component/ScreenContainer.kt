package com.chumakov123.outageschedule.presentation.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.presentation.theme.Spacing

@Composable
fun ScreenContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = Spacing.Medium,
                end = Spacing.Medium,
                top = Spacing.Medium,
                bottom = 0.dp
            )
    ) {
        content()
    }
}