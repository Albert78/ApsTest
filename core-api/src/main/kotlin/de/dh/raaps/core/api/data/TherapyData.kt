package de.dh.raaps.core.api.data

import de.dh.raaps.core.api.ID_UNDEFINED

data class TherapyData(
    val id: Long = ID_UNDEFINED,
    val basalBlocks: List<Block>,
    val isfBlocks: List<Block>,
    val icBlocks: List<Block>,
    val targetBlocks: List<TargetBlock>
)

