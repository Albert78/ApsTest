package de.dh.apstest.core.api.data

import de.dh.apstest.core.api.MINUTES_PER_DAY

data class TargetBlock(val duration: Minutes, val lowTarget: BgValue, val highTarget: BgValue)

fun List<TargetBlock>.isFullDay(): Boolean =
    sumOf { it.duration.value.toInt() } == MINUTES_PER_DAY