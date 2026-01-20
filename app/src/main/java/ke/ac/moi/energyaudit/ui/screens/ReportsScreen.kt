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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.ac.moi.energyaudit.data.AggregatedReading
import ke.ac.moi.energyaudit.utils.toReadableString
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(viewModel: EnergyViewModel) {
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val readings by viewModel.aggregatedReadings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Dropdown for aggregation level
        var expanded by remember { mutableStateOf(false) }

        LevelSelector(expanded, onExpandedChange = { expanded = it }, selectedLevel, onLevelSelected = {
            viewModel.selectLevel(it)
            expanded = false
        }, levels = viewModel.levels)

        Spacer(modifier = Modifier.height(16.dp))

        // Aggregated readings list
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(readings) { item ->
                AggregatedReadingCard(item) {
                    // TODO: Drill-down click action
                    // You can navigate to wings or meters here
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelector(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedLevel: String,
    onLevelSelected: (String) -> Unit,
    levels: List<String>
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpandedChange) {
        OutlinedTextField(
            value = "$selectedLevel Level",
            onValueChange = {},
            readOnly = true,
            label = { Text("Aggregation Level") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            levels.forEach { level ->
                DropdownMenuItem(
                    text = { Text(level) },
                    onClick = { onLevelSelected(level) },
                )
            }
        }
    }
}

@Composable
fun AggregatedReadingCard(
    reading: AggregatedReading,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = reading.level,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            HorizontalDivider()
            Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                MetricItem(
                    icon = Icons.Default.Power,
                    label = "Total Power",
                    value = "%.2f kW".format(reading.totalPowerKw),
                    color = MaterialTheme.colorScheme.primary
                )
                MetricItem(
                    icon = Icons.Default.FlashOn,
                    label = "Avg. Voltage",
                    value = "%.1f V".format(reading.avgVoltage),
                    color = MaterialTheme.colorScheme.secondary
                )
                MetricItem(
                    icon = Icons.Default.Bolt,
                    label = "Total Current",
                    value = "%.2f A".format(reading.totalCurrent),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Default.Update,
                        contentDescription = "Since",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Since: ${reading.since.toReadableString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Icon(
                        Icons.Default.Update,
                        contentDescription = "Last updated",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Updated: ${reading.lastUpdated.toReadableString()}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun RowScope.MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.weight(1f)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, color = color,
            textAlign = TextAlign.Center
        )
    }
}
