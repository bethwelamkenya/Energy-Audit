package ke.ac.moi.energyaudit.data

data class PowerMetrics(
    val realPowerKw: Double,
    val reactivePowerKvar: Double,
    val apparentPowerKva: Double,
    val powerFactor: Double
)