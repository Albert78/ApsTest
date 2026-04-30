package de.dh.raaps.core.api

import android.app.Application

interface PumpPlugin {
    val name: String
    // TODO

    fun start(application: Application)
    fun stop()
}