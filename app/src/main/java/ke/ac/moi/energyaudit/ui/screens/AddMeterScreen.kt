package ke.ac.moi.energyaudit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ke.ac.moi.energyaudit.ui.theme.EnergyAuditTheme
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeterScreen(
    onAddMeter: (String, String, String, Float, Float, LocalDate) -> Unit
) {
    var meterId by remember { mutableStateOf("") }
    var building by remember { mutableStateOf("") }
    var wing by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter Meter Details",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = meterId,
            onValueChange = { meterId = it },
            label = { Text("Meter ID") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = building,
            onValueChange = { building = it },
            label = { Text("Building Name") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = wing,
            onValueChange = { wing = it },
            label = { Text("Wing/Floor") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = latitude,
            onValueChange = { latitude = it },
            label = { Text("Latitude") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
        )

        OutlinedTextField(
            value = longitude,
            onValueChange = { longitude = it },
            label = { Text("Longitude") },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        DateSelectionTextField(
            label = "Installed Date",
            selectedDate = date,
            onDateSelected = {
                date = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (meterId.isNotBlank() && building.isNotBlank() && wing.isNotBlank() && latitude.isNotBlank() && longitude.isNotBlank()) {
                    onAddMeter(
                        meterId,
                        building,
                        wing,
                        latitude.toFloat(),
                        longitude.toFloat(),
                        date
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = meterId.isNotBlank() && building.isNotBlank() && wing.isNotBlank() && latitude.isNotBlank() && longitude.isNotBlank()
        ) {
            Text("Add Meter")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMeterScreenPreview() {
    EnergyAuditTheme {
        AddMeterScreen(onAddMeter = { _, _, _, _, _, _ -> })
    }
}
