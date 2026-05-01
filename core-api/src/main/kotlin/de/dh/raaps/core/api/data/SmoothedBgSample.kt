package de.dh.raaps.core.api.data

/**
 * Represents a blood glucose sample after smoothing.
 */
data class SmoothedBgSample(
    val origValue: BgValue,
    val smoothedValue: BgValue,
    val sampleKind: BgSampleKind,
    override val timestamp: Timestamp
): HistoricalValue {
    val deviation: BgValue
        get() = BgValue.fromMgDl(smoothedValue.mgdl - origValue.mgdl)

    companion object {
        fun plainValue(value: BgReading): SmoothedBgSample =
            SmoothedBgSample(value.value, value.value, value.sampleKind, value.timestamp)
    }
}

fun BgReading.smoothTo(smoothedValue: BgValue): SmoothedBgSample =
    SmoothedBgSample(this.value, smoothedValue, this.sampleKind, this.timestamp)