package de.dh.raaps.model

import android.util.Log
import de.dh.raaps.core.api.data.Minutes
import de.dh.raaps.core.api.data.Tick
import de.dh.raaps.core.api.data.Timestamp

data class ApsHistorySnapshot(
    val ticks: List<ApsTickState?>,
    val tickInterval: Minutes
)

class ApsRollingHistory(
    val historyHours: Int = 10,
    val tickDuration: Minutes = Minutes(5)
) {
    private val capacity = (historyHours * 60) / tickDuration.value.toInt()
    // Ring buffer which holds our history window
    private val buffer = arrayOfNulls<ApsTickState>(capacity)

    // The "Present": All data in the buffer is relative to this tick
    var anchorTick: Tick = Tick.invalid()

    fun tick(timestamp: Timestamp): Tick {
        val tickSizeMs = tickDuration.value * 60 * 1000

        return Tick((timestamp.ms  / tickSizeMs).toInt())
    }

    fun getNowTick(): Tick {
        return tick(Timestamp.now())
    }

    fun getFirstTick() = Tick(anchorTick.value - capacity + 1)

    /**
     * Advances the history's timeframe to a new point in time.
     * Use this to move the "window" forward, e.g., every 5 minutes or on new data.
     */
    fun advanceTo(newAnchorTick: Tick) {
        if (anchorTick == Tick.invalid()) {
            anchorTick = newAnchorTick
            buffer[bufferIndex(newAnchorTick)] = ApsTickState.empty(newAnchorTick)
            return
        }

        if (newAnchorTick > anchorTick) {
            // Clear the slots that have become "stale" due to time advancing
            val ticksToClear = (newAnchorTick.value - anchorTick.value).coerceAtMost(capacity)
            for (i in 1..ticksToClear) {
                val tick = Tick(anchorTick.value + i)
                buffer[bufferIndex(tick)] = ApsTickState.empty(tick)
                // TODO: Write ApsState to database
            }
            anchorTick = newAnchorTick
        }
    }

    /**
     * Tries to get an entry of our state history.
     * Only succeeds if the given tick falls within the current history window.
     */
    fun getApsTickState(tick: Tick, advanceToTick: Boolean): ApsTickState? {
        // Ensure the anchor is at least as new as the incoming data
        if (tick > anchorTick && advanceToTick) {
            advanceTo(tick)
        }

        val minValidTick = getFirstTick().value

        if (tick.value in minValidTick..anchorTick.value) {
            return buffer[bufferIndex(tick)]
        }
        return null
    }

    private fun bufferIndex(tick: Tick): Int {
        return tick.value % capacity
    }

    fun replaceBufferTickStates(tickStates: List<ApsTickState>) {
        // Clear buffer
        for (i in 0..<capacity) {
            buffer[i] = null
        }

        // Fill given tick states into buffer
        val firstTick = getFirstTick()
        for (loadedState in tickStates) {
            if (loadedState.tick !in firstTick..anchorTick) {
                Log.w(TAG, "Trying to fill tick state into history with invalid tick number: Tick = ${loadedState.tick.value}, FirstTick = ${firstTick.value}, AnchorTick = ${anchorTick.value}")
                continue
            }
            val index = bufferIndex(loadedState.tick)
            if (index in 0..<capacity) {
                buffer[index] = loadedState
            }
        }

        // Fill empty buffer spaces with empty tick states
        for (i in 0..<capacity) {
            val tick = Tick(firstTick.value + i)
            val index = bufferIndex(tick)
            if (buffer[index] == null) {
                buffer[index] = ApsTickState.empty(tick)
            }
        }
    }

    /**
     * Synchronous access to a chronological snapshot of the current history.
     * The list is ordered from oldest (index 0) to newest.
     */
    fun getSnapshot(): ApsHistorySnapshot {
        val currentAnchor = anchorTick
        if (currentAnchor == Tick.invalid()) {
            return ApsHistorySnapshot(List(capacity) { null }, tickDuration)
        }

        val result = List(capacity) { i ->
            val tickValue = currentAnchor.value - capacity + 1 + i
            if (tickValue >= 0) {
                buffer[bufferIndex(Tick(tickValue)) % capacity]
            } else {
                null
            }
        }
        return ApsHistorySnapshot(result, tickDuration)
    }

    companion object {
        val TAG = ApsRollingHistory::class.simpleName
    }
}
