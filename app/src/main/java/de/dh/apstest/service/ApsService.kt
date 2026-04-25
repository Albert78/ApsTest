package de.dh.apstest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.dh.apstest.MainApplication
import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.PumpPlugin
import de.dh.apstest.data.DataRepository
import de.dh.apstest.plugin.cgm.SampleCgmPlugin
import de.dh.apstest.plugin.pump.SamplePumpPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ApsService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var glucosePlugin: GlucosePlugin
    private lateinit var pumpPlugin: PumpPlugin

    val dataRepository: DataRepository = MainApplication.instance.dataRepository

    override fun onCreate() {
        super.onCreate()
        // Here we could use a plugin manager or dependency injection
        glucosePlugin = SampleCgmPlugin()
        pumpPlugin = SamplePumpPlugin()

        startObservation()
    }

    private fun startObservation() {
        serviceScope.launch {
            glucosePlugin.getGlucoseReadings().collect { reading ->
                // 1. Persist reading
                dataRepository.persistReading(reading)

                // 2. Run APS Algorithm (Logic would go here)
                runApsLogic(reading.value)
            }
        }
    }

    private suspend fun runApsLogic(currentGlucose: Double) {
        if (currentGlucose > 150.0) {
            // Suggest or deliver correction bolus
            pumpPlugin.deliverInsulin(0.1)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}