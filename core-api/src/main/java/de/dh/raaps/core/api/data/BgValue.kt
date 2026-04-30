package de.dh.raaps.core.api.data

import kotlin.math.roundToInt

private const val MMOL_TO_MGDL = 18.0182

/**
 * A memory-efficient, type-safe representation of a blood glucose value.
 */
@JvmInline
value class BgValue(val mgdl: Short) {
    val mmol: Double
        get() = (mgdl / MMOL_TO_MGDL * 10.0).roundToInt() / 10.0

    companion object {
        fun fromMgDl(value: Short): BgValue {
            return BgValue(value)
        }

        fun fromMgDl(value: Int): BgValue {
            return BgValue(value.toShort())
        }

        fun fromMmol(value: Double): BgValue {
            return fromMgDl((value * MMOL_TO_MGDL).toInt())
        }
    }
}

