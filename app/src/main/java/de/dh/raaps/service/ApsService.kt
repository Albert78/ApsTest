package de.dh.raaps.service

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.IBinder
import de.dh.raaps.MainApplication
import de.dh.raaps.core.api.GlucosePlugin
import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.data.DataRepository
import de.dh.raaps.interop.DataReceiver
import de.dh.raaps.model.ApsState
import de.dh.raaps.notifications.ApsNotificationData
import de.dh.raaps.notifications.ApsNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ApsService : Service() {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val notificationManager: ApsNotificationManager = MainApplication.instance.notificationManager
    private val dataReceiver = DataReceiver()

    val dataRepository: DataRepository = MainApplication.instance.dataRepository
    val apsState : ApsState = MainApplication.instance.apsState
    var glucosePlugin: GlucosePlugin? = null
    var pumpPlugin: PumpPlugin? = null

    override fun onCreate() {
        super.onCreate()

        notificationManager.createNotificationChannels()
        startServiceInForeground()

        initialize()
        registerDataReceiver()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startServiceInForeground()
        return START_STICKY
    }

    private fun initialize() {
        MainApplication.instance.setServiceRunning(true)
        // TODO: Support Handle changes of CGM and Pump plugins at runtime
        glucosePlugin = MainApplication.instance.glucosePlugin
        pumpPlugin = MainApplication.instance.pumpPlugin

        installApsPipeline()
    }

    private fun startServiceInForeground() {
        val apsNotificationData = ApsNotificationData.create(apsState)
        val notification: Notification = notificationManager.createForegroundServiceNotification(apsNotificationData)

        startForeground(
            ApsNotificationManager.NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
    }

    private fun registerDataReceiver() {
        val filter = IntentFilter("com.eveningoutpost.dexdrip.BgEstimate")
        registerReceiver(dataReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    private fun installApsPipeline() {
        serviceScope.launch {
            glucosePlugin?.let { installGlucosePipeline(it) }
            installApsCalculationPipeline()
        }
    }

    private suspend fun installGlucosePipeline(glucosePlugin: GlucosePlugin) {
        val sensorType = dataRepository.getOrCreateSensorTypeByName(glucosePlugin.getSensorTypeName())
        val dataProvider =
            dataRepository.getOrCreateDataProviderByName(glucosePlugin.name, glucosePlugin.dataProviderType)
        val tickIntervalSize = Minutes(5)

        glucosePlugin.getValues()
//            .persist(dataRepository, dataProvider, sensorType)
            .smoothGlucosePTWMA(windowSize = Minutes(5), weightSlope = 0.7)
            .collect { bg ->
                apsState.updateBg(bg)
            }
    }

    private fun installApsCalculationPipeline() {
        ToDo.toBeImplemented("Not yet implemented")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(dataReceiver)
        MainApplication.instance.setServiceRunning(false)
        serviceJob.cancel()
        super.onDestroy()
    }
}