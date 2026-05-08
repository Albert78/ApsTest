package de.dh.raaps.model

import de.dh.raaps.common.api.ID_UNDEFINED
import de.dh.raaps.common.api.data.SmoothedBgSample
import de.dh.raaps.common.api.data.Tick

/**
 * Contains the APS data contained at a discrete time tick in the APS rolling history window.
 */
data class ApsTickState(
    var id: Long,
    val tick: Tick,
    var bg: SmoothedBgSample? = null,

    // TODO: All needed fields for APS calculations
    var iob: Double = 0.0, // Insulin on Board
    var cob: Double = 0.0, // Carbs on Board
    var prediction: Double? = null
) {
    companion object {
        fun empty(tick: Tick) = ApsTickState(id = ID_UNDEFINED, tick = tick)
    }
}
