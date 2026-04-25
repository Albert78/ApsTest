package de.dh.apstest.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "glucose_readings")
data class GlucoseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val value: Double,
    val timestamp: Long,
    val unit: String
)

@Entity(tableName = "insulin_treatments")
data class InsulinEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val units: Double,
    val timestamp: Long,
    val type: String // e.g., "BOLUS", "BASAL"
)