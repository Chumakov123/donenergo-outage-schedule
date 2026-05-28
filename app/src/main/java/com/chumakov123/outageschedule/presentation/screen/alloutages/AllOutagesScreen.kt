package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllOutagesScreen(
    viewModel: AllOutagesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        Column {
            if (state.isLoading && state.outages.isEmpty()) {
                CircularProgressIndicator()
                return@ScreenContainer
            }

            if (state.isLoading) {
                Text("Обновление...")
            }

            if (state.error != null && state.outages.isEmpty()) {
                Text(state.error)
                return@ScreenContainer
            }

            Text("Загружено: ${state.outages.size}")

            state.outages.take(5).forEach {
                val statusText = when (it.status) {
                    OutageStatus.UPCOMING -> "Запланировано"
                    OutageStatus.ACTIVE -> "Идёт"
                    OutageStatus.FINISHED -> "Завершено"
                }

                Text(
                    "${it.branchName}\n$statusText\n${it.city}\n${it.startDate}-${it.endDate}\n${it.startTime}-${it.endTime}\n${it.address}\n"
                )
            }
        }
    }
}