package ke.ac.moi.energyaudit.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import ke.ac.moi.energyaudit.R
import ke.ac.moi.energyaudit.EnergyAuditApplication
import ke.ac.moi.energyaudit.data.EnergyReadingEntity
import ke.ac.moi.energyaudit.data.MeterLocationEntity

object NotificationHelper {

    fun canPostNotifications(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showVoltageAlert(
        context: Context,
        meter: MeterLocationEntity,
        reading: EnergyReadingEntity
    ) {
        if (!canPostNotifications(context)) {
            return
        }

        val notification = NotificationCompat.Builder(
            context,
            EnergyAuditApplication.ENERGY_ALERTS_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.untitled1)
            .setContentTitle("Voltage Alert")
            .setContentText("Abnormal power consumption detected from ${meter.building} Wing ${meter.wing}: ${reading.powerKw} kW at ${reading.timestamp.toReadableString()}")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}
