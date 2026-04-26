package de.dh.apstest.core.api.data

import de.dh.apstest.core.api.MINUTES_PER_DAY
import de.dh.apstest.core.api.MINUTES_PER_HOUR

/**
 * A memory-efficient, type-safe representation of a small number of minutes, supported range is
 * from 0 until three weeks.
 */
@JvmInline
value class Minutes(val value: Short) {
    companion object {
        val ONE_HOUR = Minutes(MINUTES_PER_HOUR.toShort())
        val ONE_DAY = Minutes(MINUTES_PER_DAY.toShort())
    }
}