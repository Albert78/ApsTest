package de.dh.raaps

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import de.dh.raaps.core.api.GlucosePlugin
import de.dh.raaps.core.api.PumpPlugin
import de.dh.raaps.data.DataRepository
import de.dh.raaps.data.db.AppDatabase
import de.dh.raaps.model.ApsState
import de.dh.raaps.notifications.ApsNotificationData
import de.dh.raaps.notifications.ApsNotificationManager
import de.dh.raaps.plugin.glucose.receiver.ReceiverGlucosePlugin
import de.dh.raaps.plugin.pump.SamplePumpPlugin
import de.dh.raaps.service.ApsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainApplication : Application() {
    lateinit var notificationManager: ApsNotificationManager
        private set
    lateinit var appStateRepository: AppStateRepository
        private set
    lateinit var dataRepository: DataRepository
        private set
    lateinit var apsState: ApsState
        private set
    var glucosePlugin: GlucosePlugin? = null
    var pumpPlugin: PumpPlugin? = null

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _isServiceRunning = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        instance = this

        notificationManager = ApsNotificationManager(this)
        appStateRepository = AppStateRepository(context = this, scope = applicationScope)
        val appDatabase = AppDatabase.getInstance(this)
        dataRepository = DataRepository(appDatabase)
        // TODO: Decouple initialization of APS system
        apsState = ApsState(dataRepository)
        glucosePlugin = ReceiverGlucosePlugin()
        pumpPlugin = SamplePumpPlugin()
        startApsService()
        installNotificationUpdater()
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    fun triggerUpdatesAfterPermissionsChange() {
        startApsService()
    }

    fun startApsService() {
        val intent = Intent(this, ApsService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    fun installNotificationUpdater() {
        applicationScope.launch {
            apsState.lastDataTime.collect { timestamp ->
                val notificationData = ApsNotificationData.create(apsState)
                notificationManager.updateNotification(notificationData)
            }
        }
    }

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}