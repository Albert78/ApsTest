package de.dh.apstest.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<GlucoseEntity>>

    @Insert
    suspend fun insert(reading: GlucoseEntity)
}

@Database(entities = [GlucoseEntity::class, InsulinEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun glucoseDao(): GlucoseDao
}