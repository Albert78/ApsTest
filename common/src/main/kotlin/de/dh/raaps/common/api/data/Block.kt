package de.dh.raaps.common.api.data

import de.dh.raaps.common.api.MINUTES_PER_DAY

data class Block(val duration: Minutes, val amount: Double)

fun List<Block>.isFullDay(): Boolean =
    sumOf { it.duration.value.toInt() } == MINUTES_PER_DAY