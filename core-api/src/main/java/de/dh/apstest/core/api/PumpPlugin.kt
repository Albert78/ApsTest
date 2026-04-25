package de.dh.apstest.core.api

interface PumpPlugin {
    val name: String
    suspend fun deliverInsulin(units: Double): Boolean
    suspend fun getBasalRate(): Double
}