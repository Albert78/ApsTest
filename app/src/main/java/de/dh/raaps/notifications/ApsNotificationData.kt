package de.dh.raaps.notifications

import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.BgValue
import de.dh.raaps.core.api.data.GlucoseUnit
import de.dh.raaps.model.ApsHistorySnapshot

data class ApsNotificationData(
    val bgValue: BgValue?,
    val unit: GlucoseUnit
) {
    fun getBgValueAsString(): String {
        return if (bgValue == null) "?" else "${bgValue.mgdl} mg/dl"
    }

    companion object {
        fun create(apsHistorySnapshot: ApsHistorySnapshot): ApsNotificationData {
            val latestBg = apsHistorySnapshot.ticks.lastOrNull { it?.bg != null }?.bg?.smoothedValue
            ToDo.toBeImplemented("Take glucose unit from preferences")
            return ApsNotificationData(latestBg, GlucoseUnit.MG_DL)
        }
    }
}

