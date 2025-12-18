package ke.ac.moi.energyaudit.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.ac.moi.energyaudit.data.ChartRange
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.ui.viewmodel.ChartViewModel
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModel
import ke.ac.moi.energyaudit.utils.computeStats
import kotlinx.coroutines.flow.flowOf
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(
    energyViewModel: EnergyViewModel,
    chartViewModel: ChartViewModel,
) {
    val meters by energyViewModel.meters.collectAsStateWithLifecycle()
    var selectedMeter by remember { mutableStateOf(meters.firstOrNull()) }
    var reportPeriod by rememberSaveable { mutableStateOf(ChartRange.LAST_30) }

    // --- 2. Data Logic ---
    val readings by remember(selectedMeter, reportPeriod) {
        selectedMeter?.let { chartViewModel.chartData(it.meterId, reportPeriod.limit) }
            ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val stats = remember(readings) { computeStats(readings) }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            // --- Selector ---
            item {
                MeterLocationSelector(
                    locations = meters,
                    selectedLocation = selectedMeter,
                    onLocationSelected = { selectedMeter = it }
                )
            }

            // --- Performance Grade Section ---
            item {
                EfficiencyGradeCard(stats?.avg ?: 0f)
            }

            // --- Summary Stats ---
            item {
                Text("Consumption Summary", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                // Reusing the improved stats row from earlier
                stats?.let { ChartStatsRow(it) }
            }

            // --- Simplified Trend View ---
            item {
                ElevatedCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Historical Trend", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(16.dp))
                        // Using the chart we built earlier
                        PowerLineChart(
                            readings = readings,
                            modifier = Modifier.fillMaxWidth().height(180.dp)
                        )
                    }
                }
            }

            // --- Anomaly List ---
            item {
                Text("Detected Inefficiencies", style = MaterialTheme.typography.titleMedium)
            }

            val anomalies = readings.filter { it.powerKw > 15f }
            if (anomalies.isEmpty()) {
                item { Text("No issues detected for this period.", style = MaterialTheme.typography.bodySmall) }
            } else {
                items(anomalies.take(3)) { reading ->
                    AnomalyItem(reading)
                }
            }
        }

        IconButton(modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), onClick = { /* Open Date Picker */ }) {
            IconBox(icon = Icons.Default.DateRange, contentDescription = "Select Date", size = 60)
        }
    }
}

@Composable
fun EfficiencyGradeCard(avgPower: Float) {
    val grade = when {
        avgPower < 5f -> "A+" to MaterialTheme.colorScheme.primary
        avgPower < 10f -> "B" to MaterialTheme.colorScheme.secondary
        else -> "C" to MaterialTheme.colorScheme.error
    }

    Card (
        colors = CardDefaults.cardColors(containerColor = grade.second.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Building Efficiency", style = MaterialTheme.typography.labelMedium)
                Text("Overall Rating", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            Surface (
                shape = CircleShape,
                color = grade.second,
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(grade.first, color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

@Composable
fun AnomalyItem(reading: EnergyReadingEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text("High Peak: ${"%.1f".format(reading.powerKw)} kW", style = MaterialTheme.typography.bodyMedium)
            Text(
                reading.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ReportsScreen1(
    energyViewModel: EnergyViewModel,
    chartViewModel: ChartViewModel,
    // Skeleton Setters
    setTitle: (String) -> Unit,
    setTopActions: (@Composable RowScope.() -> Unit) -> Unit,
    setFabIcon: (ImageVector?) -> Unit
) {
    val meters by energyViewModel.meters.collectAsStateWithLifecycle()
    var selectedMeter by remember { mutableStateOf(meters.firstOrNull()) }
    var selectedRange by rememberSaveable { mutableStateOf(ChartRange.LAST_30) }

    // 1. Sync with Skeleton (Fixing the "disappearing UI" issue)
    LaunchedEffect(Unit) {
        setTitle("Performance Report")
        setFabIcon(Icons.Default.Share) // Report export action
        setTopActions {
            IconButton(onClick = { /* Open Filter Sheet */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filter")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            setTitle("NoteFlow") // Or default
            setTopActions {}
            setFabIcon(null)
        }
    }

    // 2. Data Fetching
    val readings by remember(selectedMeter, selectedRange) {
        selectedMeter?.let { chartViewModel.chartData(it.meterId, selectedRange.limit) }
            ?: flowOf(emptyList())
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val stats = remember(readings) { computeStats(readings) }

    // 3. Layout
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Selection Section ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Select Meter", style = MaterialTheme.typography.labelLarge)
                MeterLocationSelector(
                    locations = meters,
                    selectedLocation = selectedMeter,
                    onLocationSelected = { selectedMeter = it }
                )
            }
        }

        // --- Summary Grid ---
        item {
            stats?.let {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Executive Summary", style = MaterialTheme.typography.titleMedium)
                    ChartStatsRow(it) // Reusing our improved stats row
                }
            }
        }

        // --- Visual Analysis Section ---
        item {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row (
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Power Consumption Trend", style = MaterialTheme.typography.titleSmall)
                        // Range chips inside the card
                        RangeChipGroup(selectedRange) { selectedRange = it }
                    }

                    Spacer(Modifier.height(16.dp))

                    ZoomablePowerLineChart(
                        readings = readings,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- Detailed Table Section ---
        item {
            Text("High Usage Events", style = MaterialTheme.typography.titleMedium)
        }

        items(readings.filter { it.powerKw > 15f }.take(5)) { alertReading ->
            EfficiencyAlertItem(alertReading)
        }
    }
}

@Composable
fun RangeChipGroup(selected: ChartRange, onSelected: (ChartRange) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChartRange.entries.forEach { range ->
            FilterChip(
                selected = selected == range,
                onClick = { onSelected(range) },
                label = { Text(range.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
fun EfficiencyAlertItem(reading: EnergyReadingEntity) {
    OutlinedCard (
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    "High Load Detected: ${"%.1f".format(reading.powerKw)} kW",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    reading.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}