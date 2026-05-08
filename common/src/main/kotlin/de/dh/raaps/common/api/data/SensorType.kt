package de.dh.raaps.common.api.data

import de.dh.raaps.common.api.ID_UNDEFINED

data class SensorType(
    val id: Long = ID_UNDEFINED,
    val name: String
)