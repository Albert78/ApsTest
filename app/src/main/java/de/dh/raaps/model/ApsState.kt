package de.dh.raaps.model

import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.data.DataRepository

/**
 * The core state of the APS system. Contains the rolling window of history values and other
 * data for calculation.
 */
class ApsState(
    val dataRepository: DataRepository
) {
    val rollingHistory: ApsRollingHistory = loadRollingHistory(dataRepository)

    // TODO: Triggers for pump commands as flow or something
    // TODO: Triggers for UI updates as flow

    fun updateBg(bg: SmoothedBgSample, dataTick: Tick) {
        val tickState = rollingHistory.getApsTickState(dataTick, true) ?: return
        tickState.bg = bg
        recalculate()
    }

    fun recalculate() {
        // TODO
        rollingHistory.publishState()
    }

    companion object {
        fun loadRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
            // TODO: Load from DB or initialize empty
            return ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
        }
    }
}

