package de.dh.apstest.model

import de.dh.apstest.core.api.data.SmoothedBgSample
import de.dh.apstest.core.api.data.Tick

data class ApsState(
    val tick: Tick,
    val glucose: SmoothedBgSample?,
    val iob: Double = 0.0, // Insulin on Board
    val cob: Double = 0.0, // Carbs on Board
    val prediction: Double? = null
) {
    companion object {
        fun empty(tick: Tick) = ApsState(tick = tick, glucose = null)
    }
}