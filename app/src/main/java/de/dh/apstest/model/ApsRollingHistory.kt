package de.dh.apstest.model

import de.dh.apstest.core.api.data.Minutes
import de.dh.apstest.core.api.data.Tick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ApsRollingHistory(
    val historyHours: Int = 10,
    val tickDuration: Minutes = Minutes(5)
) {
    private val capacity = (historyHours * 60) / tickDuration.value.toInt()
    // Ring buffer which holds our history window
    private val buffer = arrayOfNulls<ApsTickState>(capacity)

    // The "Present": All data in the buffer is relative to this tick
    private var anchorTick: Int = -1

    // Emits the buffer as unmodifiable list to all observers (e.g. UI)
    private val _state = MutableStateFlow<List<ApsTickState?>>(List(capacity) { null })
    val state: StateFlow<List<ApsTickState?>> = _state.asStateFlow()

    /**
     * Advances the history's timeframe to a new point in time.
     * Use this to move the "window" forward, e.g., every 5 minutes or on new data.
     */
    fun advanceTo(newAnchorTick: Tick) {
        val newTickValue = newAnchorTick.value
        if (anchorTick == -1) {
            anchorTick = newTickValue
            return
        }

        if (newTickValue > anchorTick) {
            // Clear the slots that have become "stale" due to time advancing
            val ticksToClear = (newTickValue - anchorTick).coerceAtMost(capacity)
            for (i in 1..ticksToClear) {
                buffer[bufferIndex(Tick(anchorTick + i))] = ApsTickState.empty()
                // TODO: Write ApsState to database
            }
            anchorTick = newTickValue
            publishState()
        }
    }

    /**
     * Tries to get an entry of our state history.
     * Only succeeds if the given tick falls within the current history window.
     */
    fun getApsTickState(tick: Tick, advanceToTick: Boolean): ApsTickState? {
        // Ensure the anchor is at least as new as the incoming data
        if (tick.value > anchorTick && advanceToTick) {
            advanceTo(tick)
        }

        val minValidTick = anchorTick - capacity + 1

        if (tick.value in minValidTick..anchorTick) {
            return buffer[bufferIndex(tick)]
        }
        return null
    }

    fun publishState() {
        _state.value = buffer.toList()
    }

    private fun bufferIndex(tick: Tick): Int {
        return tick.value % capacity
    }

    /**
     * Synchronous access to the current history.
     */
    fun getSnapshot(): List<ApsTickState?> = _state.value
}