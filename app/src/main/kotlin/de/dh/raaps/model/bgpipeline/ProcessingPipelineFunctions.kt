package de.dh.raaps.model.bgpipeline

import android.util.Log
import de.dh.raaps.common.api.DataProvider
import de.dh.raaps.common.api.data.BgReading
import de.dh.raaps.common.api.data.BgSampleKind
import de.dh.raaps.common.api.data.BgValue
import de.dh.raaps.common.api.data.Minutes
import de.dh.raaps.common.api.data.SensorType
import de.dh.raaps.common.api.data.SmoothedBgSample
import de.dh.raaps.common.api.data.Tick
import de.dh.raaps.common.api.data.Timestamp
import de.dh.raaps.common.api.data.smoothTo
import de.dh.raaps.data.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlin.math.abs

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
 * Downsamples the data stream by aligning readings to fixed time intervals (ticks).
 *
 * This function divides the timeline into fixed buckets of a specified duration,
 * anchored to midnight at start of the epoch. It ensures that only the first reading
 * encountered within each tick is emitted, while subsequent readings in the same interval
 * are discarded.
 *
 * Attention: This simple sampling function won't work well for timestamps around the tick borders
 * with a bit of jitter. If e.g. the timestamps modulo our tick borders are allmost 0, and the
 * sequence of timestamps are not absolutely equidistant, samples will fall into one or the following
 * tick slot. This will leave some slots empty while others are populated with two values.
 *
 * @param tickInterval The interval size of the returned sample grid in minutes.
 * @return A Flow of Pairs containing:
 *         - [BgReading]: The original BgReading.
 *         - [Tick]: The Tick containing the zero-based index of the sample.
 */
fun Flow<BgReading>.sampleByTick(tickInterval: Minutes): Flow<Pair<BgReading, Tick>> {
    var lastTick = Tick(-1)
    val tickSizeMs: Long = tickInterval.value * 60L * 1000L

    return map { it to Tick((it.timestamp.ms / tickSizeMs).toInt()) }
        .filter { (bg, tick) ->
            if (tick != lastTick) {
                lastTick = tick
                true
            } else {
                Log.i("BG ProcessingPipeline", "Skipping entry in the same tick: $bg, tick=$tick, lastTick=$lastTick")
                false
            }
        }
}

/**
 * Downsamples the data stream by aligning readings to fixed time intervals (ticks) using a stable
 * sampling algorithm.
 *
 * It works like this:
 * - Slot index is computed by simple integer division.
 * - Slot centers are phase-shifted so the first value lies exactly
 *   in the center of its slot.
 * - Jitter is suppressed by stable slot assignment.
 * - Last 10 slot-center errors are averaged.
 * - If average absolute error exceeds threshold, phase is re-initialized
 *   around the current value.
 * - No gap filling: missing slots are simply skipped.
 * - Duplicate slots are suppressed (only first emitted value per slot).
 */
fun Flow<BgReading>.sampleByTickStable(
    tickInterval: Minutes,
    errorWindowSize: Int = 10,
    reinitThresholdMs: Long = 10_000L
): Flow<Pair<BgReading, Tick>> = flow {
    var initialized = false
    var offsetMs = 0L
    var lastTick: Tick? = null

    val tickIntervalMs = tickInterval.inMs()
    val errors = ArrayList<Long>()

    fun centerOn(timestamp: Timestamp) {
        val phase = timestamp.ms % tickIntervalMs
        offsetMs = tickIntervalMs / 2 - phase
        errors.clear()
        initialized = true
    }

    collect { bg ->
        if (!initialized && bg.timestamp.ms != 0L) {
            centerOn(bg.timestamp)
        }

        val tick = Tick(((bg.timestamp.ms + offsetMs) / tickIntervalMs).toInt())

        // Sanity check
        val lTick = lastTick
        if (lTick != null && lTick >= tick) {
            // Skip old value
            return@collect
        }

        val slotCenter = Timestamp(tick.value * tickIntervalMs + tickIntervalMs / 2 - offsetMs)
        val error = bg.timestamp - slotCenter

        errors.addLast(error)
        while (errors.size > errorWindowSize) {
            errors.removeFirst()
        }

        emit(Pair(bg, tick))
        lastTick = tick

        if (errors.size == errorWindowSize) {
            val avgError = errors.sum() / errors.size

            if (abs(avgError) > reinitThresholdMs) {
                centerOn(bg.timestamp)
            }
        }
    }
}

fun Flow<Pair<BgReading, Tick>>.fillGaps(tickInterval: Minutes): Flow<Pair<BgReading, Tick>> {
    var lastTickValue: Int? = null
    var lastBgTimestamp: Timestamp = Timestamp(0)
    val tickSizeMs = tickInterval.value.toLong() * 60 * 1000L

    return transform { (bg, tick) ->
        val currentTickValue = tick.value

        if (lastTickValue != null) {
            // Fill all ticks between last and current
            for (gapTick in (lastTickValue!! + 1) until currentTickValue) {
                // Align at previous reading's time
                val gapTimestamp = Timestamp(lastBgTimestamp.ms + tickSizeMs)
                val invalidReading = BgReading(
                    value = BgValue(0),
                    sampleKind = BgSampleKind.Invalid,
                    timestamp = gapTimestamp
                )
                emit(invalidReading to Tick(gapTick))
            }
        }

        lastTickValue = currentTickValue
        lastBgTimestamp = bg.timestamp
        emit(bg to tick)
    }
}

