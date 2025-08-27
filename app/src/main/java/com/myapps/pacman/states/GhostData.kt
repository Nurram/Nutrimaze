package com.myapps.pacman.states

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import kotlinx.coroutines.Delay

data class GhostData(
    val ghostPosition: Position = Position(-1,-1),
    val ghostDirection: Direction = Direction.NOWHERE,
    val ghostLifeStatement: Boolean = true,
    val ghostDelay: Long = 0L,
    val identifier:String  = ""
)
