package de.dh.raaps.data.db

import de.dh.raaps.common.api.DataProvider
import de.dh.raaps.common.api.data.BgReading
import de.dh.raaps.common.api.data.SensorType
import de.dh.raaps.common.api.data.SmoothedBgSample
import de.dh.raaps.data.db.entities.DataProviderEntity
import de.dh.raaps.data.db.entities.GlucoseReadingEntity
import de.dh.raaps.data.db.entities.SensorTypeEntity
import de.dh.raaps.data.db.entities.TickStateEntity
import de.dh.raaps.model.ApsTickState

fun BgReading.toNewEntity(dataProviderId: Long, sourceSensorId: Long) = GlucoseReadingEntity(
    value_mgdl = this.value.mgdl,
    sample_kind = this.sampleKind,
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

fun ApsTickState.toEntity() = TickStateEntity(
    id = this.id,
    tick = this.tick,
    orig_bg_value = this.bg?.origValue,
    smoothed_bg_value = this.bg?.smoothedValue,
    bg_sample_kind = this.bg?.sampleKind,
    bg_readig_timestamp = this.bg?.timestamp
)

fun TickStateEntity.toModel(): ApsTickState {
    val origValue = this.orig_bg_value
    val smoothedValue = this.smoothed_bg_value
    val sampleKind = this.bg_sample_kind
    val timestamp = this.bg_readig_timestamp
    val bg = if (origValue != null && smoothedValue != null && sampleKind != null && timestamp != null)
        SmoothedBgSample(
            origValue,
            smoothedValue,
            sampleKind,
            timestamp
        )
    else null
    return ApsTickState(
        id = this.id,
        tick = this.tick,
        bg = bg
    )
}

fun List<TickStateEntity>.toModel(): List<ApsTickState> {
    return List(this.size, { index -> this[index].toModel() })
}

fun List<ApsTickState>.toEntity(): List<TickStateEntity> {
    return List(this.size, { index -> this[index].toEntity() })
}