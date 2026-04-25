package de.dh.apstest.plugin.pump

import de.dh.apstest.core.api.PumpPlugin
import android.util.Log

class SamplePumpPlugin : PumpPlugin {
    override val name: String = "Sample Pump Plugin"

    override suspend fun deliverInsulin(units: Double): Boolean {
        Log.d("SamplePumpPlugin", "Delivering $units units of insulin")
        // In a real implementation, this would communicate with the hardware
        return true
    }

    override suspend fun getBasalRate(): Double {
        return 0.5 // Default sample basal rate
    }
}