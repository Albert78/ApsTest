package de.dh.raaps.model

import android.util.Log
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
import kotlinx.coroutines.flow.map

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
    var lastBg: SmoothedBgSample? = null
        private set

    /**
     * Time delay between a glucose value in blood and the announced Timestamp of the bg reading.
     * Typically, the announced timestamp represents the time of measure of the CGM system, which
     * is about 5 minutes behind blood glucose.
     */
    var glucoseReadingsTimeDelay: Minutes = Minutes(0)

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
        sensorType: SensorType
    ) {
        val readingsInterval = plugin.readingsInterval
        val datasourceTimeDelay = plugin.readingsTimeDelay
        // TODO: Save changes in readings interval (5 minutes to 1 minutes and vice versa) to database

        // Persist values
        val persistedValues = plugin.getValues()
            .persist(dataRepository, dataProvider, sensorType)

        // Smooth values.
        // filterTimeDelay could be used if we apply a filter which will emit the smoothed
        // value with a delay, e.g. for extremely noisy values.
        val (smoothedValues, filterTimeDelay) = when (readingsInterval) {
            BgReadingsInterval.OneMinute ->
                // More aggressive smoothing to get better values
                Pair(
                    persistedValues
                        .smoothGlucoseSmart_1_Minute_Readings(),
                    Minutes(0)
                )

            BgReadingsInterval.FiveMinutes ->
                // Less smoothing to avoid additional delay of values
                Pair(
                    persistedValues
                        .smoothGlucoseSmart_5_Minute_Readings(),
                    Minutes(0)
                )

            BgReadingsInterval.AdHoc ->
                // No smoothing at all
                Pair(
                    persistedValues
                        .map { SmoothedBgSample.plainValue(it) },
                    Minutes(0)
                )
        }

        glucoseReadingsTimeDelay = datasourceTimeDelay + filterTimeDelay

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
            Log.d(TAG, "Got new BG in APS Core: $bg")
            lastBg = currentBg
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

    companion object {
        val TAG = APSCore::class.simpleName
    }
}