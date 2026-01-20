package ke.ac.moi.energyaudit.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.ac.moi.energyaudit.data.AggregatedReading
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.repository.EnergyRepository
import ke.ac.moi.energyaudit.utils.FakeEnergySensor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EnergyViewModel(
    private val repository: EnergyRepository
) : ViewModel() {

    private val _meters = MutableStateFlow<List<MeterLocationEntity>>(emptyList())
    val meters: StateFlow<List<MeterLocationEntity>> = _meters

    // Level options
    val levels = listOf("Wing", "Block", "Building")
    private val _selectedLevel = MutableStateFlow(levels.first())
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    init {
        viewModelScope.launch {

            repository.seedMetersIfNeeded()
            _meters.value = repository.getAllMeters()

            while (true) {
                _meters.value.forEach {
                    repository.insertSimulatedReading(it.meterId)
                }
                delay(30000)
            }
        }
    }

    fun addMeter(meter: MeterLocationEntity) {
        viewModelScope.launch {
            repository.addMeter(meter)
            _meters.value = repository.getAllMeters()
        }
    }

    fun removeMeter(meter: MeterLocationEntity) {
        viewModelScope.launch {
            repository.removeMeter(meter)
            _meters.value = repository.getAllMeters()
        }
    }


    fun latestReading(meterId: String) =
        repository.observeLatestReading(meterId)

    fun averagePower(meterId: String) =
        repository.observeAveragePower(meterId)

    fun generateRecommendation(reading: EnergyReadingEntity): String =
        repository.generateRecommendation(reading)

    fun isInefficient(reading: EnergyReadingEntity): Boolean {
        return reading.powerKw > 15f
    }


    fun selectLevel(level: String) {
        _selectedLevel.value = level
    }

    // Aggregated readings exposed as Flow
    val aggregatedReadings: StateFlow<List<AggregatedReading>> =
        combine(repository.observeAllReadings(), selectedLevel) { readings, level ->
            aggregateByLevel(readings, level)
        }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Function to aggregate by level
    private fun aggregateByLevel(
        readings: List<EnergyReadingEntity>,
        level: String
    ): List<AggregatedReading> {

        // Create a lookup map
        val meterMap = _meters.value.associateBy { it.meterId }

        return readings.groupBy { reading ->
            val meter = meterMap[reading.meterId] ?: error("Meter not found: ${reading.meterId}")
            meterIdToLevel(reading.meterId, meter, level)
        }.map { (groupId, groupReadings) ->
            val totalPower = groupReadings.sumOf { it.powerKw.toDouble() }.toFloat()
            val avgVoltage = groupReadings.map { it.voltage }.average().toFloat()
            val totalCurrent = groupReadings.sumOf { it.current.toDouble() }.toFloat()
            val since = groupReadings.minOf { it.timestamp }
            val lastUpdated = groupReadings.maxOf { it.timestamp }

            AggregatedReading(
                level = groupId,
                totalPowerKw = totalPower,
                avgVoltage = avgVoltage,
                totalCurrent = totalCurrent,
                since = since,
                lastUpdated = lastUpdated
            )
        }.sortedBy { it.level }
    }

    // Helper to extract hierarchy from meterId
    private fun meterIdToLevel(
        meterId: String,
        meter: MeterLocationEntity,
        level: String
    ): String {
        return when (level) {
            "Building" -> meterId.split("-").first()      // first part of ID, e.g., ADM
            "Block" -> "${meterId.split("-").first()}-${meter.block}"  // ADM-B1
            "Wing" -> meter.meterId                       // full meter ID
            else -> meter.meterId
        }
    }
}

