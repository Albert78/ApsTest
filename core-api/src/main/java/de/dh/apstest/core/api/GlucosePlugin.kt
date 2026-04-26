package de.dh.apstest.core.api

import de.dh.apstest.core.api.data.BgReading
import kotlinx.coroutines.flow.Flow

interface GlucosePlugin {
    val name: String
    val dataProviderType: String
    fun getSensorTypeName(): String
    fun getValues(): Flow<BgReading>
}