package de.dh.apstest.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import de.dh.apstest.MainApplication
import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.PumpPlugin
import de.dh.apstest.core.api.data.Minutes
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
        // Here we could use a plugin manager
        glucosePlugin = SampleCgmPlugin()
        pumpPlugin = SamplePumpPlugin()

        startObservation()
    }

    private fun startObservation() {
        serviceScope.launch {
            val sensorType = dataRepository.getOrCreateSensorTypeByName(glucosePlugin.getSensorTypeName())
            val dataProvider = dataRepository.getOrCreateDataProviderByName(glucosePlugin.name, glucosePlugin.dataProviderType)
            val tickIntervalSize = Minutes(5)

            val values = glucosePlugin.getValues()
                .persist(dataRepository, dataProvider, sensorType)
                .smoothGlucosePTWMA(windowSize = Minutes(5), weightSlope = 0.7)
                .sampleByTick(tickIntervalSize = tickIntervalSize)
                .toRollingHistory(historyHours = 10, tickIntervalSize = tickIntervalSize)
            // Weiter:
            // 5. weitere Berechnungen (COB, IOB, Sensitivity, Basal)
            // 6. Ausgabe

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