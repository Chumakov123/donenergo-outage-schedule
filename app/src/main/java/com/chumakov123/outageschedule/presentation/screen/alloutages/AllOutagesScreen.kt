package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

        val groupedOutages = state.outages
            .sortedWith(
                compareBy(
                    { parseDateTime(it) ?: LocalDateTime.MAX },
                    { it.address.lowercase() }
                )
            )
            .groupBy { it.city.trim() }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.Medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Только мои места",
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Switch(
                        checked = state.onlyTrackedPlaces,
                        onCheckedChange = { enabled ->
                            viewModel.toggleFilter(enabled)
                        }
                    )
                }
            }

            if (state.emptyFilterMessage != null) {
                item {
                    Text(
                        text = state.emptyFilterMessage,
                        modifier = Modifier.padding(horizontal = Spacing.Medium),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

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

            groupedOutages.forEach { (city, outages) ->

                item {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = Spacing.Small)
                    )
                }

                items(outages) { outage ->
                    val matchedPlace = TrackedPlaceMatcher.findBestMatch(
                        outage = outage,
                        places = state.trackedPlaces
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
                    ) {

                        when (outage.status) {
                            OutageStatus.ACTIVE -> {
                                Text(
                                    text = "Идёт",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            OutageStatus.FINISHED -> {
                                Text(
                                    text = "Завершено",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutageStatus.UPCOMING -> {
                                // Не отображаем метку
                            }
                        }

                        formatDateRange(outage)?.let { dateText ->
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        formatTimeRange(outage)?.let { timeText ->
                            Text(
                                text = timeText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = buildHighlightedAddress(
                                address = outage.address,
                                highlight = matchedPlace?.matchedStreetText
                            ),
                            style = MaterialTheme.typography.bodyLarge
                        )

                        outage.reason
                            ?.takeIf { it.isNotBlank() }
                            ?.let { reason ->
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

private fun buildHighlightedAddress(
    address: String,
    highlight: String?
) = buildAnnotatedString {
    if (highlight.isNullOrBlank()) {
        append(address)
        return@buildAnnotatedString
    }

    val index = address.indexOf(highlight, ignoreCase = true)

    if (index < 0) {
        append(address)
        return@buildAnnotatedString
    }

    append(address.substring(0, index))
    withStyle(
        SpanStyle(
            fontWeight = FontWeight.SemiBold,
            color = Color.Unspecified
        )
    ) {
        append(address.substring(index, index + highlight.length))
    }
    append(address.substring(index + highlight.length))
}

private fun formatDateRange(outage: Outage): String? {
    val start = formatDate(outage.startDate)
    val end = formatDate(outage.endDate)

    return when {
        start == null && end == null -> null

        start != null && end != null -> {
            if (outage.startDate == outage.endDate) {
                start
            } else {
                "$start — $end"
            }
        }

        start != null -> start
        else -> end
    }
}

private fun formatTimeRange(outage: Outage): String? {
    val start = outage.startTime?.takeIf { it.isNotBlank() }
    val end = outage.endTime?.takeIf { it.isNotBlank() }

    return when {
        start != null && end != null -> "$start — $end"
        start != null -> start
        else -> end
    }
}

private fun formatDate(value: String?): String? {
    val date = value?.takeIf { it.isNotBlank() }
        ?: return null

    return runCatching {
        val parsed = LocalDate.parse(
            date,
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        )

        parsed.format(
            DateTimeFormatter.ofPattern(
                "d MMMM",
                Locale.forLanguageTag("ru")
            )
        )
    }.getOrNull()
}

private fun parseDateTime(outage: Outage): LocalDateTime? {
    val date = outage.startDate?.takeIf { it.isNotBlank() }
        ?: return null

    val time = outage.startTime
        ?.takeIf { it.isNotBlank() }
        ?: "00:00"

    return runCatching {
        val parsedDate = LocalDate.parse(
            date,
            DateTimeFormatter.ofPattern("dd.MM.yyyy")
        )

        val parsedTime = LocalTime.parse(time)

        LocalDateTime.of(parsedDate, parsedTime)
    }.getOrNull()
}