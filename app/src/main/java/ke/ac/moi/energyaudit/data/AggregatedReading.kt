package ke.ac.moi.energyaudit.data

import java.time.LocalDateTime

data class AggregatedReading(
    val level: String,       // e.g., ADM-B1, ADM, ADM+SCI
    val totalPowerKw: Float,
    val avgVoltage: Float,
    val totalCurrent: Float,
    val since: LocalDateTime,
    val lastUpdated: LocalDateTime
)
