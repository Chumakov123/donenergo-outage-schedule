package com.chumakov123.outageschedule.presentation.screen.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
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

            items(data) { outage ->

                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
                ) {

                    Text(
                        text = outage.branchName,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = outage.city,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "${outage.startDate} — ${outage.endDate}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = "${outage.startTime} — ${outage.endTime}",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Text(
                        text = outage.address,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    outage.reason?.takeIf { it.isNotBlank() }?.let { reason ->
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    outage.note?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            text = "Примечание: $note",
                            style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = Spacing.Small)
                    )
                }
            }
        }
    }
}