package ke.ac.moi.energyaudit.utils

import ke.ac.moi.energyaudit.data.ChartStats
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.ac.moi.energyaudit.data.AggregatedReading
import ke.ac.moi.energyaudit.data.ChartRange
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.data.PowerMetrics
import java.io.OutputStreamWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.Random
import kotlin.math.sqrt

fun exportEnergyReadingsToCsv(
    resolver: ContentResolver,
    uri: Uri,
    readings: List<EnergyReadingEntity>
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    resolver.openOutputStream(uri)?.use { output ->
        OutputStreamWriter(output).use { writer ->
            // Header
            writer.appendLine("Timestamp,Power (kW),Voltage,Current")

            readings.forEach { r ->
                writer.appendLine(
                    "${r.timestamp.format(formatter)}," +
                            "${r.powerKw}," +
                            "${r.voltage}," +
                            "${r.current}"
                )
            }
        }
    }
}

fun saveBitmapAsPng(
    resolver: ContentResolver,
    uri: Uri,
    bitmap: Bitmap
) {
    resolver.openOutputStream(uri)?.use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}

fun exportPdfReport(
    context: Context,
    uri: Uri,
    chartBitmap: Bitmap,
    stats: ChartStats,
    meter: MeterLocationEntity,
    range: ChartRange
) {
    val document = PdfDocument()

    val pageInfo = PdfDocument.PageInfo
        .Builder(595, 842, 1) // A4
        .create()

    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    var y = 40

    // Title
    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText("Energy Usage Report", 40f, y.toFloat(), paint)

    y += 40
    paint.textSize = 12f
    paint.isFakeBoldText = false

    canvas.drawText("Meter: ${meter.building} - ${meter.wing}", 40f, y.toFloat(), paint)
    y += 20
    canvas.drawText("Range: ${range.label}", 40f, y.toFloat(), paint)

    // Chart
    y += 30
    val chartHeight = 300
    val scaledChart = Bitmap.createScaledBitmap(
        chartBitmap,
        pageInfo.pageWidth - 80,
        chartHeight,
        true
    )
    canvas.drawBitmap(scaledChart, 40f, y.toFloat(), null)

    y += chartHeight + 30

    // Stats
    canvas.drawText("Average: ${stats.avg} kW", 40f, y.toFloat(), paint)
    y += 20
    canvas.drawText("Peak: ${stats.max} kW", 40f, y.toFloat(), paint)
    y += 20
    canvas.drawText("Minimum: ${stats.min} kW", 40f, y.toFloat(), paint)

    document.finishPage(page)

    context.contentResolver.openOutputStream(uri)?.use {
        document.writeTo(it)
    }

    document.close()
}



