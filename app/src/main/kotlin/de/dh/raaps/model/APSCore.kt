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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class APSCoreState {
    Plain,
    Initializing,
    Calculating,
    Idle,
    Uninitializing
}

/**
 * The computation core of the APS system.
 * This class is NOT thread-safe by itself and must be called from a controlled threading environment (like APS facade).
 * This class should remain (almost) free of workarounds for the Android system.
 * We only need to signal the internal calculation state by setting the [busyState] flag. The surrounding app
 * is responsible for acquiring a wake lock.
 */
class APSCore(
    val dataRepository: DataRepository,
    private val onDataUpdated: () -> Unit,
    private val onCoreStateChanged: () -> Unit,
    private val onAcquireBusyState: () -> Unit,
    private val onReleaseBusyState: () -> Unit
) {
    // State
    var rollingHistory: ApsRollingHistory = ApsRollingHistory(historyHours = 0, tickDuration = Minutes(5))
    var currentBg: SmoothedBgSample? = null
        private set
    var lastBg: SmoothedBgSample? = null
        private set

    var coreState: APSCoreState = APSCoreState.Plain
        private set

    /**
     * Time delay between a glucose value in blood and the announced Timestamp of the bg reading.
     * Typically, the announced timestamp represents the time of measure of the CGM system, which
     * is about 5 minutes behind blood glucose.
     */
    var glucoseReadingsTimeDelay: Minutes = Minutes(0)

    /**
     * Lock which is acquired in situations where a suspend function
     * must be atomic. Other functions don't need to be locked since
     * we're single-threaded inside this class by design.
     */
    private val atomicOperationLock = Mutex()

    /**
     * Block marker for code blocks which need a wake lock in the system.
     * If blocks are not marked with this marker, the processor can go into sleep mode any time.
     */
    private suspend fun <T> busyWork(block: suspend () -> T): T {
        onAcquireBusyState()
        try {
            return block()
        } finally {
            onReleaseBusyState()
        }
    }

    private fun setCoreState(state: APSCoreState) {
        coreState = state
        onCoreStateChanged()
    }

    suspend fun initialize() {
        atomicOperationLock.withLock {
            Log.d(TAG, "Initializing...")
            setCoreState(APSCoreState.Initializing)
            rollingHistory = initializeRollingHistory(dataRepository)
            Log.d(TAG, "Finished initialization...")
            setCoreState(APSCoreState.Idle)
        }
    }

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
            // Threading notice:
            // Since we're in a coroutine, this collect call won't block our (single) thread; instead,
            // it will just suspend and free the thread for other work.
            .collect { bg ->
                updateBg(bg)
            }

        Log.d(TAG, "Installed glucose pipeline")
    }

    /**
     * Processes a new glucose reading. This can also be called from outside to provide an additional
     * bg value, e.g. from a bloody measure.
     */
    suspend fun updateBg(bg: SmoothedBgSample) {
        busyWork {
            Log.d(TAG, "Got new BG: $bg")
            lastBg = currentBg
            currentBg = bg
            val tick = rollingHistory.tick(bg.timestamp)
            val lastAnchorTick = rollingHistory.anchorTick
            val tickState = rollingHistory.getApsTickState(tick, true) ?: return@busyWork
            tickState.bg = bg
            dataRepository.insertOrUpdateTickState(tickState)
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

    private suspend fun initializeRollingHistory(dataRepository: DataRepository): ApsRollingHistory {
        val result = ApsRollingHistory(historyHours = 10, tickDuration = Minutes(5))
        val anchorTick = result.getNowTick()
        result.advanceTo(anchorTick)
        val tickStates = dataRepository.getTickStates(result.getFirstTick(), anchorTick)
        result.replaceBufferTickStates(tickStates)
        return result
    }

    companion object {
        val TAG = APSCore::class.simpleName
    }
}
