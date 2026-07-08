package com.example.incidentscompose.ui.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.incidentscompose.R
import com.example.incidentscompose.data.model.IncidentResponse
import com.example.incidentscompose.navigation.IncidentListKey
import com.example.incidentscompose.navigation.IncidentMapKey
import com.example.incidentscompose.navigation.MyIncidentListKey
import com.example.incidentscompose.navigation.StatsKey
import com.example.incidentscompose.ui.components.BottomNavBar
import com.example.incidentscompose.viewmodel.StatsPeriod
import com.example.incidentscompose.viewmodel.StatsViewModel
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.Stat
import org.jetbrains.letsPlot.compose.PlotPanel
import org.jetbrains.letsPlot.geom.geomBar
import org.jetbrains.letsPlot.label.ggtitle
import org.jetbrains.letsPlot.themes.theme
import org.jetbrains.letsPlot.themes.elementBlank
import org.koin.compose.viewmodel.koinViewModel
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
@Composable
fun StatsScreen(
    onNavigateToMyIncidentList: () -> Unit,
    onNavigateToIncidentList: () -> Unit,
    onNavigateToIncidentMap: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    viewModel: StatsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavBar(
                currentKey = StatsKey,
                userRole = uiState.userRole,
                onNavigateTo = { route ->
                    when (route) {
                        IncidentListKey -> onNavigateToIncidentList()
                        IncidentMapKey -> onNavigateToIncidentMap()
                        MyIncidentListKey -> onNavigateToMyIncidentList()
                        StatsKey -> { /* Already here */ }
                        else -> {
                            if (route.toString().contains("UserManagement")) {
                                onNavigateToUserManagement()
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.stats),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Official Dashboard - Incident Analytics",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                PeriodSelector(
                    selectedPeriod = uiState.selectedPeriod,
                    onPeriodSelected = { viewModel.selectPeriod(it) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val incidents = uiState.incidents

                if (incidents.isEmpty()) {
                    Text("No incidents found to display statistics.")
                } else {
                    IncidentsByCategoryChart(incidents)
                    IncidentsByStatusChart(incidents)
                    IncidentsPerPeriodChart(incidents, uiState.selectedPeriod)
                }
            }
        }
    }
}

@Composable
fun PeriodSelector(
    selectedPeriod: StatsPeriod,
    onPeriodSelected: (StatsPeriod) -> Unit
) {
    val periods = StatsPeriod.entries.toList()
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        periods.forEachIndexed { index, period ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = periods.size),
                onClick = { onPeriodSelected(period) },
                selected = period == selectedPeriod
            ) {
                Text(period.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

@Composable
fun IncidentsByCategoryChart(incidents: List<IncidentResponse>) {
    val categoryCounts = incidents.groupingBy { it.category.name }.eachCount()
    val data = mapOf(
        "Category" to categoryCounts.keys.toList(),
        "Count" to categoryCounts.values.toList()
    )

    val plot = letsPlot(data) +
            geomBar(stat = Stat.identity) { x = "Category"; y = "Count"; fill = "Category" } +
            ggtitle("Incidents by Category") +
            theme(
                axisTitle = elementBlank(),
                axisText = elementBlank(),
                axisTicks = elementBlank(),
                axisLine = elementBlank(),
                panelGrid = elementBlank()
            )

    ChartCard(title = "By Category") {
        PlotPanel(figure = plot, modifier = Modifier.fillMaxSize(), computationMessagesHandler = { })
    }
}

@Composable
fun IncidentsByStatusChart(incidents: List<IncidentResponse>) {
    val statusCounts = incidents.groupingBy { it.status.name }.eachCount()
    val data = mapOf(
        "Status" to statusCounts.keys.toList(),
        "Count" to statusCounts.values.toList()
    )

    val plot = letsPlot(data) +
            geomBar(stat = Stat.identity) { x = "Status"; y = "Count"; fill = "Status" } +
            ggtitle("Incidents by Status") +
            theme(
                axisTitle = elementBlank(),
                axisText = elementBlank(),
                axisTicks = elementBlank(),
                axisLine = elementBlank(),
                panelGrid = elementBlank()
            )

    ChartCard(title = "By Status") {
        PlotPanel(figure = plot, modifier = Modifier.fillMaxSize(), computationMessagesHandler = { })
    }
}

@Composable
fun IncidentsPerPeriodChart(incidents: List<IncidentResponse>, period: StatsPeriod) {
    val now = ZonedDateTime.now()
    val formatter = when (period) {
        StatsPeriod.DAY -> DateTimeFormatter.ofPattern("HH:00")
        StatsPeriod.WEEK -> DateTimeFormatter.ofPattern("EEE")
        StatsPeriod.MONTH -> DateTimeFormatter.ofPattern("MMM")
    }

    val filteredIncidents = incidents.filter {
        try {
            val normalized = if (!it.createdAt.endsWith("Z") && !it.createdAt.contains("+")) "${it.createdAt}Z" else it.createdAt
            val createdAt = ZonedDateTime.parse(normalized)
            when (period) {
                StatsPeriod.DAY -> createdAt.isAfter(now.minusDays(1))
                StatsPeriod.WEEK -> createdAt.isAfter(now.minusWeeks(1))
                StatsPeriod.MONTH -> createdAt.isAfter(now.minusMonths(1))
            }
        } catch (e: Exception) {
            false
        }
    }

    val groupedData = filteredIncidents.groupBy {
        try {
            val normalized = if (!it.createdAt.endsWith("Z") && !it.createdAt.contains("+")) "${it.createdAt}Z" else it.createdAt
            val createdAt = ZonedDateTime.parse(normalized)
            when (period) {
                StatsPeriod.DAY -> createdAt.format(DateTimeFormatter.ofPattern("MMM dd"))
                else -> createdAt.format(formatter)
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }.mapValues { it.value.size }

    val allPossibleKeys = when (period) {
        StatsPeriod.DAY -> {
            // For day view, we just show one bar for the last 24h or just the current day
            listOf(now.format(DateTimeFormatter.ofPattern("MMM dd")))
        }
        StatsPeriod.WEEK -> {
            // Sort from Monday to Sunday
            val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            days
        }
        StatsPeriod.MONTH -> {
            // For month, we just use the ones that have data or maybe last 30 days? 
            // The request didn't specify for month, so we stick to what we have or just keep it simple.
            groupedData.keys.toList().sortedWith { a, b ->
                try {
                    val dateA = run {
                        val found = filteredIncidents.firstOrNull {
                            try {
                                val normalized = if (!it.createdAt.endsWith("Z") && !it.createdAt.contains("+")) "${it.createdAt}Z" else it.createdAt
                                ZonedDateTime.parse(normalized).format(formatter) == a
                            } catch (e: Exception) { false }
                        }
                        val raw = found?.createdAt ?: "2000-01-01T00:00:00Z"
                        ZonedDateTime.parse(if (!raw.endsWith("Z") && !raw.contains("+")) "${raw}Z" else raw)
                    }
                    val dateB = run {
                        val found = filteredIncidents.firstOrNull {
                            try {
                                val normalized = if (!it.createdAt.endsWith("Z") && !it.createdAt.contains("+")) "${it.createdAt}Z" else it.createdAt
                                ZonedDateTime.parse(normalized).format(formatter) == b
                            } catch (e: Exception) { false }
                        }
                        val raw = found?.createdAt ?: "2000-01-01T00:00:00Z"
                        ZonedDateTime.parse(if (!raw.endsWith("Z") && !raw.contains("+")) "${raw}Z" else raw)
                    }
                    dateA.compareTo(dateB)
                } catch (e: Exception) {
                    a.compareTo(b)
                }
            }
        }
    }

    val data = mapOf(
        "Period" to allPossibleKeys,
        "Count" to allPossibleKeys.map { groupedData[it] ?: 0 }
    )

    val plot = letsPlot(data) +
            geomBar(stat = Stat.identity) { x = "Period"; y = "Count"; fill = "Period" } +
            ggtitle("Incidents distribution (${period.name})") +
            theme(
                axisTitle = elementBlank(),
                axisText = elementBlank(),
                axisTicks = elementBlank(),
                axisLine = elementBlank(),
                panelGrid = elementBlank()
            )

    ChartCard(title = "Incident Distribution") {
        PlotPanel(figure = plot, modifier = Modifier.fillMaxSize(), computationMessagesHandler = { })
    }
}

@Composable
fun ChartCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
        }
    }
}
