package de.dh.raaps.model

import android.content.Context
import android.os.PowerManager
import de.dh.raaps.core.api.GlucosePlugin
import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.data.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * APS system facade for the access from outside (UI, ...).
 * Manages threading and plugin lifecycles, ensuring all calls to the core are serialized
 * on a single background thread.
 */
class APS(
    val context: Context,
    val dataRepository: DataRepository
) {
    // Threading: Single background thread to avoid race conditions in the core logic
    private val apsDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val apsScope = CoroutineScope(apsDispatcher + SupervisorJob())

    // Power Management
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "raaps:ApsCoreLock")

    // Computation Core: Pure logic and state, completely thread-agnostic
    private val core = APSCore(
        dataRepository,
        { emitDataUpdateEvent() }
    )

    // Plugins & Active Jobs
    private var glucoseJob: Job? = null
    var glucosePlugin: GlucosePlugin? = null
        set(value) {
            field?.stop()
            field = value
            field?.start()
            restartGlucosePipeline()
        }

    private var pumpJob: Job? = null
    var pumpPlugin: PumpPlugin? = null
        set(value) {
            field?.stop()
            field = value
            field?.start()
            // TODO: restart calculation or subscription for pump
        }

    val rollingHistory: ApsRollingHistory get() = core.rollingHistory

    // Observers: Exposed from the internal core
    // Observers (Updated by the core, read by the facade/UI)
    private val _lastDataTime = MutableStateFlow<Timestamp>(Timestamp(0))
    val lastDataTime: StateFlow<Timestamp> = _lastDataTime.asStateFlow()

    init {
        installWakeLockManager()
        restartGlucosePipeline()
    }

    private fun installWakeLockManager() {
        // Collect busy state on a separate scope to ensure it's not blocked by core calculation
        apsScope.launch(Dispatchers.Default) {
            core.busyState.collect { busyState ->
                if (busyState > 0) {
                    if (!wakeLock.isHeld) wakeLock.acquire(5_000) // 5s safety timeout
                } else {
                    if (wakeLock.isHeld) {
                        try {
                            wakeLock.release()
                        } catch (e: RuntimeException) {
                            // Ignore if already released
                        }
                    }
                }
            }
        }
    }

    private fun restartGlucosePipeline() {
        glucoseJob?.cancel() // Cancel old pipeline if one exists
        val plugin = glucosePlugin ?: return

        glucoseJob = apsScope.launch {
            installGlucosePipeline_ApsThread(plugin)
        }
    }

    private suspend fun installGlucosePipeline_ApsThread(plugin: GlucosePlugin) {
        val sensorType = dataRepository.getOrCreateSensorTypeByName(plugin.getSensorTypeName())
        val dataProvider =
            dataRepository.getOrCreateDataProviderByName(plugin.name, plugin.dataProviderType)

        core.installGlucosePipeline(plugin, dataProvider, sensorType)
    }

    private fun emitDataUpdateEvent() {
        apsScope.launch(Dispatchers.Default) {
            _lastDataTime.emit(Timestamp.now())
        }
    }

    /**
     * Entry point for external BG updates.
     * Guaranteed to run on the internal APS thread.
     */
    fun updateBg(bg: SmoothedBgSample) {
        apsScope.launch {
            core.updateBg(bg)
        }
    }

    /**
     * Gracefully stops the APS system and releases all background resources.
     */
    fun stop() {
        glucosePlugin?.let {
            it.stop()
            glucosePlugin = null
        }
        pumpPlugin?.let {
            it.stop()
            pumpPlugin = null
        }
        apsScope.cancel()
        apsDispatcher.close()
    }

    fun getCurrentBg(): SmoothedBgSample? {
        return core.currentBg
    }
}