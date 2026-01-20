package ke.ac.moi.energyaudit.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.repository.EnergyRepository
import ke.ac.moi.energyaudit.utils.FakeEnergySensor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EnergyViewModel(
    private val repository: EnergyRepository
) : ViewModel() {

    private val _meters = MutableStateFlow<List<MeterLocationEntity>>(emptyList())
    val meters: StateFlow<List<MeterLocationEntity>> = _meters

    private val _loop = MutableStateFlow<Boolean>(true)

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
}
