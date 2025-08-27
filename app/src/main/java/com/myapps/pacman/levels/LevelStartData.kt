package com.myapps.pacman.levels

import com.myapps.pacman.utils.Position


data class LevelStartData(
    var mapCharData:List<String> = emptyList(),
    val pacmanDefaultPosition: Position,
    val blinkyDefaultPosition: Position,
    val inkyDefaultPosition: Position,
    val pinkyDefaultPosition: Position,
    val clydeDefaultPosition: Position,
    val homeTargetPosition: Position,
    val ghostHomeXRange: IntRange,
    val ghostHomeYRange: IntRange,
    val blinkyScatterPosition: Position,
    val inkyScatterPosition: Position,
    val pinkyScatterPosition: Position,
    val clydeScatterPosition: Position,
    val doorTarget: Position,
    val width:Int,
    val height:Int,
    val amountOfFood:Int,
    val blinkySpeedDelay:Int,
    val isBell:Boolean
)
