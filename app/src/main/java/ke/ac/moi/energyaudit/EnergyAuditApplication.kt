package ke.ac.moi.energyaudit

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import ke.ac.moi.energyaudit.data.EnergyDatabase
import ke.ac.moi.energyaudit.repository.EnergyRepository

class EnergyAuditApplication : Application() {
    // Using 'lazy' ensures the database and repository are only created when they're first needed.
    val database by lazy { EnergyDatabase.getInstance(this) }
    val repository by lazy { EnergyRepository(database.meterLocationDao(), database.energyReadingDao()) }


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            ENERGY_ALERTS_CHANNEL_ID,
            "Energy Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Voltage and energy monitoring alerts"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val ENERGY_ALERTS_CHANNEL_ID = "energy_alerts"
    }
}
