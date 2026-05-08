package de.dh.raaps.plugin.pump

import de.dh.raaps.common.api.PumpPlugin
import de.dh.raaps.common.api.ToDo

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