package ke.ac.moi.energyaudit.ui.viewmodel


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ke.ac.moi.energyaudit.repository.EnergyRepository

/**
 * A Factory class is needed to create a ViewModel instance with constructor parameters.
 * This factory will provide the EnergyRepository to the EnergyViewModel.
 */
class EnergyViewModelFactory(private val repository: EnergyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EnergyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EnergyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}