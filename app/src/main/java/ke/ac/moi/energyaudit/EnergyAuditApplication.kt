package ke.ac.moi.energyaudit

import android.app.Application
import ke.ac.moi.energyaudit.data.EnergyDatabase
import ke.ac.moi.energyaudit.repository.EnergyRepository

class EnergyAuditApplication : Application() {
    // Using 'lazy' ensures the database and repository are only created when they're first needed.
    val database by lazy { EnergyDatabase.getInstance(this) }
    val repository by lazy { EnergyRepository(database.meterLocationDao(), database.energyReadingDao()) }
}
