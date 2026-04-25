package de.dh.apstest

import android.app.Application
import androidx.room.Room
import de.dh.apstest.data.AppDatabase

class MainApplication : Application() {
    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "aps-database"
        ).build()
    }
}