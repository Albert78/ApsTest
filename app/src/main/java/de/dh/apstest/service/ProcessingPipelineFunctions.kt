package de.dh.apstest.service

import de.dh.apstest.core.api.DataProvider
import de.dh.apstest.core.api.data.BgReading
import de.dh.apstest.core.api.data.BgSampleKind
import de.dh.apstest.core.api.data.BgValue
import de.dh.apstest.core.api.data.HistoricalValue
import de.dh.apstest.core.api.data.Minutes
import de.dh.apstest.core.api.data.SensorType
import de.dh.apstest.core.api.data.SmoothedBgSample
import de.dh.apstest.core.api.data.Tick
import de.dh.apstest.core.api.data.smoothTo
import de.dh.apstest.data.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Persists each [BgReading] emitted by the flow to the persistent storage.
 *
 * This function acts as a side effect operator within the pipeline. It uses [onEach]
 * to intercept the stream and perform a database insertion via the [DataRepository]
 * without modifying the readings. This allows the data to continue flowing to
 * subsequent processing stages (like sampling or smoothing) unchanged.
 *
 * @param dataRepository The repository responsible for database operations.
 * @param dataProvider The source entity that provided the glucose data.
 * @param sourceSensor The type of sensor from which the reading originated.
 * @return A Flow that emits the same [BgReading] objects after the persistence side effect.
 */
fun Flow<BgReading>.persist(
    dataRepository: DataRepository,
    dataProvider: DataProvider,
    sourceSensor: SensorType
): Flow<BgReading> = onEach { reading ->
    dataRepository.insertDataProviderGlucoseReading(reading, dataProvider, sourceSensor)
}

/**
 * Advanced smoothing for blood glucose samples using a Parametrized, Time-Weighted Moving Average (PTWMA)
 * to reduce sensor noise while preserving trends.
 *
 * This filter uses a trapezoidal time-window to weight samples.
 *
 * 1. Ignores INVALID samples.
 * 2. Treats HIGH/LOW as floor/ceiling values to maintain urgent safety states.
 * 3. Applies a weighted average where weights decay based on sample age.
 *
 * @param windowSize The look-back duration (e.g., 10 minutes). A larger window increases
 *                   smoothness but also adds lag to trend detection.
 * @param weightSlope Controls the "aggressiveness" of the time-decay (0.0 to 1.0):
 *                    - 0.0: Constant weight (Simple Moving Average). Maximum smoothing,
 *                           but highest lag.
 *                    - 1.0: Linear decay starting from zero at window edge (Triangular window).
 *                           High responsiveness to new data, ignores very old data.
 *                    - 0.7 (Default): Balanced trapezoidal weight. Reduces noise while
 *                           keeping the trend responsive.
 */
fun Flow<BgReading>.smoothGlucosePTWMA(windowSize: Minutes = Minutes(10), weightSlope: Double = 0.7): Flow<SmoothedBgSample> {
    val history = mutableListOf<BgReading>()
    val windowMs = windowSize.value.toLong() * 60 * 1000L

    return filter { it.sampleKind != BgSampleKind.Invalid }
        .map { current ->
            val nowMs = current.timestamp.ms
            val windowStartMs = nowMs - windowMs

            // Remove history values outside our window
            history.removeAll { it.timestamp.ms < windowStartMs }

            if (current.sampleKind != BgSampleKind.Value) {
                // If it's HIGH or LOW, don't smooth it with normal values
                // to maintain the urgent nature of the reading.
                SmoothedBgSample.plainValue(current)
            } else {
                // Only add real numerical values to the smoothing history
                history.add(current)

                // Time-Weighted Moving Average: Newer values have more weight based on their age
                var weightedSum = 0.0
                var weightTotal = 0.0

                history.forEach { sample ->
                    // slope: 1.0 = linear descending until 0, 0.0 = constant (Simple Moving Average, SMA)
                    val weight = weightSlope * (sample.timestamp.ms - windowStartMs) + (1.0 - weightSlope) * windowMs
                    weightedSum += sample.value.mgdl * weight
                    weightTotal += weight
                }

                if (weightTotal > 0) {
                    val smoothedMgDl = (weightedSum / weightTotal).toInt()
                    current.smoothTo(BgValue.fromMgDl(smoothedMgDl))
                } else {
                    SmoothedBgSample.plainValue(current)
                }
            }
        }
}

/**
 * Downsamples the data stream by aligning readings to fixed time intervals (ticks).
 *
 * This function divides the timeline into fixed buckets of a specified duration,
 * anchored to midnight at start of the epoch. It ensures that only the first reading
 * encountered within each tick is emitted, while subsequent readings in the same interval
 * are discarded.
 *
 * @param tickIntervalSize The interval size of the returned sample grid in minutes.
 * @return A Flow of Pairs containing:
 *         - [T]: The original value (must implement HistoricalValue).
 *         - [Tick]: The zero-based index of the tick.
 */
fun <T : HistoricalValue> Flow<T>.sampleByTick(tickIntervalSize: Minutes): Flow<Pair<T, Tick>> {
    var lastTick = Tick(-1)
    val tickSizeMs = tickIntervalSize.value * 60 * 1000

    return map { it to Tick((it.timestamp.ms  / tickSizeMs).toInt()) }
        .filter { (_, tick) ->
            if (tick != lastTick) {
                lastTick = tick
                true
            } else {
                false
            }
        }
}