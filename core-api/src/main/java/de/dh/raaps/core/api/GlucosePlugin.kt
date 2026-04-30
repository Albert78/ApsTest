package de.dh.raaps.core.api

import android.app.Application
import de.dh.raaps.core.api.data.BgReading
import de.dh.raaps.core.api.data.BgReadingsInterval
import de.dh.raaps.core.api.data.Minutes
import kotlinx.coroutines.flow.Flow

interface GlucosePlugin {
    val name: String
    val dataProviderType: String
    val readingsInterval: BgReadingsInterval
    val readingsTimeDelay: Minutes
    fun getSensorTypeName(): String
    fun getValues(): Flow<BgReading>

    fun start(application: Application)
    fun stop()
}