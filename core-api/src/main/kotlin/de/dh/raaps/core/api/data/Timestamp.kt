package de.dh.raaps.core.api.data

import kotlin.time.Instant

/**
 * A memory-efficient, type-safe representation of a timestamp in Milliseconds since
 * the Unix Epoch (January 1, 1970).
 */
@JvmInline
value class Timestamp(val ms: Long) : Comparable<Timestamp> {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)

    operator fun minus(other: Timestamp): Long = ms - other.ms

    fun toInstant(): Instant = Instant.fromEpochMilliseconds(ms)

    companion object {
        fun now(): Timestamp  = Timestamp(System.currentTimeMillis())
    }
}
