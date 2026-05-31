package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
import com.chumakov123.outageschedule.presentation.component.EmptyState
import com.chumakov123.outageschedule.presentation.component.ErrorState
import com.chumakov123.outageschedule.presentation.component.LoadingState
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.component.SectionHeader
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AllOutagesScreen(
    viewModel: AllOutagesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        Box(modifier = Modifier.fillMaxSize()) {
            if (state.isLoading && state.rawOutages.isEmpty()) {
                LoadingState()
            } else {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = { viewModel.onRefresh() }
                ) {
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
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = Spacing.Small),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SectionHeader(
                                    title = "Отключения",
                                    icon = Icons.Default.Bolt,
                                    modifier = Modifier.padding(horizontal = 0.dp)
                                )

                                FilterChip(
                                    selected = state.onlyTrackedPlaces,
                                    onClick = { viewModel.toggleFilter(!state.onlyTrackedPlaces) },
                                    label = { Text("Мои", style = MaterialTheme.typography.labelMedium) },
                                    leadingIcon = if (state.onlyTrackedPlaces) {
                                        { Icon(Icons.Default.Place, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    modifier = Modifier.padding(end = Spacing.Medium)
                                )
                            }
                        }

                        // Поиск
                        if (state.outages.isNotEmpty() || state.searchQuery.isNotEmpty()) {
                            item {
                                TopControls(
                                    searchQuery = state.searchQuery,
                                    onSearchChange = viewModel::onSearchQueryChange,
                                    count = state.outages.size
                                )
                            }
                        }

                        if (state.error != null && state.outages.isEmpty()) {
                            item { ErrorState(message = state.error) }
                        } else if (state.emptyFilterMessage != null) {
                            item { EmptyState(message = state.emptyFilterMessage) }
                        }

                        groupedOutages.forEach { (city, outages) ->
                            stickyHeader {
                                CityHeader(city)
                            }

                            items(outages, key = { it.buildId() }) { outage ->
                                OutageItem(
                                    modifier = Modifier.animateItem(),
                                    outage = outage,
                                    trackedPlaces = state.trackedPlaces
                                )
                            }
                        }

                        item { Spacer(modifier = Modifier.size(Spacing.Large)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopControls(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    count: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск по адресу...") },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent
            )
        )
        
        if (searchQuery.isNotEmpty() || count > 0) {
            Text(
                text = "Найдено записей: $count",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(start = Spacing.Medium)
            )
        }
    }
}

@Composable
private fun CityHeader(city: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = Spacing.Medium, vertical = 8.dp)
    ) {
        Text(
            text = city,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun OutageItem(
    modifier: Modifier = Modifier,
    outage: Outage,
    trackedPlaces: List<com.chumakov123.outageschedule.domain.model.TrackedPlace>
) {
    val matchedPlace = TrackedPlaceMatcher.findBestMatch(outage, trackedPlaces)

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatusBadge(outage.status)
                if (matchedPlace != null) {
                    Icon(
                        Icons.Default.LocationOn,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow(Icons.Default.CalendarToday, formatDateRange(outage) ?: "---")
                InfoRow(Icons.Default.Schedule, formatTimeRange(outage) ?: "---")
            }

            Text(
                text = buildHighlightedAddress(outage.address, matchedPlace?.matchedStreetText),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (matchedPlace != null) FontWeight.Bold else FontWeight.Medium
                )
            )

            if (!outage.reason.isNullOrBlank()) {
                InfoRow(Icons.Default.ErrorOutline, outage.reason, MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (!outage.note.isNullOrBlank()) {
                InfoRow(Icons.Default.Info, outage.note, MaterialTheme.colorScheme.secondary, FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, color: Color = MaterialTheme.colorScheme.onSurface, fontStyle: FontStyle? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.width(Spacing.Small))
        Text(text, style = MaterialTheme.typography.bodyMedium.copy(color = color, fontStyle = fontStyle))
    }
}

@Composable
private fun StatusBadge(status: OutageStatus) {
    val (label, containerColor, contentColor) = when (status) {
        OutageStatus.ACTIVE -> Triple("Идёт сейчас", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
        OutageStatus.FINISHED -> Triple("Завершено", MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
        OutageStatus.UPCOMING -> Triple("Ожидается", MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer)
    }
    AssistChip(
        onClick = { },
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor, labelColor = contentColor),
        border = null,
        modifier = Modifier.size(height = 32.dp, width = 120.dp) // Adjusted width for better fit
    )
}

private fun buildHighlightedAddress(address: String, highlight: String?) = buildAnnotatedString {
    if (highlight.isNullOrBlank()) { append(address); return@buildAnnotatedString }
    val index = address.indexOf(highlight, ignoreCase = true)
    if (index < 0) { append(address); return@buildAnnotatedString }
    append(address.substring(0, index))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(address.substring(index, index + highlight.length)) }
    append(address.substring(index + highlight.length))
}

private fun formatDateRange(outage: Outage): String? {
    val start = formatDate(outage.startDate)
    val end = formatDate(outage.endDate)
    return if (start != null && end != null) (if (outage.startDate == outage.endDate) start else "$start — $end") else (start ?: end)
}

private fun formatTimeRange(outage: Outage): String? {
    val start = outage.startTime?.takeIf { it.isNotBlank() }
    val end = outage.endTime?.takeIf { it.isNotBlank() }
    return if (start != null && end != null) "$start — $end" else (start ?: end)
}

private val inputDateFormats = listOf(DateTimeFormatter.ofPattern("dd.MM.yyyy"), DateTimeFormatter.ofPattern("dd.MM.yy"))
private val outputDateFormat = DateTimeFormatter.ofPattern("d MMMM", Locale.forLanguageTag("ru"))

private fun formatDate(value: String?): String? {
    val date = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
    inputDateFormats.forEach { formatter -> runCatching { LocalDate.parse(date, formatter) }.getOrNull()?.let { return it.format(outputDateFormat) } }
    return date
}

private fun parseDate(date: String): LocalDate? = inputDateFormats.firstNotNullOfOrNull { runCatching { LocalDate.parse(date.trim(), it) }.getOrNull() }

private fun parseDateTime(outage: Outage): LocalDateTime? {
    val date = outage.startDate.takeIf { it.isNotBlank() } ?: return null
    val parsedDate = parseDate(date) ?: return null
    val parsedTime = runCatching { LocalTime.parse(outage.startTime?.takeIf { it.isNotBlank() } ?: "00:00") }.getOrNull() ?: LocalTime.MIN
    return LocalDateTime.of(parsedDate, parsedTime)
}
