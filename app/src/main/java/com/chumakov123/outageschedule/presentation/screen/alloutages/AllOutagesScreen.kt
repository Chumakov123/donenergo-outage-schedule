package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.chumakov123.outageschedule.presentation.component.ScreenContainer

@Composable
fun AllOutagesScreen(
    viewModel: AllOutagesViewModel = org.koin.androidx.compose.koinViewModel()
) {

    val data = viewModel.state.collectAsState()

    ScreenContainer {
        Column {
            Text("Загружено: ${data.value.size}")

            data.value.take(5).forEach {
                Text("${it.city}\n${it.startDate}-${it.endDate}\n${it.startTime}-${it.endTime}\n${it.address}\n")
            }
        }
    }
}