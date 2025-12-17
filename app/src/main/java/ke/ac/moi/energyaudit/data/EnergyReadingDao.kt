package ke.ac.moi.energyaudit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EnergyReadingDao {

    @Insert
    suspend fun insert(reading: EnergyReadingEntity)

    @Query(
        """
        SELECT * FROM energy_readings
        WHERE meter_id = :meterId
        ORDER BY timestamp DESC
        LIMIT :limit
    """
    )
    suspend fun getLatestReadings(
        meterId: String,
        limit: Int = 20
    ): List<EnergyReadingEntity>

    @Query(
        """
        SELECT AVG(power_kw) FROM energy_readings
        WHERE meter_id = :meterId
    """
    )
    suspend fun getAveragePower(meterId: String): Float?
}
