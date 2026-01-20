package ke.ac.moi.energyaudit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import ke.ac.moi.energyaudit.data.ChartRange
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MeterCard(
    meter: MeterLocationEntity,
    reading: EnergyReadingEntity,
    inefficient: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (inefficient)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "${meter.building} - Wing ${meter.wing}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Meter ID: ${meter.meterId}")

            Spacer(Modifier.height(8.dp))

            Text("Power: ${"%.2f".format(reading.powerKw)} kW")
            Text("Voltage: ${reading.voltage} V")
            Text("Current: ${reading.current} A")
        }
    }
}

@Composable
fun MeterCard1(
    meter: MeterLocationEntity,
    viewModel: EnergyViewModel
) {
    val reading by viewModel
        .latestReading(meter.meterId)
        .collectAsState(initial = null)

    reading?.let {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("${meter.building} - Wing ${meter.wing}")
                Text("Power: ${"%.2f".format(it.powerKw)} kW")
                Text(
                    viewModel.generateRecommendation(it)
                )
            }
        }
    }
}



@OptIn(ExperimentalTextApi::class)
@Composable
fun PowerLineChart(
    readings: List<EnergyReadingEntity>,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    // 1. Initialize TextMeasurer
    val textMeasurer = rememberTextMeasurer()

    if (readings.size < 2) return

    val maxPower = readings.maxOf { it.powerKw }.coerceAtLeast(1f)
    val minPower = readings.minOf { it.powerKw }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
    ) {
        // 2. Define Margins for Labels
        val labelMarginX = 50.dp.toPx() // Space for Y-axis labels (kW)
        val labelMarginY = 30.dp.toPx() // Space for X-axis labels (Time)

        val chartWidth = size.width - labelMarginX
        val chartHeight = size.height - labelMarginY
        val spacingX = chartWidth / (readings.size - 1)

        fun getY(value: Float): Float {
            val range = (maxPower - minPower).coerceAtLeast(1f)
            return chartHeight - ((value - minPower) / range) * chartHeight
        }

        // 3. Draw Y-Axis Labels (kW) and Horizontal Grid
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = (chartHeight / gridLines) * i
            val labelValue = maxPower - ((maxPower - minPower) / gridLines) * i
            val formattedValue = "%.1f".format(labelValue)

            // Draw horizontal dashed grid line
            drawLine(
                color = gridColor,
                start = Offset(labelMarginX, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )

            // Draw text on the left of the grid
            drawText(
                textMeasurer = textMeasurer,
                text = "$formattedValue kW",
                style = labelStyle,
                topLeft = Offset(0f, y - 10.dp.toPx()) // Center the text vertically
            )
        }

        // 4. Draw X-Axis Labels (Time)
        // We only show a few labels to avoid clutter
        val labelFrequency = (readings.size / 4).coerceAtLeast(1)
        readings.forEachIndexed { index, reading ->
            if (index % labelFrequency == 0 || index == readings.lastIndex) {
                val x = labelMarginX + (index * spacingX)
                val timeText = reading.timestamp.format(timeFormatter)

                drawText(
                    textMeasurer = textMeasurer,
                    text = timeText,
                    style = labelStyle,
                    topLeft = Offset(x - 20.dp.toPx(), chartHeight + 8.dp.toPx())
                )
            }
        }

        // 5. Draw the Chart Curve (within the chart area)
        val strokePath = Path().apply {
            moveTo(labelMarginX, getY(readings[0].powerKw))
            for (i in 0 until readings.size - 1) {
                val x1 = labelMarginX + (i * spacingX)
                val y1 = getY(readings[i].powerKw)
                val x2 = labelMarginX + ((i + 1) * spacingX)
                val y2 = getY(readings[i + 1].powerKw)

                cubicTo(
                    x1 + spacingX / 2f, y1,
                    x1 + spacingX / 2f, y2,
                    x2, y2
                )
            }
        }

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ZoomablePowerLineChart1(
    readings: List<EnergyReadingEntity>,
    modifier: Modifier = Modifier
) {
    if (readings.size < 2) return

    val color = MaterialTheme.colorScheme.primary
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // 🔍 Zoom & scroll state (DATA-based, not pixel-based)
    var scaleX by remember { mutableFloatStateOf(1f) } // 1 = full view
    var offsetIndex by remember { mutableFloatStateOf(0f) }

    val minScale = 1f
    val maxScale = 6f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(16.dp)
            .pointerInput(readings.size) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scaleX = (scaleX * zoom).coerceIn(minScale, maxScale)
                    offsetIndex -= pan.x / 50f // convert pixels → data space
                }
            }
    ) {
        /* ---------------- Layout ---------------- */

        val labelMarginX = 50.dp.toPx()
        val labelMarginY = 30.dp.toPx()

        val chartWidth = size.width - labelMarginX
        val chartHeight = size.height - labelMarginY

        /* ---------------- Viewport math ---------------- */

        val visibleCount = (readings.size / scaleX).coerceAtLeast(2f).toInt()

        val maxOffset =
            (readings.size - visibleCount).coerceAtLeast(0)

        offsetIndex = offsetIndex.coerceIn(0f, maxOffset.toFloat())

        val startIndex = offsetIndex.toInt()
        val endIndex = (startIndex + visibleCount).coerceAtMost(readings.size)

        val visibleReadings = readings.subList(startIndex, endIndex)

        val spacingX =
            chartWidth / (visibleReadings.size - 1).coerceAtLeast(1)

        /* ---------------- Y scale ---------------- */

        val maxPower =
            visibleReadings.maxOf { it.powerKw }.coerceAtLeast(1f)
        val minPower =
            visibleReadings.minOf { it.powerKw }

        fun getY(value: Float): Float {
            val range = (maxPower - minPower).coerceAtLeast(1f)
            return chartHeight - ((value - minPower) / range) * chartHeight
        }

        /* ---------------- Grid & Y labels ---------------- */

        val gridLines = 4
        for (i in 0..gridLines) {
            val y = (chartHeight / gridLines) * i
            val labelValue =
                maxPower - ((maxPower - minPower) / gridLines) * i

            drawLine(
                color = gridColor,
                start = Offset(labelMarginX, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )

            drawText(
                textMeasurer = textMeasurer,
                text = "%.1f kW".format(labelValue),
                style = labelStyle,
                topLeft = Offset(0f, y - 10.dp.toPx())
            )
        }

        /* ---------------- X labels ---------------- */

        val labelFrequency =
            (visibleReadings.size / 4).coerceAtLeast(1)

        visibleReadings.forEachIndexed { index, reading ->
            if (index % labelFrequency == 0 || index == visibleReadings.lastIndex) {
                val x = labelMarginX + index * spacingX
                drawText(
                    textMeasurer = textMeasurer,
                    text = reading.timestamp.format(timeFormatter),
                    style = labelStyle,
                    topLeft = Offset(x - 20.dp.toPx(), chartHeight + 8.dp.toPx())
                )
            }
        }

        /* ---------------- Curve ---------------- */

        val path = Path().apply {
            moveTo(
                labelMarginX,
                getY(visibleReadings.first().powerKw)
            )

            for (i in 0 until visibleReadings.lastIndex) {
                val x1 = labelMarginX + i * spacingX
                val y1 = getY(visibleReadings[i].powerKw)
                val x2 = labelMarginX + (i + 1) * spacingX
                val y2 = getY(visibleReadings[i + 1].powerKw)

                cubicTo(
                    x1 + spacingX / 2f, y1,
                    x1 + spacingX / 2f, y2,
                    x2, y2
                )
            }
        }

        drawPath(
            path,
            color,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun ZoomablePowerLineChart(
    readings: List<EnergyReadingEntity>,
    modifier: Modifier = Modifier
) {
    if (readings.size < 2) return

    val color = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    val tooltipStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.onPrimary
    )
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val textMeasurer = rememberTextMeasurer()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // 🔍 Zoom & scroll state (DATA-based, not pixel-based)
    var scaleX by remember { mutableFloatStateOf(1f) } // 1 = full view
    var offsetIndex by remember { mutableFloatStateOf(0f) }
    var selectedIndex by remember { mutableIntStateOf(-1) }

    val minScale = 1f
    val maxScale = 6f

    // We calculate these once per recomposition so pointerInput AND Canvas can see them
    val density = LocalDensity.current
    val labelMarginX = with(density) { 50.dp.toPx() }
    val labelMarginY = with(density) { 30.dp.toPx() }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        val chartWidth = constraints.maxWidth.toFloat() - labelMarginX
        val chartHeight = constraints.maxHeight.toFloat() - labelMarginY

        // Calculate Viewport/Visible Data here
        val visibleCount = (readings.size / scaleX).coerceAtLeast(2f).toInt()
        val safeOffset = offsetIndex.coerceIn(0f, (readings.size - visibleCount).toFloat())
        val startIndex = safeOffset.toInt()
        val endIndex = (startIndex + visibleCount).coerceAtMost(readings.size)
        val visibleReadings = readings.subList(startIndex, endIndex)

        val spacingX = chartWidth / (visibleReadings.size - 1).coerceAtLeast(1)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(readings.size) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scaleX = (scaleX * zoom).coerceIn(minScale, maxScale)
                        offsetIndex -= pan.x / 50f // convert pixels → data space
                    }
                }
                .pointerInput(visibleReadings.size) {
                    detectTapGestures { offset ->
                        val x = offset.x - labelMarginX
                        if (x >= 0) {
                            val index = (x / spacingX).roundToInt()
                            selectedIndex = index
                                .coerceIn(0, visibleReadings.lastIndex)
                        }
                    }
                }

        ) {
            /* ---------------- Y scale ---------------- */

            val maxPower =
                visibleReadings.maxOf { it.powerKw }.coerceAtLeast(1f)
            val minPower =
                visibleReadings.minOf { it.powerKw }

            fun getY(value: Float): Float {
                val range = (maxPower - minPower).coerceAtLeast(1f)
                return chartHeight - ((value - minPower) / range) * chartHeight
            }

            /* ---------------- Grid & Y labels ---------------- */

            val gridLines = 4
            for (i in 0..gridLines) {
                val y = (chartHeight / gridLines) * i
                val labelValue =
                    maxPower - ((maxPower - minPower) / gridLines) * i

                drawLine(
                    color = gridColor,
                    start = Offset(labelMarginX, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = "%.1f kW".format(labelValue),
                    style = labelStyle,
                    topLeft = Offset(0f, y - 10.dp.toPx())
                )
            }

            /* ---------------- X labels ---------------- */

            val labelFrequency =
                (visibleReadings.size / 4).coerceAtLeast(1)

            visibleReadings.forEachIndexed { index, reading ->
                if (index % labelFrequency == 0 || index == visibleReadings.lastIndex) {
                    val x = labelMarginX + index * spacingX
                    drawText(
                        textMeasurer = textMeasurer,
                        text = reading.timestamp.format(timeFormatter),
                        style = labelStyle,
                        topLeft = Offset(x - 20.dp.toPx(), chartHeight + 8.dp.toPx())
                    )
                }
            }

            /* ---------------- Curve ---------------- */

            val path = Path().apply {
                moveTo(
                    labelMarginX,
                    getY(visibleReadings.first().powerKw)
                )

                for (i in 0 until visibleReadings.lastIndex) {
                    val x1 = labelMarginX + i * spacingX
                    val y1 = getY(visibleReadings[i].powerKw)
                    val x2 = labelMarginX + (i + 1) * spacingX
                    val y2 = getY(visibleReadings[i + 1].powerKw)

                    cubicTo(
                        x1 + spacingX / 2f, y1,
                        x1 + spacingX / 2f, y2,
                        x2, y2
                    )
                }
            }

            drawPath(
                path,
                color,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            if (selectedIndex in visibleReadings.indices) {
                val reading = visibleReadings[selectedIndex]

                val x = labelMarginX + selectedIndex * spacingX
                val y = getY(reading.powerKw)

                // Vertical indicator
                drawLine(
                    color = color.copy(alpha = 0.3f),
                    start = Offset(x, 0f),
                    end = Offset(x, chartHeight),
                    strokeWidth = 1.dp.toPx()
                )

                // Highlight point
                drawCircle(
                    color = color,
                    radius = 6.dp.toPx(),
                    center = Offset(x, y)
                )

                /* ---------- Tooltip ---------- */

                val tooltipText =
                    "${reading.timestamp.format(timeFormatter)}\n" +
                            "%.2f kW".format(reading.powerKw)

                val textLayout = textMeasurer.measure(
                    text = tooltipText,
                    style = tooltipStyle
                )

                val padding = 8.dp.toPx()
                val tooltipWidth = textLayout.size.width + padding * 2
                val tooltipHeight = textLayout.size.height + padding * 2

                val tooltipX =
                    (x - tooltipWidth / 2)
                        .coerceIn(labelMarginX, size.width - tooltipWidth)

                val tooltipY =
                    (y - tooltipHeight - 12.dp.toPx())
                        .coerceAtLeast(0f)

                drawRoundRect(
                    color = color.copy(alpha = 0.9f),
                    topLeft = Offset(tooltipX, tooltipY),
                    size = Size(tooltipWidth, tooltipHeight),
                    cornerRadius = CornerRadius(12.dp.toPx())
                )

                drawText(
                    textMeasurer = textMeasurer,
                    text = tooltipText,
                    style = tooltipStyle,
                    topLeft = Offset(
                        tooltipX + padding,
                        tooltipY + padding
                    )
                )
            }
        }
    }
}

