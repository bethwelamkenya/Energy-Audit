package ke.ac.moi.energyaudit.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Report
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val title: String, val route: String, val icon: ImageVector) {
    object Dashboard : Screen("Energy Audit","dashboard", Icons.Default.Home)
    object Analytics : Screen("Meter Analytics","analytics", Icons.Default.Analytics)
    object AddMeter : Screen("Add New Meter","addMeter", Icons.Default.Add)
    object Reports : Screen("System Report","reports", Icons.Default.Report)

    companion object{
        val screens = listOf(Dashboard, Analytics, Reports, AddMeter)
    }
}
