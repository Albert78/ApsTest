package de.dh.apstest

import android.app.Application
import de.dh.apstest.data.AppDatabase
import de.dh.apstest.data.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

class MainApplication : Application() {
    lateinit var appStateRepository: AppStateRepository
        private set
    lateinit var dataRepository: DataRepository
        private set

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        instance = this

        appStateRepository = AppStateRepository(context = this, scope = applicationScope)
        val appDatabase = AppDatabase.getInstance(this)
        dataRepository = DataRepository(appDatabase)
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }

    companion object {
        lateinit var instance: MainApplication
            private set
    }
}