@Composable
fun PowerLineChart2(
    readings: List<EnergyReadingEntity>,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    if (readings.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Insufficient data", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    val maxPower = readings.maxOf { it.powerKw }.coerceAtLeast(1f)
    val minPower = readings.minOf { it.powerKw }

    // Formatting for the time labels (e.g., 14:30)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 16.dp, vertical = 24.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacingX = width / (readings.size - 1)

        // Helper to calculate Y position
        fun getY(value: Float): Float {
            val range = (maxPower - minPower).coerceAtLeast(1f)
            return height - ((value - minPower) / range) * height
        }

        // 1. Draw Horizontal Grid Lines & Y-Axis Labels
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = (height / gridLines) * i
            val labelValue = maxPower - ((maxPower - minPower) / gridLines) * i

            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
            // Note: For actual text labels in Canvas, use drawContext.canvas.nativeCanvas.drawText
            // or the newer Compose drawText API if available.
        }

        // 2. Create the Path for the Curve
        val strokePath = Path().apply {
            val startY = getY(readings[0].powerKw)
            moveTo(0f, startY)

            for (i in 0 until readings.size - 1) {
                val x1 = i * spacingX
                val y1 = getY(readings[i].powerKw)
                val x2 = (i + 1) * spacingX
                val y2 = getY(readings[i + 1].powerKw)

                // Cubic Bezier for smooth curves
                cubicTo(
                    x1 + spacingX / 2f, y1,
                    x1 + spacingX / 2f, y2,
                    x2, y2
                )
            }
        }

        // 3. Draw the Gradient Fill
        val fillPath = android.graphics.Path(strokePath.asAndroidPath()).asComposePath().apply {
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // 4. Draw the main Line
        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // 5. Draw Data Points (Circles)
        readings.forEachIndexed { index, reading ->
            drawCircle(
                color = color,
                radius = 4.dp.toPx(),
                center = Offset(index * spacingX, getY(reading.powerKw))
            )
        }
    }
}

