package de.dh.apstest.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.dh.apstest.model.GlucoseUnit

const val ID_UNDEFINED = -1L

/**
 * Types of glucose sensors like "Libre3", "Dexcom G6", ...
 */
@Entity(
    tableName = "sensor_type"
)
data class SensorTypeEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = ID_UNDEFINED,
    var name: String
)

/**
 * Providers for glucose values.
 */
@Entity(
    tableName = "data_provider"
)
data class DataProviderEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Long = ID_UNDEFINED,
    var name: String,
    var type: String
)

@Entity(
    tableName = "glucose_reading",
    foreignKeys = [
        ForeignKey(
            entity = SensorTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["fk_source_sensor"]
        ),
        ForeignKey(
            entity = DataProviderEntity::class,
            parentColumns = ["id"],
            childColumns = ["fk_data_provider"]
        ),
    ],
    indices = [
        Index("timestamp_ms")
    ]
)
data class GlucoseReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = ID_UNDEFINED,
    val value: Double,
    val unit: GlucoseUnit,
    val timestamp_ms: Long,
    val fk_data_provider: Long,
    val fk_source_sensor: Long
)