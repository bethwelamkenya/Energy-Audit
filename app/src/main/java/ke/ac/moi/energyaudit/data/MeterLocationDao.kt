package ke.ac.moi.energyaudit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MeterLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meters: List<MeterLocationEntity>)

    @Query("SELECT * FROM meter_locations")
    suspend fun getAllMeters(): List<MeterLocationEntity>
}
