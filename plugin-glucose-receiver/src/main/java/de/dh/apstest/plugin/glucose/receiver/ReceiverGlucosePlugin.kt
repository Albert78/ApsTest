package de.dh.apstest.plugin.glucose.receiver

import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.data.BgReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ReceiverGlucosePlugin : GlucosePlugin {
    override val name: String = "Receiver Glucose Plugin"

    override val dataProviderType: String = "Glucose"

    override fun getSensorTypeName() = "External Receiver"

    private val _readings = MutableSharedFlow<BgReading>(extraBufferCapacity = 64)

    /**
     * This can be used to inject values received from a BroadcastReceiver.
     */
    fun injectReading(value: BgReading): Boolean {
        return _readings.tryEmit(value)
    }

    override fun getValues(): Flow<BgReading> {
        return _readings.asSharedFlow()
    }
}