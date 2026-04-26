package de.dh.apstest.core.api.data

/**
 * A memory-efficient representation of a discrete point in time on a fixed grid.
 *
 * A Tick represents the number of elapsed intervals (of a specific duration)
 * since the Unix Epoch (January 1, 1970). This allows for easy comparison
 * and alignment of data points to a consistent time-grid, regardless of
 * their original high-resolution timestamps.
 */
@JvmInline
value class Tick(val value: Int)