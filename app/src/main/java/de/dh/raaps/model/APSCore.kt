package de.dh.raaps.model

import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.data.DataRepository

/**
 * The computation core of the APS system.
 * This class is NOT thread-safe by itself and must be called from a controlled threading environment (like APS facade).
 */
class APSCore(
    val dataRepository: DataRepository,
    private val onDataUpdated: () -> Unit
) {
    // State
    val rollingHistory: ApsRollingHistory = loadRollingHistory(dataRepository)
    var currentBg: SmoothedBgSample? = null
        private set

    /**
     * Processes a new glucose reading.
     */
    fun updateBg(bg: SmoothedBgSample) {
        currentBg = bg
        val tick = rollingHistory.tick(bg.timestamp)
        val lastAnchorTick = rollingHistory.anchorTick
        val tickState = rollingHistory.getApsTickState(tick, true) ?: return
        tickState.bg = bg
        if (tick != lastAnchorTick) {
            recalculate()
        }
        onDataUpdated()
    }

    /**
     * Core therapy calculation logic.
     */
    fun recalculate() {
        // TODO: Implement therapy algorithm (IOB, COB, Prediction, Temp Basal)
    }

    private fun loadRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
        // TODO: Load from DB or initialize empty
        return ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
    }
}