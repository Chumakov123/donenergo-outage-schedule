package com.chumakov123.outageschedule.presentation.screen.alloutages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.domain.model.OutageStatus
import com.chumakov123.outageschedule.domain.model.TrackedPlace
import com.chumakov123.outageschedule.domain.trackedplace.TrackedPlaceMatcher
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
    onOpenTrackedPlaces: (Boolean) -> Unit,
    viewModel: AllOutagesViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader(
                    title = "Отключения",
                    icon = Icons.Default.Bolt,
                    modifier = Modifier.padding(horizontal = 0.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleSearch() },
                        enabled = state.isSearchEnabled || state.isSearchVisible
                    ) {
                        Icon(
                            imageVector = if (state.isSearchVisible) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search",
                            tint = when {
                                state.isSearchVisible -> MaterialTheme.colorScheme.primary
                                !state.isSearchEnabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleFilter(!state.onlyTrackedPlaces) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Мои адреса",
                            tint = if (state.onlyTrackedPlaces) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isSearchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                TopControls(
                    searchQuery = state.searchQuery,
                    onSearchChange = viewModel::onSearchQueryChange,
                    count = state.outages.size
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (state.isLoading && state.rawOutages.isEmpty()) {
                    LoadingState()
                } else {
                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.onRefresh() }
                    ) {
                        when {
                            state.error != null && state.outages.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    ErrorState(message = state.error)
                                }
                            }

                            state.emptyFilterMessage != null -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    EmptyTrackedPlacesState(
                                        message = state.emptyFilterMessage,
                                        actionText = if (state.trackedPlaces.isEmpty()) {
                                            "Добавить адрес"
                                        } else {
                                            "Открыть мои адреса"
                                        },
                                        onActionClick = {
                                            onOpenTrackedPlaces(state.trackedPlaces.isEmpty())
                                        }
                                    )
                                }
                            }

                            else -> {
                                val groupedOutages = state.outages
                                    .sortedWith(
                                        compareBy<Outage> { it.status == OutageStatus.FINISHED }
                                            .thenBy { parseDateTime(it) ?: LocalDateTime.MAX }
                                            .thenBy { it.address.lowercase() }
                                    )
                                    .groupBy { it.city.trim() }

                                val activePlaces = remember(state.trackedPlaces) {
                                    state.trackedPlaces.filter { it.isEnabled }
                                }

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                                ) {
                                    groupedOutages.forEach { (city, outages) ->
                                        stickyHeader {
                                            CityHeader(city)
                                        }

                                        items(outages, key = { it.buildId() }) { outage ->
                                            OutageItem(
                                                modifier = Modifier.animateItem(),
                                                outage = outage,
                                                trackedPlaces = activePlaces
                                            )
                                        }
                                    }

                                    if (state.outages.isNotEmpty()) {
                                        item { Spacer(modifier = Modifier.size(Spacing.Large)) }
                                    }
                                }
                            }
                        }
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
    trackedPlaces: List<TrackedPlace>
) {
    val matches = TrackedPlaceMatcher.findAllMatches(outage, trackedPlaces)
    val highlights = matches.mapNotNull { it.matchedStreetText }.filter { it.isNotBlank() }.distinct()
    val isFinished = outage.status == OutageStatus.FINISHED

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isFinished) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isFinished) 0.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                if (outage.status != OutageStatus.UPCOMING) {
                    StatusBadge(outage.status)
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    InfoRow(Icons.Default.CalendarToday, formatDateRange(outage) ?: "---")
                    InfoRow(Icons.Default.Schedule, formatTimeRange(outage) ?: "---")
                }

                Text(
                    text = buildHighlightedAddress(outage.address, highlights),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = if (isFinished) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface
                    )
                )

                if (!outage.reason.isNullOrBlank()) {
                    InfoRow(Icons.Default.ErrorOutline, outage.reason, MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (!outage.note.isNullOrBlank()) {
                    InfoRow(Icons.Default.Info, outage.note, MaterialTheme.colorScheme.secondary, FontStyle.Italic)
                }
            }

            if (matches.isNotEmpty()) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = if (isFinished) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(Spacing.Medium)
                        .size(20.dp)
                )
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
        modifier = Modifier.size(height = 32.dp, width = 120.dp)
    )
}

@Composable
private fun EmptyTrackedPlacesState(
    message: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.Large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.size(Spacing.Small))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(Spacing.Medium))
        Button(
            onClick = onActionClick
        ) {
            Text(actionText)
        }
    }
}

private fun buildHighlightedAddress(address: String, highlights: List<String>) = buildAnnotatedString {
    if (highlights.isEmpty()) {
        append(address)
        return@buildAnnotatedString
    }

    val ranges = mutableListOf<IntRange>()
    highlights.forEach { highlight ->
        var startIndex = 0
        while (true) {
            val index = address.indexOf(highlight, startIndex, ignoreCase = true)
            if (index == -1) break
            ranges.add(index until (index + highlight.length))
            startIndex = index + highlight.length
        }
    }

    if (ranges.isEmpty()) {
        append(address)
        return@buildAnnotatedString
    }

    val sortedRanges = ranges.sortedBy { it.first }
    val mergedRanges = mutableListOf<IntRange>()
    if (sortedRanges.isNotEmpty()) {
        var current = sortedRanges[0]
        for (i in 1 until sortedRanges.size) {
            val next = sortedRanges[i]
            if (next.first <= current.last + 1) {
                current = current.first..maxOf(current.last, next.last)
            } else {
                mergedRanges.add(current)
                current = next
            }
        }
        mergedRanges.add(current)
    }

    var lastIndex = 0
    mergedRanges.forEach { range ->
        if (range.first > lastIndex) {
            append(address.substring(lastIndex, range.first))
        }
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(address.substring(range.first, minOf(range.last + 1, address.length)))
        }
        lastIndex = range.last + 1
    }
    if (lastIndex < address.length) {
        append(address.substring(lastIndex))
    }
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
