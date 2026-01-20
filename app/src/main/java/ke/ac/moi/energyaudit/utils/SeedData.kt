package ke.ac.moi.energyaudit.utils

import ke.ac.moi.energyaudit.data.MeterLocationEntity
import java.time.LocalDate

object SeedData {

    val meters = listOf(
        MeterLocationEntity(
            meterId = "ENG-B1-WA",
            building = "Engineering Block",
            block = "1",
            wing = "A",
            latitude = -1.2921f,
            longitude = 36.8219f,
            installedDate = LocalDate.of(2023, 5, 10)
        ),
        MeterLocationEntity(
            meterId = "SCI-B2-WB",
            building = "Science Complex",
            block = "2",
            wing = "B",
            latitude = -1.2924f,
            longitude = 36.8222f,
            installedDate = LocalDate.of(2022, 9, 18)
        ),
        MeterLocationEntity(
            meterId = "LIB-B3-WC",
            building = "Library",
            block = "1",
            wing = "C",
            latitude = -1.2927f,
            longitude = 36.8226f,
            installedDate = LocalDate.of(2021, 3, 2)
        )
    )
}
