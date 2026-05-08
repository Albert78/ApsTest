package de.dh.raaps.common.api

interface PumpPlugin {
    val name: String
    // TODO

    fun start()
    fun stop()
}