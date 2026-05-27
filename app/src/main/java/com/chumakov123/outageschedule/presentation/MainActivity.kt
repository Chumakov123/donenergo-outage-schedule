package com.chumakov123.outageschedule.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chumakov123.outageschedule.presentation.navigation.AppScaffold
import com.chumakov123.outageschedule.presentation.theme.OutageScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OutageScheduleTheme {
                AppScaffold()
            }
        }
    }
}