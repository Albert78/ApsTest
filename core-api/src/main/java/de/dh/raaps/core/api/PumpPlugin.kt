package de.dh.raaps.core.api

interface PumpPlugin {
    val name: String
    // TODO

    fun start()
    fun stop()
}