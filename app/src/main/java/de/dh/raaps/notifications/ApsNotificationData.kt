package de.dh.raaps.notifications

import de.dh.raaps.core.api.ToDo
import de.dh.raaps.core.api.data.GlucoseUnit
import de.dh.raaps.core.api.data.SmoothedBgSample
import de.dh.raaps.model.APS

data class ApsNotificationData(
    val bgValue: SmoothedBgSample?,
    val unit: GlucoseUnit
) {
    fun getBgValueAsString(): String? {
        val bg = bgValue ?: return null
        return "${bg.origValue.mgdl} -> ${bg.smoothedValue.mgdl} mg/dl"
    }

    companion object {
        fun create(aps: APS): ApsNotificationData {
            val latestBg = aps.getCurrentBg()
            ToDo.toBeImplemented("Take glucose unit from preferences")
            return ApsNotificationData(latestBg, GlucoseUnit.MG_DL)
        }
    }
}