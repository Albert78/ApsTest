package de.dh.raaps.model

import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.data.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The computation core of the APS system.
 * This class is NOT thread-safe by itself and must be called from a controlled threading environment (like APS facade).
 * This class should remain (almost) free of workarounds for the Android system.
 * We only need to signal the internal calculation state by setting the [isBusy] flag. The surrounding app
 * is responsible for acquiring a wake lock.
 */
class APSCore(
    val dataRepository: DataRepository,
    private val onDataUpdated: () -> Unit
) {
    // State
    val rollingHistory: ApsRollingHistory = loadRollingHistory(dataRepository)
    var currentBg: SmoothedBgSample? = null
        private set

    // Signal to the facade that the core is currently performing critical work
    private val _isBusy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

    /**
     * Processes a new glucose reading.
     */
    suspend fun updateBg(bg: SmoothedBgSample) {
        withBusySignal {
            currentBg = bg
            val tick = rollingHistory.tick(bg.timestamp)
            val lastAnchorTick = rollingHistory.anchorTick
            val tickState = rollingHistory.getApsTickState(tick, true) ?: return@withBusySignal
            tickState.bg = bg
            if (tick != lastAnchorTick) {
                recalculate()
            }
            onDataUpdated()
        }
    }

    /**
     * Core therapy calculation logic.
     */
    fun recalculate() {
        // TODO: Implement therapy algorithm (IOB, COB, Prediction, Temp Basal)
    }

    private suspend fun <T> withBusySignal(block: suspend () -> T): T {
        _isBusy.value = true
        try {
            return block()
        } finally {
            _isBusy.value = false
        }
    }

    private fun loadRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
        // TODO: Load from DB or initialize empty
        return ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
    }
}