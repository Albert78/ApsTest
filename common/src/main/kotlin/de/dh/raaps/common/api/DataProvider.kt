package de.dh.raaps.common.api

data class DataProvider(
    val id: Long = ID_UNDEFINED,
    val name: String,
    val type: String
)