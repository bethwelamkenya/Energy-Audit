package ke.ac.moi.energyaudit.repository

import ke.ac.moi.energyaudit.data.EnergyReadingDao
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationDao
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.utils.FakeEnergySensor
import ke.ac.moi.energyaudit.utils.SeedData
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

class EnergyRepository(
    private val meterDao: MeterLocationDao,
    private val readingDao: EnergyReadingDao
) {

    private val sensor = FakeEnergySensor()

    /* -----------------------------
       Static meter data
    ------------------------------*/

    suspend fun seedMeters(meters: List<MeterLocationEntity>) {
        meterDao.insertAll(meters)
    }

    suspend fun addMeter(meter: MeterLocationEntity) {
        meterDao.insert(meter)
    }

    suspend fun seedMetersIfNeeded() {
        val count = meterDao.count()
        if (count == 0) {
            meterDao.insertAll(SeedData.meters)
        }
    }

    suspend fun getAllMeters(): List<MeterLocationEntity> =
        meterDao.getAllMeters()

    /* -----------------------------
       Real-time ingestion
    ------------------------------*/

    suspend fun insertSimulatedReading(meterId: String) {
        val reading = sensor.generate(meterId)

        readingDao.insert(
            EnergyReadingEntity(
                meterId = meterId,
                powerKw = reading.powerKw,
                voltage = reading.voltage,
                current = reading.current,
                timestamp = reading.timestamp
            )
        )
    }

    /* -----------------------------
       Live data streams (UI)
    ------------------------------*/

    fun observeLatestReading(meterId: String): Flow<EnergyReadingEntity?> =
        readingDao.observeLatestReading(meterId)

    fun observeRecentReadings(
        meterId: String,
        limit: Int = 20
    ): Flow<List<EnergyReadingEntity>> =
        readingDao.observeRecentReadings(meterId, limit)


    fun observeChartData(
        meterId: String,
        limit: Int = 50
    ): Flow<List<EnergyReadingEntity>> =
        readingDao.observeReadingsForChart(meterId, limit)

    fun observeReadingsSince(
        meterId: String,
        since: LocalDateTime,
    ): Flow<List<EnergyReadingEntity>> =
        readingDao.observeReadingsSince(meterId, since)

    fun observeAveragePower(meterId: String): Flow<Float?> =
        readingDao.observeAveragePower(meterId)

    /* -----------------------------
       Analytics & KPIs
    ------------------------------*/

    suspend fun getDailyPeakPower(meterId: String): Float? {
        val since = LocalDateTime.now().minusDays(1)
        return readingDao.getPeakPower(meterId, since)
    }

    fun isInefficient(reading: EnergyReadingEntity): Boolean {
        return reading.powerKw > 15f
    }

    fun generateRecommendation(reading: EnergyReadingEntity): String {
        return when {
            reading.powerKw > 20f ->
                "Reduce HVAC load or shut down unused zones."

            reading.voltage < 210 ->
                "Voltage drop detected. Inspect power lines."

            reading.current > 35 ->
                "High current draw. Possible equipment overload."

            else ->
                "Energy usage within optimal range."
        }
    }
}

