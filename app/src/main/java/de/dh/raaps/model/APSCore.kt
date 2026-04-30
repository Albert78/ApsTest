package de.dh.raaps.model

import de.dh.raaps.core.api.DataProvider
import de.dh.raaps.core.api.GlucosePlugin
import de.dh.raaps.core.api.data.BgReadingsInterval
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SensorType
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.data.DataRepository
import de.dh.raaps.service.persist
import de.dh.raaps.service.smoothGlucoseSmart_1_Minute_Readings
import de.dh.raaps.service.smoothGlucoseSmart_5_Minute_Readings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * The computation core of the APS system.
 * This class is NOT thread-safe by itself and must be called from a controlled threading environment (like APS facade).
 * This class should remain (almost) free of workarounds for the Android system.
 * We only need to signal the internal calculation state by setting the [busyState] flag. The surrounding app
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

    // Signal to the facade that the core is currently performing critical work. If the
    // busy state is bigger than 0, the core is working and needs a wake lock.
    private val _busyState = MutableStateFlow(0)
    val busyState: StateFlow<Int> = _busyState.asStateFlow()

    /**
     * Installs the input Flow of BG values from the given plugin.
     */
    suspend fun installGlucosePipeline(
        plugin: GlucosePlugin,
        dataProvider: DataProvider,
        sensorType: SensorType,
        readingsInterval: BgReadingsInterval
    ) {
        // TODO: Save changes in readings interval (5 minutes to 1 minutes and vice versa)

        // Persist values
        val persistedValues = plugin.getValues()
            .persist(dataRepository, dataProvider, sensorType)

        // Smooth values
        val smoothedValues = when (readingsInterval) {
            BgReadingsInterval.OneMinute ->
                persistedValues
                    .smoothGlucoseSmart_1_Minute_Readings()

            BgReadingsInterval.FiveMinutes ->
                persistedValues
                    .smoothGlucoseSmart_5_Minute_Readings()
        }

        // Collect for core calculation
        smoothedValues
            .collect { bg ->
                updateBg(bg)
            }
    }

    /**
     * Processes a new glucose reading. This can also be called from outside to provide an additional
     * bg value, e.g. from a bloody measure.
     */
    suspend fun updateBg(bg: SmoothedBgSample) {
        busyWork {
            currentBg = bg
            val tick = rollingHistory.tick(bg.timestamp)
            val lastAnchorTick = rollingHistory.anchorTick
            val tickState = rollingHistory.getApsTickState(tick, true) ?: return@busyWork
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

    /**
     * Block marker for code blocks which need a wake lock in the system.
     * If blocks are not marked with this marker, the processor can go into sleep mode any time.
     */
    private suspend fun <T> busyWork(block: suspend () -> T): T {
        _busyState.value++
        try {
            return block()
        } finally {
            _busyState.value--
        }
    }

    private fun loadRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
        // TODO: Load from DB or initialize empty
        return ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
    }
}