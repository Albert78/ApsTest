package de.dh.apstest.core.api.data

import de.dh.apstest.core.api.ID_UNDEFINED

data class SensorType(
    val id: Long = ID_UNDEFINED,
    val name: String
)