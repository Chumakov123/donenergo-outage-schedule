package com.chumakov123.outageschedule.presentation.screen.history

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val data = viewModel.state.collectAsState().value

    ScreenContainer {
        Column {
            Text("История: ${data.size}")

            data.take(10).forEach {
                Text(
                    "${it.branchName}\n${it.city}\n${it.startDate}-${it.endDate}\n${it.startTime}-${it.endTime}\n${it.address}\n"
                )
            }
        }
    }
}