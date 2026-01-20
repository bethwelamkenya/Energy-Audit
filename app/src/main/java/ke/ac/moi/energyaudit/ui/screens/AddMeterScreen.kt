package ke.ac.moi.energyaudit.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import ke.ac.moi.energyaudit.data.MeterLocationEntity
import ke.ac.moi.energyaudit.ui.theme.EnergyAuditTheme
import java.time.LocalDate
import java.util.Locale

private fun generateMeterId1(building: String, block: String, wing: String): String {
    val buildingAbbr = building.trim()
        .split(Regex("[\\s-]+")) // Split on spaces or hyphens
        .filter { it.isNotBlank() }
        .joinToString("") { it.first().toString() }
        .uppercase(Locale.ROOT)

    val finalBlock = block.trim().replace(" ", "").uppercase(Locale.ROOT)
    val finalWing = wing.trim().replace(" ", "").uppercase(Locale.ROOT)

    return "$buildingAbbr-$finalBlock-$finalWing"
}

fun abbreviateBuildingName(buildingName: String): String {
    val ignoreWords = listOf("block", "building", "hall", "hostel", "complex")
    val words = buildingName.lowercase()
        .split(" ", "-")
        .filter { it.isNotBlank() && !ignoreWords.contains(it) }

    if (words.isEmpty()) return buildingName.take(3).uppercase()

    return if (words.size == 1) {
        // Only one word → take first 3 letters + digits
        val word = words.first()
        val letters = word.takeWhile { it.isLetter() }.take(3).uppercase()
        val digits = word.dropWhile { it.isLetter() }.filter { it.isDigit() }
        letters + digits
    } else {
        // Multiple words → take first letter of each word
        words.joinToString("") { word ->
            val letter = word.firstOrNull()?.uppercaseChar() ?: ""
            val digit = word.drop(1).filter { it.isDigit() }
            "$letter$digit"
        }
    }
}

fun normalizeBlockInput(input: String): String {
    val cleaned = input.trim().uppercase()

    return when {
        cleaned.matches(Regex("^\\d+$")) -> "B$cleaned" // numbers → B1, B2
        cleaned.matches(Regex("^[A-Z]{1,2}$")) -> "B$cleaned" // letters → BA, BB
        else -> cleaned.take(2) // fallback
    }
}

fun normalizeWingInput(input: String): String {
    val cleaned = input.trim().uppercase()

    return when {
        cleaned.matches(Regex("^\\d+$")) -> "W$cleaned" // if user typed a number, prepend "W"
        cleaned.matches(Regex("^[A-Z]{1,2}$")) -> "W$cleaned" // already a letter(s)
        cleaned.matches(Regex("^WA$|^WB$|^WC$")) -> cleaned // optional fixed mapping
        else -> cleaned.take(2) // fallback to first 1-2 letters
    }
}

fun generateMeterId(buildingName: String, blockInput: String, wingInput: String): String {
    val abbr = abbreviateBuildingName(buildingName)
    val block = normalizeBlockInput(blockInput)
    val wing = normalizeWingInput(wingInput)
    return listOf(abbr, block, wing).joinToString("-")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMeterScreen(
    onAddMeter: (MeterLocationEntity) -> Unit
) {
    var building by remember { mutableStateOf("") }
    var block by remember { mutableStateOf("") }
    var wing by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(LocalDate.now()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Enter Meter Details",
            style = MaterialTheme.typography.headlineSmall
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
            value = block,
            onValueChange = { block = it },
            label = { Text("Building Block") },
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )

        DateSelectionTextField(
            label = "Installed Date",
            selectedDate = date,
            onDateSelected = {
                date = it
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        val areFieldsValid = building.isNotBlank() &&
                block.isNotBlank() &&
                wing.isNotBlank() &&
                latitude.isNotBlank() &&
                longitude.isNotBlank()

        Button(
            onClick = {
                val meterId = generateMeterId(building, block, wing)
                onAddMeter(
                    MeterLocationEntity(
                    meterId,
                    building.trim(),
                    block.trim(),
                    wing.trim(),
                    latitude.trim().toFloat(),
                    longitude.trim().toFloat(),
                    date
                )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            enabled = areFieldsValid
        ) {
            Text("Add Meter")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMeterScreenPreview() {
    EnergyAuditTheme {
        AddMeterScreen(onAddMeter = { _-> })
    }
}
