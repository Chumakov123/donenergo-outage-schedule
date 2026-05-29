package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun AllOutagesScreen(
    viewModel: AllOutagesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {

        if (state.isLoading && state.outages.isEmpty()) {
            CircularProgressIndicator()
            return@ScreenContainer
        }

        if (state.error != null && state.outages.isEmpty()) {
            Text(state.error)
            return@ScreenContainer
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {

            item {
                if (state.isLoading) {
                    Text("Обновление...")
                }
            }

            item {
                Text(
                    text = "Загружено: ${state.outages.size}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            items(state.outages) {

                val statusText = when (it.status) {
                    OutageStatus.UPCOMING -> "Запланировано"
                    OutageStatus.ACTIVE -> "Идёт"
                    OutageStatus.FINISHED -> "Завершено"
                }

                Column {
                    Text(
                        text = it.branchName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(statusText)

                    Text(it.city)

                    Text(
                        "${it.startDate}-${it.endDate}"
                    )

                    Text(
                        "${it.startTime}-${it.endTime}"
                    )

                    Text(it.address)
                }
            }
        }
    }
}