package de.dh.raaps.plugin.pump

import android.app.Application
import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.core.api.ToDo

class SamplePumpPlugin : PumpPlugin {
    override val name: String = "Sample Pump Plugin"

    // TODO

    override fun start(application: Application) {
        ToDo.toBeImplemented("SamplePumpPlugin")
    }

    override fun stop() {
        // TODO
    }
}