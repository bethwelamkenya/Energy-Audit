package ke.ac.moi.energyaudit.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.repository.EnergyRepository
import ke.ac.moi.energyaudit.utils.FakeEnergySensor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
class EnergyViewModel(
    private val repository: EnergyRepository
) : ViewModel() {

    private val sensor = FakeEnergySensor()

    private val _readings =
        MutableStateFlow<Map<String, EnergyReadingEntity>>(emptyMap())
    val readings: StateFlow<Map<String, EnergyReadingEntity>> = _readings

    init {
        viewModelScope.launch {
            while (true) {
                readings.forEach {
                    repository.insertSimulatedReading(it.meterId)
                }
                delay(2000)
            }
        }
    }

    fun isInefficient(reading: EnergyReadingEntity): Boolean {
        return reading.powerKw > 15f
    }
}
