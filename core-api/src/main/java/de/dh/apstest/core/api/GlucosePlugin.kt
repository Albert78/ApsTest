package de.dh.apstest.core.api

import kotlinx.coroutines.flow.Flow

data class GlucoseReading(
    val value: Double,
    val timestamp: Long,
    val unit: String = "mg/dL"
)

interface GlucosePlugin {
    val name: String
    fun getGlucoseReadings(): Flow<GlucoseReading>
}