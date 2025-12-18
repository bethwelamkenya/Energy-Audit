package ke.ac.moi.energyaudit.ui.viewmodel

import ke.ac.moi.energyaudit.repository.EnergyRepository
import androidx.lifecycle.ViewModel
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ChartViewModel(
    private val repository: EnergyRepository
) : ViewModel() {

    fun chartData(meterId: String): Flow<List<EnergyReadingEntity>> =
        repository.observeChartData(meterId)

    fun chartData(
        meterId: String,
        limit: Int
    ): Flow<List<EnergyReadingEntity>> =
        repository.observeRecentReadings(meterId, limit)

    fun readingsSince(
        meterId: String,
        since: LocalDateTime
    ): Flow<List<EnergyReadingEntity>> =
        repository.observeReadingsSince(meterId, since)
}
