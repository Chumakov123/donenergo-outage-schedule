package com.chumakov123.outageschedule.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun SystemBarsController(darkTheme: Boolean) {
    val view = LocalView.current
    val colorScheme = MaterialTheme.colorScheme

    SideEffect {
        val window = (view.context as Activity).window
        val color = Color.Transparent.toArgb() // Use transparent for Edge-to-Edge

        window.statusBarColor = color
        window.navigationBarColor = color

        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !darkTheme
        controller.isAppearanceLightNavigationBars = !darkTheme

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}