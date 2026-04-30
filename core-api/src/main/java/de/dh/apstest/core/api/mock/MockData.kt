package de.dh.apstest.core.api.mock

import de.dh.apstest.core.api.data.BgValue
import de.dh.apstest.core.api.data.Block
import de.dh.apstest.core.api.data.Minutes
import de.dh.apstest.core.api.data.TargetBlock
import de.dh.apstest.core.api.data.TherapyData

fun mockSimpleTherapyData() =
    TherapyData(
        icBlocks = listOf(Block(Minutes.ONE_DAY, 10.0)),
        isfBlocks = listOf(Block(Minutes.ONE_DAY, 44.0)),
        basalBlocks = listOf(Block(Minutes.ONE_DAY, 0.5)),
        targetBlocks = listOf(TargetBlock(Minutes.ONE_DAY, BgValue.fromMgDl(80), BgValue.fromMgDl(150)))
    )