package ke.ac.moi.energyaudit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "meter_locations")
data class MeterLocationEntity(
    @PrimaryKey
    @ColumnInfo(name = "meter_id")
    val meterId: String,

    val building: String,
    val wing: String,
    val latitude: Float,
    val longitude: Float,

    @ColumnInfo(name = "installed_date")
    val installedDate: LocalDate
)

