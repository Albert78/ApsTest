package de.dh.apstest.data

import de.dh.apstest.core.api.DataProvider
import de.dh.apstest.core.api.ToDo
import de.dh.apstest.core.api.data.BgReading
import de.dh.apstest.core.api.data.SensorType
import de.dh.apstest.core.api.data.TherapyData
import de.dh.apstest.core.api.data.Timestamp
import de.dh.apstest.core.api.mock.mockSimpleTherapyData
import de.dh.apstest.data.db.AppDatabase
import de.dh.apstest.data.db.entities.DataProviderEntity
import de.dh.apstest.data.db.entities.SensorTypeEntity
import de.dh.apstest.data.db.toModel
import de.dh.apstest.data.db.toNewEntity

class DataRepository(val database: AppDatabase) {
    suspend fun getOrCreateSensorTypeByName(name: String): SensorType {
        val dao = database.providersDao()
        var res = dao.getSensorTypeByName(name)
        if (res == null) {
            res = SensorTypeEntity(
                name = name
            )
            dao.insertSensorType(res)
        }
        return res.toModel()
    }

    suspend fun getOrCreateDataProviderByName(name: String, type: String): DataProvider {
        val dao = database.providersDao()
        var res = dao.getDataProviderByName(name)
        if (res == null) {
            res = DataProviderEntity(
                name = name,
                type = type
            )
            dao.insertDataProvider(res)
        }
        return res.toModel()
    }

    /**
     * Insert the given glucose reading from a data provider to the database.
     */
    suspend fun insertDataProviderGlucoseReading(reading: BgReading, dataProvider: DataProvider, sourceSensor: SensorType) {
        database.providersDao().insertGlucoseReading(reading.toNewEntity(dataProvider.id, sourceSensor.id))
    }

    /**
     * Gets the therapy data which is or was active at the given time.
     */
    suspend fun getTherapyDataForTimeInstant(time: Timestamp): TherapyData {
        ToDo.toBeImplemented("Calculate/get therapy data for given timestamp")
        return mockSimpleTherapyData()
    }
}