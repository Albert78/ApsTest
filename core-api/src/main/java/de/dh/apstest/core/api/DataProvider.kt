package de.dh.apstest.core.api

data class DataProvider(
    val id: Long = ID_UNDEFINED,
    val name: String,
    val type: String
)