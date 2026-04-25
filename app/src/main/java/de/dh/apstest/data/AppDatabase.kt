package de.dh.apstest.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import java.util.concurrent.Executors

@Dao
interface ProvidersDao {
    @Query("SELECT * FROM sensor_type ORDER BY name ASC")
    fun getAllSensorTypes(): List<SensorTypeEntity>

    @Query("SELECT * FROM data_provider ORDER BY name ASC")
    fun getAllDataProviders(): List<DataProviderEntity>

    @Query("SELECT * FROM glucose_reading where timestamp_ms > :timestampMs ORDER BY timestamp_ms ASC")
    fun getReadingsFromTime(timestampMs: Long): List<GlucoseReadingEntity>

    @Insert
    suspend fun insert(reading: GlucoseReadingEntity)
}

@Database(entities = [SensorTypeEntity::class, DataProviderEntity::class, GlucoseReadingEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun providersDao(): ProvidersDao

    companion object {
        const val CURRENT_DATABASE_VERSION = "1.0"
        private const val DATABASE_NAME = "ApsDatabase.db"
        private const val LOG_DB_STATEMENTS = false

        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val sLock = Any()

        fun getInstance(context: Context): AppDatabase {
            synchronized(sLock) {
                if (INSTANCE == null) {
                    val dbBuilder = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                    // .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_1_4)

                    if (LOG_DB_STATEMENTS) {
                        dbBuilder.setQueryCallback({ sqlQuery, bindArgs ->
                            println("SQL Query: $sqlQuery SQL Args: $bindArgs")
                        }, Executors.newSingleThreadExecutor())
                    }
                    INSTANCE = dbBuilder.build()
                }
                return INSTANCE!!
            }
        }
    }
}