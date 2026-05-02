package de.dh.raaps.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import de.dh.raaps.core.api.ID_UNDEFINED
import de.dh.raaps.core.api.data.BgSampleKind
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.core.api.data.Timestamp

@Entity(
    tableName = "tick_state",
    indices = [
        Index("tick")
    ]
)
data class TickStateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = ID_UNDEFINED,
    val tick: Tick,
    val orig_bg_value: BgValue?,
    val smoothed_bg_value: BgValue?,
    val bg_sample_kind: BgSampleKind?,
    val bg_readig_timestamp: Timestamp?
)
