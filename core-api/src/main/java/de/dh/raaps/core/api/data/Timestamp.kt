package de.dh.raaps.core.api.data

/**
 * A memory-efficient, type-safe representation of a timestamp in Milliseconds since
 * the Unix Epoch (January 1, 1970).
 */
@JvmInline
value class Timestamp(val ms: Long) : Comparable<Timestamp> {
    override fun compareTo(other: Timestamp): Int = ms.compareTo(other.ms)
}