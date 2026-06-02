package com.chumakov123.outageschedule.presentation.screen.history

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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chumakov123.outageschedule.domain.model.Outage
import com.chumakov123.outageschedule.presentation.component.ScreenContainer
import com.chumakov123.outageschedule.presentation.theme.Spacing
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = koinViewModel()
) {
    val state = viewModel.state.collectAsState().value

    ScreenContainer {
        if (state.isLoading && state.rawItems.isEmpty()) {
            LoadingState()
        } else if (state.rawItems.isEmpty()) {
            EmptyHistoryState()
        } else {
            val groupedItems = state.filteredItems
                .groupBy { formatMonthYear(it) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                // Шапка и Поиск
                item {
                    HistoryHeader(
                        searchQuery = state.searchQuery,
                        onSearchChange = viewModel::onSearchQueryChange,
                        totalCount = state.rawItems.size,
                        filteredCount = state.filteredItems.size
                    )
                }

                if (state.filteredItems.isEmpty() && state.searchQuery.isNotEmpty()) {
                    item {
                        NoSearchResultsState(state.searchQuery)
                    }
                }

                groupedItems.forEach { (month, items) ->
                    stickyHeader {
                        MonthHeader(month)
                    }

                    items(items, key = { it.buildId() }) { outage ->
                        HistoryItem(
                            modifier = Modifier.animateItem(),
                            outage = outage
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.size(Spacing.Large))
                }
            }
        }
    }
}

@Composable
private fun HistoryHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    totalCount: Int,
    filteredCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
    ) {
        Text(
            text = "Архив отключений",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Поиск в архиве...") },
            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchChange("") }) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(20.dp))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        )

        Text(
            text = if (searchQuery.isEmpty()) "Всего записей: $totalCount" else "Найдено: $filteredCount",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MonthHeader(month: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(horizontal = Spacing.Medium, vertical = 8.dp)
    ) {
        Text(
            text = month,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun HistoryItem(
    modifier: Modifier = Modifier,
    outage: Outage
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.Medium, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Business, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(Spacing.Small))
                Text(outage.branchName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoRow(Icons.Default.CalendarToday, formatDateRange(outage) ?: "---")
                InfoRow(Icons.Default.Schedule, formatTimeRange(outage) ?: "---")
            }

            Row {
                Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(18.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(Spacing.Small))
                Column {
                    Text(outage.city, style = MaterialTheme.typography.titleSmall)
                    Text(outage.address, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (!outage.reason.isNullOrBlank()) {
                InfoRow(Icons.Default.Info, outage.reason, MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = color.copy(alpha = 0.6f))
        Spacer(modifier = Modifier.width(Spacing.Small))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun LoadingState() {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator(); Spacer(Modifier.size(Spacing.Medium)); Text("Загрузка архива...")
    }
}

@Composable
private fun EmptyHistoryState() {
    Column(modifier = Modifier.fillMaxSize().padding(Spacing.Large), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(Modifier.size(Spacing.Medium))
        Text("Архив пуст", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("Здесь будут появляться записи о завершенных отключениях.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NoSearchResultsState(query: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(Spacing.Large), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("По запросу «$query» ничего не найдено", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

private fun formatMonthYear(outage: Outage): String {
    val date = parseDate(outage.startDate) ?: return "Разное"
    return date.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.forLanguageTag("ru")))
        .replaceFirstChar { it.uppercase() }
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
