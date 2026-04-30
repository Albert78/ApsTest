package de.dh.apstest.notifications

import de.dh.apstest.core.api.ToDo
import de.dh.apstest.core.api.data.BgValue
import de.dh.apstest.core.api.data.GlucoseUnit
import de.dh.apstest.model.ApsHistorySnapshot

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