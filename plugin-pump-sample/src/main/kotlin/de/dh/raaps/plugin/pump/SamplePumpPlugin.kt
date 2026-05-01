package de.dh.raaps.plugin.pump

import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.core.api.ToDo

class SamplePumpPlugin : PumpPlugin {
    override val name: String = "Sample Pump Plugin"

    // TODO

    override fun start() {
        ToDo.toBeImplemented("SamplePumpPlugin")
    }

    override fun stop() {
        // TODO
    }
}