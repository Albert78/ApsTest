package de.dh.raaps.common.api.mock

import de.dh.raaps.common.api.data.BgValue
import de.dh.raaps.common.api.data.Block
import de.dh.raaps.common.api.data.Minutes
import de.dh.raaps.common.api.data.TargetBlock
import de.dh.raaps.common.api.data.TherapyData

fun mockSimpleTherapyData() =
    TherapyData(
        icBlocks = listOf(
            Block(
                Minutes.Companion.ONE_DAY,
                10.0
            )
        ),
        isfBlocks = listOf(
            Block(
                Minutes.Companion.ONE_DAY,
                44.0
            )
        ),
        basalBlocks = listOf(
            Block(
                Minutes.Companion.ONE_DAY,
                0.5
            )
        ),
        targetBlocks = listOf(
            TargetBlock(
                Minutes.Companion.ONE_DAY,
                BgValue.fromMgDl(80),
                BgValue.fromMgDl(150)
            )
        )
    )