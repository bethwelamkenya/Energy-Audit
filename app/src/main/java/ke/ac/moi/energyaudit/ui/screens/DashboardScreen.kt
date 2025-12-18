package ke.ac.moi.energyaudit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.ui.viewmodel.EnergyViewModel

@Composable
fun DashboardScreen(modifier: Modifier = Modifier, viewModel: EnergyViewModel = viewModel(), onMeterClick: (MeterLocationEntity) -> Unit) {
    val meters by viewModel.meters.collectAsStateWithLifecycle()
    Box(modifier = modifier){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
//        items(readings.values.toList()) { meter ->
//            readings[meter.meterId]?.let { reading ->
//                MeterCard(
//                    meter = meter,
//                    reading = reading,
//                    inefficient = viewModel.isInefficient(reading)
//                )
//            }
//        }
            items(items = meters){
                MeterCard(
                    meter = it,
                    viewModel = viewModel,
                    onClick = { onMeterClick(it) }
                )
            }
        }
// Place FAB manually at the same height as bottom bar
//        Box(
//            modifier = Modifier
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 30.dp)
//        ){
//            IconButton(onClick = {
//
//            }) {
//                IconBox(
//                    icon = Icons.Default.Add,
//                    size = 60
//                )
//            }
//        }
    }

}
