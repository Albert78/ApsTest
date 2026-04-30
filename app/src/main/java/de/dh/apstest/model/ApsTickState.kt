package de.dh.apstest.model

import de.dh.apstest.core.api.data.SmoothedBgSample

/**
 * Contains the APS data contained at a discrete time tick in the APS rolling history window.
 */
data class ApsTickState(
    var bg: SmoothedBgSample? = null,

    // TODO: All needed fields for APS calculations
    var iob: Double = 0.0, // Insulin on Board
    var cob: Double = 0.0, // Carbs on Board
    var prediction: Double? = null
) {
    companion object {
        fun empty() = ApsTickState()
    }
}