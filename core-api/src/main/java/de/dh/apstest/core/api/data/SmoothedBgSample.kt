package de.dh.apstest.core.api.data

/**
 * Represents a blood glucose sample after smoothing.
 */
data class SmoothedBgSample(
    val origValue: BgValue,
    val smoothedValue: BgValue,
    override val timestamp: Timestamp
): HistoricalValue {
    val deviation: BgValue
        get() = BgValue((smoothedValue.mgdl - origValue.mgdl).toShort())

    companion object {
        fun plainValue(value: BgReading): SmoothedBgSample =
            SmoothedBgSample(value.value, value.value, value.timestamp)
    }
}

fun BgReading.smoothTo(smoothedValue: BgValue): SmoothedBgSample =
    SmoothedBgSample(this.value, smoothedValue, this.timestamp)