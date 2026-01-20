package ke.ac.moi.energyaudit.ui.screens


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ke.ac.moi.energyaudit.data.ChartRange
import ke.ac.moi.energyaudit.data.ChartStats
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.ui.viewmodel.ChartViewModel
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModel
import ke.ac.moi.energyaudit.utils.computeStats
import ke.ac.moi.energyaudit.utils.exportEnergyReadingsToCsv
import ke.ac.moi.energyaudit.utils.exportPdfReport
import ke.ac.moi.energyaudit.utils.renderChartBitmap
import ke.ac.moi.energyaudit.utils.saveBitmapAsPng
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    meterId: String?,
    energyViewModel: EnergyViewModel,
    chartViewModel: ChartViewModel,
    onMeterRemoved: () -> Unit
) {
    val meters by energyViewModel
        .meters
        .collectAsStateWithLifecycle()

    var meter by remember(meterId) {
        mutableStateOf(meters.find { it.meterId == meterId } ?: meters.first())
    }

    var range by rememberSaveable { mutableStateOf(ChartRange.LAST_30) }
    val readings by chartViewModel
        .chartData(meter.meterId, range.limit)
        .collectAsState(initial = emptyList())

    val stats = remember(readings) { computeStats(readings) }
    val context = LocalContext.current
    val textMeasurer = rememberTextMeasurer()

    val exportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("text/csv"),
        ) { uri ->
            uri?.let {
                exportEnergyReadingsToCsv(
                    resolver = context.contentResolver,
                    uri = it,
                    readings = readings
                )
                Toast
                    .makeText(context, "CSV exported successfully", Toast.LENGTH_SHORT)
                    .show()

            }
        }

    val imageExportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("image/png")
        ) { uri ->
            uri?.let {
                val bitmap = renderChartBitmap(context, readings, textMeasurer = textMeasurer)
                saveBitmapAsPng(context.contentResolver, it, bitmap)
            }
        }

    val pdfExportLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri ->
            uri?.let {
                val chartBitmap = renderChartBitmap(context, readings, textMeasurer = textMeasurer)
                exportPdfReport(
                    context = context,
                    uri = it,
                    chartBitmap = chartBitmap,
                    stats = stats!!,
                    meter = meter,
                    range = range
                )
            }
        }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 🔑 Essential for overflow
            .padding(16.dp)
    ) {
        SectionHeader("Meter Details")
        Spacer(Modifier.height(16.dp))

        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Meter ID: ${meter.meterId}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Building: ${meter.building}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Block: ${meter.block}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Wing: ${meter.wing}",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Lat: ${"%.4f".format(meter.latitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Lon: ${"%.4f".format(meter.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Installed: ${meter.installedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }


        Spacer(Modifier.height(16.dp))

        SectionHeader("Range Selector")
        Spacer(Modifier.height(16.dp))

        ChartRangeSelector(
            selected = range,
            onSelected = { range = it }
        )

        Spacer(Modifier.height(16.dp))

        // Group 2: Visualizations
        SectionHeader("Performance Overview")

        Spacer(Modifier.height(16.dp))

        ElevatedCard(
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Power Usage (kW) Over Time",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))

                // Transition animation for data changes
                AnimatedContent(targetState = readings.isEmpty(), label = "chart") { isEmpty ->
                    if (isEmpty) {
                        Box(
                            Modifier
                                .height(250.dp)
                                .fillMaxWidth(), Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        ZoomablePowerLineChart(readings)
                    }
                }
                Text(
                    "Pinch to zoom • Drag to scroll",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )

                HorizontalDivider(Modifier.padding(vertical = 16.dp))

                stats?.let {
                    ChartStatsRow(it)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        SectionHeader("Export")

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                exportLauncher.launch(
                    "energy_${meter.meterId}_${range.name.lowercase()}.csv"
                )
            },
            enabled = readings.isNotEmpty()
        ) {
            Icon(Icons.Default.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Download CSV")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                imageExportLauncher.launch(
                    "chart_${meter.meterId}_${range.name.lowercase()}.png"
                )
            },
            enabled = readings.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Image, null)
            Spacer(Modifier.width(8.dp))
            Text("Export Chart Image")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = readings.isNotEmpty(),
            onClick = {
                pdfExportLauncher.launch(
                    "energy_report_${meter.meterId}_${range.name.lowercase()}.pdf"
                )
            }
        ) {
            Icon(Icons.Default.PictureAsPdf, null)
            Spacer(Modifier.width(8.dp))
            Text("Export PDF Report")
        }

        Spacer(Modifier.height(24.dp))

        // DANGER ZONE

        SectionHeader("Danger Zone")

        Spacer(Modifier.height(16.dp))
        var showConfirmDialog by remember { mutableStateOf(false) }

        Column {
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showConfirmDialog = true // show the confirmation dialog
                }
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Remove Meter and All Readings")
            }

            if (showConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmDialog = false },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to remove this meter and all its readings? This action cannot be undone.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                energyViewModel.removeMeter(meter) // perform deletion
                                onMeterRemoved()
                                showConfirmDialog = false // close dialog
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ChartStatsRow(
    stats: ChartStats,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Min",
                value = stats.min,
                icon = Icons.Default.KeyboardArrowDown,
                color = MaterialTheme.colorScheme.primary
            )

            VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)

            StatItem(
                label = "Average",
                value = stats.avg,
                icon = Icons.Default.Settings, // Or a custom 'balance' icon
                color = MaterialTheme.colorScheme.secondary
            )

            VerticalDivider(modifier = Modifier.height(24.dp), thickness = 1.dp)

            StatItem(
                label = "Peak",
                value = stats.max,
                icon = Icons.Default.KeyboardArrowUp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: Float,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp
            )
        }
        Text(
            text = "${"%.1f".format(value)} kW",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
