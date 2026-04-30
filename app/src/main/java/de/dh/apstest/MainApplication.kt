package de.dh.apstest

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import de.dh.apstest.data.DataRepository
import de.dh.apstest.data.db.AppDatabase
import de.dh.apstest.model.ApsState
import de.dh.apstest.notifications.ApsNotificationManager
import de.dh.apstest.service.ApsService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MainApplication : Application() {
    lateinit var notificationManager: ApsNotificationManager
        private set
    lateinit var appStateRepository: AppStateRepository
        private set
    lateinit var dataRepository: DataRepository
        private set
    lateinit var apsState: ApsState
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        notificationManager = ApsNotificationManager(this)
        appStateRepository = AppStateRepository(context = this, scope = applicationScope)
        val appDatabase = AppDatabase.getInstance(this)
        dataRepository = DataRepository(appDatabase)
        // TODO: Decouple initialization of APS system
        apsState = ApsState(dataRepository)
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    fun triggerUpdatesAfterPermissionsChange() {
        val intent = Intent(this, ApsService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}