/**
 * Checks if the last [n] samples in history are all valid values.
 */
private fun isNValidValues(history: List<BgReading>, n: Int): Boolean {
    if (history.size < n) return false
    for (i in history.size - n until history.size - 1) {
        if (history[i].sampleKind == BgSampleKind.Invalid) {
            return false
        }
    }
    return true
}

/**
 * Implementation of Parametrized Time-Weighted Moving Average.
 * If there aren't enough valid values in the given window, this method returns `null`, else it
 * returns the PTWMA-smoothed value.
 */
private fun calculatePTWMA(
    history: List<BgReading>,
    windowStartMs: Long,
    windowMs: Long,
    weightSlope: Double
): BgValue? {
    var weightedSum = 0.0
    var weightTotal = 0.0

    history.forEach { sample ->
        if (sample.sampleKind != BgSampleKind.Invalid) {
            val weight = weightSlope * (sample.timestamp.ms - windowStartMs) + (1.0 - weightSlope) * windowMs
            weightedSum += sample.value.mgdl * weight
            weightTotal += weight
        }
    }

    if (weightTotal <= 0) {
        return null
    }
    return BgValue.fromMgDl((weightedSum / weightTotal).toInt())
}

/**
 * Smoothes the last value in the given window based on a Savitzky-Golay filter (Window 5, Order 2)
 * with special treating for the end border.
 * Operates on the last 3 values of the provided list.
 */
private fun calculateSavitzkyGolayEndBorder3(window: List<BgReading>): BgValue {
    val coeffs = SavitzkyGolayFilterWin5Order2.COEFFS
    // We want the smoothed value for the LAST point, so we cannot apply all 5 coeffs.
    // -> Border treatment for the filter: Center the coeffs at the last entry in the window
    // (at this time we need the smoothed value) and use the last window entry (the most current one)
    // for the last three coeffs. This border treatment seems to be a good compromise.
    val sum = window[window.size - 3].value.mgdl * coeffs[0] + // Center the coeffs at the 3rd value from behind
            window[window.size - 2].value.mgdl * coeffs[1] +
            window[window.size - 1].value.mgdl * (coeffs[2] + coeffs[3] + coeffs[4]) // Use the last entry for the right part of the filter coeffs
    return BgValue.fromMgDl(sum.toInt())
}

fun Flow<BgReading>.smoothGlucoseSmart(
    tickIntervalSize: Minutes = Minutes(5),
    windowSize: Int = 3,
    sgMaxDeviationMgDl: Int
): Flow<SmoothedBgSample> {
    val ptwmaWeightSlope = 0.7
    val windowMs = windowSize.toLong() * tickIntervalSize.inMs()

    val history = mutableListOf<BgReading>() // Indexed by Tick index
    return sampleByTickStable(tickIntervalSize)
        .fillGaps(tickIntervalSize)
        .map { (current, _) ->
            val nowMs = current.timestamp.ms
            val windowStartMs = nowMs - windowMs

            // Maintain a sliding window of the last 3 samples.
            history.add(current)
            for (i in 0 until history.size - windowSize) {
                history.removeAt(0)
            }

            if (current.sampleKind != BgSampleKind.Value) {
                return@map SmoothedBgSample.plainValue(current)
            }

            // -------------------------
            // Filter orchestrator
            // -------------------------

            // If the window is completely filled with valid values:
            // Try Savitzky-Golay (our version requires exactly 3 contiguous valid samples)
            val sgValue = if (isNValidValues(history, 3)) {
                val smoothed = calculateSavitzkyGolayEndBorder3(history)
                // If SG deviates too much from the raw current value, don't use this filter (outlier protection)
                if (abs(smoothed.mgdl - current.value.mgdl) > sgMaxDeviationMgDl) null else smoothed
            } else null

            // Fallback to PTWMA if deviation was too high or if we have invalid values
            val smoothedValue = sgValue ?: calculatePTWMA(history, windowStartMs, windowMs, ptwmaWeightSlope)
            if (smoothedValue != null) current.smoothTo(smoothedValue) else SmoothedBgSample.plainValue(current)
        }
}

/**
 * Smart glucose smoothing for readings in a 5-minute interval.
 * This function dynamically switches between algorithms based on data quality.
 */
fun Flow<BgReading>.smoothGlucoseSmart_5_Minute_Readings(): Flow<SmoothedBgSample> {
    return smoothGlucoseSmart(
        tickIntervalSize = Minutes(5),
        windowSize = 3,
        sgMaxDeviationMgDl = 15
    )
}

/**
 * Smart glucose smoothing for readings in a 1-minute interval.
 * This function dynamically switches between algorithms based on data quality.
 */
fun Flow<BgReading>.smoothGlucoseSmart_1_Minute_Readings(): Flow<SmoothedBgSample> {
    return smoothGlucoseSmart(
        tickIntervalSize = Minutes(1),
        windowSize = 10,
        sgMaxDeviationMgDl = 3
    )
}