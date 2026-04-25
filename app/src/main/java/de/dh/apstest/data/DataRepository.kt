package de.dh.apstest.data

import de.dh.apstest.core.api.GlucoseReading

class DataRepository(val database: AppDatabase) {
    suspend fun persistReading(reading: GlucoseReading) {
//        database.providersDao().insert(
//            GlucoseEntity(
//                value = reading.value,
//                timestamp = reading.timestamp,
//                unit = reading.unit
//            )
//        )
    }
}