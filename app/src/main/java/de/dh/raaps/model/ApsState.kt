package de.dh.raaps.model

import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.data.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The core state of the APS system. Contains the rolling window of history values and other
 * data for calculation.
 */
class ApsState(
    val dataRepository: DataRepository
) {
    val rollingHistory: ApsRollingHistory = loadRollingHistory(dataRepository)
    var currentBg: SmoothedBgSample? = null

    // Emits the timestamp of the last state to all observers (e.g. UI)
    private val _lastDataTime = MutableStateFlow<Timestamp>(Timestamp(0))
    val lastDataTime: StateFlow<Timestamp> = _lastDataTime.asStateFlow()

    // TODO: Triggers for pump commands as flow or something
    // TODO: Triggers for UI updates as flow

    suspend fun updateBg(bg: SmoothedBgSample) {
        currentBg = bg
        val tick = rollingHistory.tick(bg.timestamp)
        val lastAnchorTick = rollingHistory.anchorTick
        val tickState = rollingHistory.getApsTickState(tick, true) ?: return
        tickState.bg = bg
        if (tick != lastAnchorTick) {
            recalculate()
        }
        _lastDataTime.emit(bg.timestamp)
    }

    fun recalculate() {
        // TODO
    }

    companion object {
        fun loadRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
            // TODO: Load from DB or initialize empty
            return ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
        }
    }
}