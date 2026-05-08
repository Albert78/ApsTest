package de.dh.raaps.common.api

import kotlinx.coroutines.flow.Flow

interface GlucosePlugin {
    val name: String
    val dataProviderType: String
    val readingsInterval: de.dh.raaps.common.api.data.BgReadingsInterval
    val readingsTimeDelay: de.dh.raaps.common.api.data.Minutes
    fun getSensorTypeName(): String
    fun getValues(): Flow<de.dh.raaps.common.api.data.BgReading>

    fun start()
    fun stop()
}