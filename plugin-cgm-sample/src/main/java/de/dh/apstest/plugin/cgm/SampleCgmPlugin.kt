package de.dh.apstest.plugin.cgm

import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.GlucoseReading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class SampleCgmPlugin : GlucosePlugin {
    override val name: String = "Sample CGM Plugin"

    override fun getGlucoseReadings(): Flow<GlucoseReading> = flow {
        while (true) {
            val reading = GlucoseReading(
                value = 100.0 + Random.nextDouble(-10.0, 10.0),
                timestamp = System.currentTimeMillis()
            )
            emit(reading)
            delay(5000) // Emit every 5 seconds for demo purposes
        }
    }
}