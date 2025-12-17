package ke.ac.moi.energyaudit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "energy_readings",
    foreignKeys = [
        ForeignKey(
            entity = MeterLocationEntity::class,
            parentColumns = ["meter_id"],
            childColumns = ["meter_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meter_id")]
)
data class EnergyReadingEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reading_id")
    val readingId: Long = 0,

    @ColumnInfo(name = "meter_id")
    val meterId: String,

    @ColumnInfo(name = "power_kw")
    val powerKw: Float,

    val voltage: Float,
    val current: Float,

    val timestamp: LocalDateTime
)

