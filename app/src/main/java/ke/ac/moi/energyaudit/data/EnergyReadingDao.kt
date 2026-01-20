package ke.ac.moi.energyaudit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface EnergyReadingDao {

    @Insert
    suspend fun insert(reading: EnergyReadingEntity)

    @Query("DELETE FROM energy_readings WHERE meter_id = :meterId")
    suspend fun deleteAllByMeterId(meterId: String)

    @Query("SELECT * FROM energy_readings")
    fun observeAllReadings(): Flow<List<EnergyReadingEntity>>

    @Query("""
        SELECT * FROM energy_readings
        WHERE meter_id = :meterId
        ORDER BY timestamp DESC
        LIMIT 1
    """)
    fun observeLatestReading(meterId: String): Flow<EnergyReadingEntity?>

    @Query("""
        SELECT * FROM energy_readings
        WHERE meter_id = :meterId
        ORDER BY timestamp DESC
        LIMIT :limit
    """)
    fun observeRecentReadings(
        meterId: String,
        limit: Int = 20
    ): Flow<List<EnergyReadingEntity>>

    @Query("""
        SELECT * FROM energy_readings
        WHERE meter_id = :meterId
        ORDER BY timestamp ASC
        LIMIT :limit
    """)
    fun observeReadingsForChart(
        meterId: String,
        limit: Int = 50
    ): Flow<List<EnergyReadingEntity>>

    @Query("""
    SELECT * FROM energy_readings
    WHERE meter_id = :meterId
      AND timestamp >= :since
    ORDER BY timestamp ASC
""")
    fun observeReadingsSince(
        meterId: String,
        since: LocalDateTime
    ): Flow<List<EnergyReadingEntity>>


    @Query("""
        SELECT AVG(power_kw) FROM energy_readings
        WHERE meter_id = :meterId
    """)
    fun observeAveragePower(meterId: String): Flow<Float?>

    @Query("""
        SELECT MAX(power_kw) FROM energy_readings
        WHERE meter_id = :meterId
        AND timestamp >= :since
    """)
    suspend fun getPeakPower(
        meterId: String,
        since: LocalDateTime
    ): Float?
}

