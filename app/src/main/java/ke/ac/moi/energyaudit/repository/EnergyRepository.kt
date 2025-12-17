package ke.ac.moi.energyaudit.repository

import android.os.Build
import androidx.annotation.RequiresApi
import ke.ac.moi.energyaudit.data.EnergyReadingDao
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.utils.FakeEnergySensor

class EnergyRepository(
    private val energyDao: EnergyReadingDao
) {
    private val sensor = FakeEnergySensor()

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertSimulatedReading(meterId: String) {
        val reading = sensor.generate(meterId)

        energyDao.insert(
            EnergyReadingEntity(
                meterId = reading.meterId,
                powerKw = reading.powerKw,
                voltage = reading.voltage,
                current = reading.current,
                timestamp = reading.timestamp
            )
        )
    }
}
