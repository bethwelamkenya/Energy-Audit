package ke.ac.moi.energyaudit.utils

import android.os.Build
import androidx.annotation.RequiresApi
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import java.time.LocalDateTime

class FakeEnergySensor {

    fun generate(meterId: String): EnergyReadingEntity {
        val voltage = (210..240).random().toFloat()
        val current = (5..40).random().toFloat()

        return EnergyReadingEntity(
            meterId = meterId,
            powerKw = (voltage * current) / 1000f,
            voltage = voltage,
            current = current,
            timestamp = LocalDateTime.now()
        )
    }
}
