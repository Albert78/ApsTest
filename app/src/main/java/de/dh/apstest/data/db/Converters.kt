package de.dh.apstest.data.db

import de.dh.apstest.core.api.DataProvider
import de.dh.apstest.core.api.data.BgReading
import de.dh.apstest.core.api.data.SensorType
import de.dh.apstest.data.db.entities.DataProviderEntity
import de.dh.apstest.data.db.entities.GlucoseReadingEntity
import de.dh.apstest.data.db.entities.SensorTypeEntity

fun BgReading.toNewEntity(dataProviderId: Long, sourceSensorId: Long) = GlucoseReadingEntity(
    value_mgdl = this.value.mgdl,
    sampleKind = this.sampleKind,
    timestamp_ms = this.timestamp.ms,
    fk_data_provider = dataProviderId,
    fk_source_sensor = sourceSensorId
)

fun SensorType.toEntity() = SensorTypeEntity(
    id = this.id,
    name = this.name
)

fun SensorTypeEntity.toModel() = SensorType(
    id = this.id,
    name = this.name
)

fun DataProvider.toEntity() = DataProviderEntity(
    id = this.id,
    name = this.name,
    type = this.type
)

fun DataProviderEntity.toModel() = DataProvider(
    id = this.id,
    name = this.name,
    type = this.type
)