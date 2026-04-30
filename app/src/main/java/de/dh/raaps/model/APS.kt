package de.dh.raaps.model

import de.dh.raaps.core.api.GlucosePlugin
import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.data.DataRepository
import de.dh.raaps.service.persist
import de.dh.raaps.service.smoothGlucosePTWMA
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
    val dataRepository: DataRepository
) {
    // Threading: Single background thread to avoid race conditions in the core logic
    private val apsDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val apsScope = CoroutineScope(apsDispatcher + SupervisorJob())

    // Computation Core: Pure logic and state, completely thread-agnostic
    private val core = APSCore(dataRepository, { emitDataUpdateEvent() })

    // Plugins & Active Jobs
    private var glucoseJob: Job? = null
    var glucosePlugin: GlucosePlugin? = null
        set(value) {
            field = value
            restartGlucosePipeline()
        }

    private var pumpJob: Job? = null
    var pumpPlugin: PumpPlugin? = null
        set(value) {
            field = value
            // TODO: restart calculation or subscription for pump
        }

    val rollingHistory: ApsRollingHistory get() = core.rollingHistory

    // Observers: Exposed from the internal core
    // Observers (Updated by the core, read by the facade/UI)
    private val _lastDataTime = MutableStateFlow<Timestamp>(Timestamp(0))
    val lastDataTime: StateFlow<Timestamp> = _lastDataTime.asStateFlow()

    init {
        restartGlucosePipeline()
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

        plugin.getValues()
            .persist(dataRepository, dataProvider, sensorType)
            .smoothGlucosePTWMA(windowSize = Minutes(5), weightSlope = 0.7)
            .collect { bg ->
                // Since this runs within apsScope, we call the core directly
                core.updateBg(bg)
            }
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