@Composable
fun PowerLineChart1(
    readings: List<EnergyReadingEntity>,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    if (readings.isEmpty()) return

    val maxPower = readings.maxOf { it.powerKw }
    val minPower = readings.minOf { it.powerKw }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        val stepX = size.width / (readings.size - 1).coerceAtLeast(1)

        fun yFor(value: Float): Float {
            return size.height -
                    ((value - minPower) / (maxPower - minPower)) * size.height
        }

        for (i in 0 until readings.lastIndex) {
            val start = Offset(
                x = i * stepX,
                y = yFor(readings[i].powerKw)
            )
            val end = Offset(
                x = (i + 1) * stepX,
                y = yFor(readings[i + 1].powerKw)
            )

            drawLine(
                color = color,
                start = start,
                end = end,
                strokeWidth = 4f
            )
        }
    }
}


@Composable
fun IconBox(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Int = 40,
    contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.3f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon, contentDescription = contentDescription, tint = color,
            modifier = Modifier.size((size * (0.6)).dp)
//            modifier = Modifier.size(24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionTextField(
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    OutlinedTextField(
        value = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        label = { Text(label) },
        shape = RoundedCornerShape(16.dp),
        trailingIcon = {
            Icon(
                Icons.Default.DateRange,
                contentDescription = "Select Date"
            )
        },
        readOnly = true,
        enabled = false
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate())
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterLocationSelector(
    locations: List<MeterLocationEntity>,
    selectedLocation: MeterLocationEntity?,
    onLocationSelected: (MeterLocationEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Use a Read-only TextField to act as the dropdown trigger
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            // Display the selected location's info or a placeholder
            value = selectedLocation?.let { "${it.building} - ${it.wing}" } ?: "Select Location",
            onValueChange = {},
            readOnly = true,
            label = { Text("Meter Location") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .menuAnchor() // Important for alignment
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            locations.forEach { location ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = "${location.building} (Wing ${location.wing})",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "ID: ${location.meterId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onLocationSelected(location)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
fun ChartRangeSelector(
    selected: ChartRange,
    onSelected: (ChartRange) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ChartRange.values().forEach { range ->
            FilterChip(
                selected = range == selected,
                onClick = { onSelected(range) },
                label = { Text(range.label) }
            )
        }
    }
}
