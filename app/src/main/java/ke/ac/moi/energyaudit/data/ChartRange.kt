package ke.ac.moi.energyaudit.data

enum class ChartRange(val limit: Int, val label: String) {
    LAST_10(10, "10"),
    LAST_30(30, "30"),
    LAST_60(60, "60")
}
