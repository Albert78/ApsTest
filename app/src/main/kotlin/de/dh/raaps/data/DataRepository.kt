package de.dh.raaps.data

import de.dh.raaps.core.api.DataProvider
import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.BgReading
import de.dh.raaps.core.api.data.SensorType
import de.dh.raaps.core.api.data.TherapyData
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.core.api.data.Timestamp
import de.dh.raaps.core.api.mock.mockSimpleTherapyData
import de.dh.raaps.data.db.AppDatabase
import de.dh.raaps.data.db.entities.DataProviderEntity
import de.dh.raaps.data.db.entities.SensorTypeEntity
import de.dh.raaps.data.db.toEntity
import de.dh.raaps.data.db.toModel
import de.dh.raaps.data.db.toNewEntity
import de.dh.raaps.model.ApsTickState

class DataRepository(val database: AppDatabase) {
    suspend fun getOrCreateSensorTypeByName(name: String): SensorType {
        val dao = database.providerDao()
        var entity = dao.getSensorTypeByName(name)
        if (entity == null) {
            entity = SensorTypeEntity(
                name = name
            )
            dao.insertSensorType(entity)
        }
        return entity.toModel()
    }

    suspend fun getOrCreateDataProviderByName(name: String, type: String): DataProvider {
        val dao = database.providerDao()
        var entity = dao.getDataProviderByName(name)
        if (entity == null) {
            entity = DataProviderEntity(
                name = name,
                type = type
            )
            dao.insertDataProvider(entity)
        }
        return entity.toModel()
    }

    /**
     * Insert the given glucose reading from a data provider to the database.
     */
    suspend fun insertDataProviderGlucoseReading(reading: BgReading, dataProvider: DataProvider, sourceSensor: SensorType) {
        database.providerDao().insertGlucoseReading(reading.toNewEntity(dataProvider.id, sourceSensor.id))
        // We don't need to update the reading's ID because it is a disposable object
    }

    /**
     * Gets the therapy data which is or was active at the given time.
     */
    suspend fun getTherapyDataForTimeInstant(time: Timestamp): TherapyData {
        ToDo.toBeImplemented("Calculate/get therapy data for given timestamp")
        return mockSimpleTherapyData()
    }

    /**
     * Inserts the given tick state into the DB, or updates it if it already exists.
     * If the entity existed in the DB, this method will update the ID of the entity to the
     * value in the DB.
     */
    suspend fun insertOrUpdateTickState(tickState: ApsTickState) {
        val id = database.stateDao().insertOrUpdateTickState(tickState.toEntity())
        if (id != -1L) {
            tickState.id = id
        }
    }

    suspend fun getTickStates(fromTick: Tick, toTick: Tick): List<ApsTickState> {
        return database.stateDao().getTickStates(fromTick, toTick).toModel()
    }
}