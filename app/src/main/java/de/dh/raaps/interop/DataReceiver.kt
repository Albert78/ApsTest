package de.dh.raaps.interop

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.dh.raaps.MainApplication
import de.dh.raaps.core.api.data.BgReading
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.RawBg
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.plugin.glucose.receiver.ReceiverGlucosePlugin
import kotlin.math.round
import kotlin.time.Duration.Companion.days

class DataReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        processIntent(context, intent)
    }

    fun processIntent(context: Context, intent: Intent) {
        val bundle = intent.extras ?: return
        when (intent.action) {
            Intents.XDRIP_ACTION_BG_ESTIMATE -> {
                val timestampMs = bundle.getLong(Intents.XDRIP_EXTRA_TIMESTAMP, 0)
                val valueMgDl = round(bundle.getDouble(Intents.XDRIP_EXTRA_BG_ESTIMATE, 0.0))
                val raw = round(bundle.getDouble(Intents.XDRIP_EXTRA_RAW, 0.0))
                val sourceSensorName = bundle.getString(Intents.XDRIP_DATA_SOURCE)

                val now = System.currentTimeMillis()
                var sensorStartTime: Long? = bundle.getLong(Intents.XDRIP_EXTRA_SENSOR_STARTED_AT, 0)
                // check start time validity
                sensorStartTime?.let {
                    if (it == 0L || it > now || now - it > 30.days.inWholeMilliseconds) {
                        sensorStartTime = null
                    }
                }
                val rawValue = RawBg(
                    BgValue(valueMgDl.toInt().toShort()),
                    Timestamp(timestampMs)
                )
                fun mapRawXDripValues(raw: RawBg): BgReading {
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
                if (timestampMs > 0) {
                    val receiverPlugin: ReceiverGlucosePlugin? = MainApplication.instance.glucosePlugin as? ReceiverGlucosePlugin
                    receiverPlugin?.injectReading(mapRawXDripValues(rawValue))
                }
            }
        }
    }
}