package de.dh.raaps.notifications

import de.dh.raaps.common.api.data.BgSampleKind
import de.dh.raaps.common.api.data.BgValue
import de.dh.raaps.common.api.data.SmoothedBgSample
import de.dh.raaps.model.APS

data class ApsNotificationData(
    val lastBgSample: SmoothedBgSample?,
    val secondToLastBgSample: SmoothedBgSample?
) {
    fun getBgDelta(): BgValue? {
        if (lastBgSample == null || lastBgSample.sampleKind != BgSampleKind.Value
            || secondToLastBgSample == null || secondToLastBgSample.sampleKind != BgSampleKind.Value
        ) {
            return null
        }
        return BgValue.fromMgDl(lastBgSample.origValue.mgdl - secondToLastBgSample.origValue.mgdl)
    }

    companion object {
        fun create(aps: APS): ApsNotificationData {
            val lastBg = aps.getCurrentBg()
            val secondToLastBg = aps.getLastBg()
            return ApsNotificationData(lastBg, secondToLastBg)
        }
    }
}