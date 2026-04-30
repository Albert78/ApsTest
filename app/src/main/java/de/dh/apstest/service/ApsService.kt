package de.dh.apstest.service

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import de.dh.apstest.MainApplication
import de.dh.apstest.core.api.GlucosePlugin
import de.dh.apstest.core.api.PumpPlugin
import de.dh.apstest.core.api.data.Minutes
import de.dh.apstest.data.DataRepository
import de.dh.apstest.model.ApsState
import de.dh.apstest.notifications.ApsNotificationManager
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
    private val notificationManager: ApsNotificationManager = MainApplication.instance.notificationManager

    val dataRepository: DataRepository = MainApplication.instance.dataRepository
    val apsState : ApsState = MainApplication.instance.apsState

    override fun onCreate() {
        super.onCreate()

        notificationManager.createNotificationChannels()
        startServiceInForeground()

        // Here we could use a plugin manager
        glucosePlugin = SampleCgmPlugin()
        pumpPlugin = SamplePumpPlugin()

        startObservation()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceInForeground()

        return START_STICKY
    }

    private fun startServiceInForeground() {
        val notification: Notification = notificationManager.createForegroundServiceNotification()

        startForeground(
            ApsNotificationManager.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
    }

    private fun startObservation() {
        serviceScope.launch {
            val sensorType = dataRepository.getOrCreateSensorTypeByName(glucosePlugin.getSensorTypeName())
            val dataProvider = dataRepository.getOrCreateDataProviderByName(glucosePlugin.name, glucosePlugin.dataProviderType)
            val tickIntervalSize = Minutes(5)

            glucosePlugin.getValues()
                .persist(dataRepository, dataProvider, sensorType)
                .smoothGlucosePTWMA(windowSize = Minutes(5), weightSlope = 0.7)
                .sampleByTick(tickIntervalSize = tickIntervalSize)
                .collect { (bg, tick) ->
                    apsState.updateBg(bg, tick)
                }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}