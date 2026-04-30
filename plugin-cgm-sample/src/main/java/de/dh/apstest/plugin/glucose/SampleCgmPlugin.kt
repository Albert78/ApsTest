package de.dh.apstest.plugin.glucose

import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.data.BgReading
import de.dh.apstest.core.api.data.BgSampleKind
import de.dh.apstest.core.api.data.BgValue
import de.dh.apstest.core.api.data.RawBg
import de.dh.apstest.core.api.data.Timestamp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class SampleCgmPlugin : GlucosePlugin {
    override val name: String = "Sample CGM Plugin"

    override val dataProviderType: String = "CGM"

    override fun getSensorTypeName() = "Libre3"

    fun getRawGlucoseReadings(): Flow<RawBg> = flow {
        while (true) {
            val reading = RawBg(
                value = BgValue.fromMgDl(100 + Random.nextInt(-10, 10)),
                timestamp = Timestamp(System.currentTimeMillis()),
            )
            emit(reading)
            delay(1000*60) // Emit minute for demo purposes
        }
    }

    override fun getValues(): Flow<BgReading> {
        return getRawGlucoseReadings().map { raw ->
            sampleMapRawValues(raw)
        }
    }

    private fun sampleMapRawValues(raw: RawBg): BgReading {
        val kind = when (raw.value.mgdl.toInt()) {
            39 -> BgSampleKind.Low
            401 -> BgSampleKind.High
            0 -> BgSampleKind.Invalid
            else -> BgSampleKind.Value
        }
        return BgReading(
            value = raw.value,
            sampleKind = kind,
            timestamp = raw.timestamp
        )
    }
}