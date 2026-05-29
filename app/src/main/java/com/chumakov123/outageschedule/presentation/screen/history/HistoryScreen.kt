package com.chumakov123.outageschedule.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val data = viewModel.state.collectAsState().value

    ScreenContainer {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {

            item {
                Text(
                    text = "История: ${data.size}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            items(data) {

                Column {

                    Text(
                        text = it.branchName,
                        style = MaterialTheme.typography.titleMedium
                    )

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