@OptIn(ExperimentalTextApi::class)
@Composable
fun DrawScope.drawPowerChart1(
    readings: List<EnergyReadingEntity>,
    color: Color,
    gridColor: Color,
    labelStyle: TextStyle,
    textMeasurer: TextMeasurer
) {
    // ⬅ reuse your existing Canvas drawing logic here
    // NO pointerInput, NO state
    if (readings.size < 2) return

    val maxPower = readings.maxOf { it.powerKw }.coerceAtLeast(1f)
    val minPower = readings.minOf { it.powerKw }
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    Canvas(
        modifier = Modifier
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
fun DrawScope.drawPowerChart(
    readings: List<EnergyReadingEntity>,
    color: Color,
    gridColor: Color,
    labelStyle: TextStyle,
    textMeasurer: TextMeasurer
) {
    if (readings.size < 2) return

    val labelMarginX = 50.dp.toPx()
    val labelMarginY = 30.dp.toPx()

    val chartWidth = size.width - labelMarginX
    val chartHeight = size.height - labelMarginY

    val maxPower = readings.maxOf { it.powerKw }.coerceAtLeast(1f)
    val minPower = readings.minOf { it.powerKw }

    val spacingX = chartWidth / (readings.size - 1)
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun getY(value: Float): Float {
        val range = (maxPower - minPower).coerceAtLeast(1f)
        return chartHeight - ((value - minPower) / range) * chartHeight
    }

    /* ---------- Grid & Y labels ---------- */

    val gridLines = 4
    repeat(gridLines + 1) { i ->
        val y = (chartHeight / gridLines) * i
        val value =
            maxPower - ((maxPower - minPower) / gridLines) * i

        drawLine(
            color = gridColor,
            start = Offset(labelMarginX, y),
            end = Offset(size.width, y),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
        )

        drawText(
            textMeasurer,
            "%.1f kW".format(value),
            style = labelStyle,
            topLeft = Offset(0f, y - 10.dp.toPx())
        )
    }

    /* ---------- X labels ---------- */

    val labelFrequency = (readings.size / 4).coerceAtLeast(1)
    readings.forEachIndexed { index, reading ->
        if (index % labelFrequency == 0 || index == readings.lastIndex) {
            val x = labelMarginX + index * spacingX
            drawText(
                textMeasurer,
                reading.timestamp.format(timeFormatter),
                style = labelStyle,
                topLeft = Offset(
                    x - 20.dp.toPx(),
                    chartHeight + 8.dp.toPx()
                )
            )
        }
    }

    /* ---------- Curve ---------- */

    val path = Path().apply {
        moveTo(labelMarginX, getY(readings.first().powerKw))
        for (i in 0 until readings.lastIndex) {
            val x1 = labelMarginX + i * spacingX
            val y1 = getY(readings[i].powerKw)
            val x2 = labelMarginX + (i + 1) * spacingX
            val y2 = getY(readings[i + 1].powerKw)

            cubicTo(
                x1 + spacingX / 2f, y1,
                x1 + spacingX / 2f, y2,
                x2, y2
            )
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(3.dp.toPx(), cap = StrokeCap.Round)
    )
}


fun renderChartBitmap(
    context: Context,
    readings: List<EnergyReadingEntity>,
    width: Int = 1080,
    height: Int = 600,
    textMeasurer: TextMeasurer,
): Bitmap {
    val imageBitmap = ImageBitmap(width, height)
    val canvas = Canvas(imageBitmap)

    val density = context.resources.displayMetrics.density
    val drawScope = CanvasDrawScope()

    drawScope.draw(
        density = Density(density),
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = Size(width.toFloat(), height.toFloat())
    ) {
        drawPowerChart(
            readings = readings,
            color = Color(0xFF4CAF50),
            labelStyle = TextStyle(fontSize = 12.sp),
            textMeasurer = textMeasurer,
            gridColor = Color.Gray
        )
    }

    return imageBitmap.asAndroidBitmap()
}


fun computeStats(readings: List<EnergyReadingEntity>): ChartStats? {
    if (readings.isEmpty()) return null

    val powers = readings.map { it.powerKw }
    return ChartStats(
        min = powers.min(),
        max = powers.max(),
        avg = powers.average().toFloat()
    )
}

fun LocalDateTime.toReadableString(): String {
    val formatter = DateTimeFormatter.ofPattern(
        "EEE, dd MMM yyyy, HH:mm",
        Locale.getDefault()
    )
    return this.format(formatter)
}

fun calculatePowerMetrics(
    voltage: Double,
    current: Double,
    powerFactor: Double
): PowerMetrics {
    val apparentPower = (voltage * current) / 1000.0
    val realPower = apparentPower * powerFactor
    val reactivePower = sqrt(
        apparentPower * apparentPower - realPower * realPower
    )

    return PowerMetrics(
        realPowerKw = realPower,
        reactivePowerKvar = reactivePower,
        apparentPowerKva = apparentPower,
        powerFactor = powerFactor
    )
}

fun gaussianPowerFactor(): Double {
    val pf = 0.92 + Random().nextGaussian() * 0.03
    return pf.coerceIn(0.7, 1.0)
}

fun parseMeterId(meterId: String): Triple<String, String, String> {
    // Example: ADM-B1_WA
    val parts = meterId.split("-", "_")
    // ["ADM", "B1", "WA"]
    return Triple(parts[0], parts[1], parts[2])
}

//fun aggregateByLevel(readings: List<EnergyReadingEntity>, level: (String) -> String): List<AggregatedReading> {
//    return readings.groupBy { level(it.meterId) }.map { (groupId, groupReadings) ->
//        val totalPower = groupReadings.sumOf { it.powerKw.toDouble() }.toFloat()
//        val avgVoltage = groupReadings.map { it.voltage }.average().toFloat()
//        val totalCurrent = groupReadings.sumOf { it.current.toDouble() }.toFloat()
//        val lastUpdated = groupReadings.maxOf { it.timestamp }
//        AggregatedReading(groupId, totalPower, avgVoltage, totalCurrent, lastUpdated)
//    }
//}

