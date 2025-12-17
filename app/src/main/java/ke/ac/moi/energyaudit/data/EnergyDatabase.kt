package ke.ac.moi.energyaudit.data

import android.content.Context
import ke.ac.moi.energyaudit.utils.DateConverters

@Database(
    entities = [
        MeterLocationEntity::class,
        EnergyReadingEntity::class
    ],
    version = 1
)
@TypeConverters(DateConverters::class)
abstract class EnergyDatabase : RoomDatabase() {

    abstract fun meterLocationDao(): MeterLocationDao
    abstract fun energyReadingDao(): EnergyReadingDao

    companion object {
        @Volatile
        private var INSTANCE: EnergyDatabase? = null

        fun getInstance(context: Context): EnergyDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    EnergyDatabase::class.java,
                    "energy_audit_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
