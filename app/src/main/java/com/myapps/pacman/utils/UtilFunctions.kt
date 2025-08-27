package com.myapps.pacman.utils

import android.content.Context
import android.media.AudioManager
import androidx.core.content.getSystemService
import com.myapps.pacman.levels.LevelStartData
import com.myapps.pacman.utils.matrix.Matrix

fun transformIntoCharMatrix(
    strings: List<String>,
    rows: Int,
    columns: Int
): Matrix<Char> = Matrix<Char>(rows, columns)
    .apply {
        for (i in strings.indices) {
            for (j in strings[i].indices) {
                insertElement(strings[i][j], i, j)
            }
        }
    }


fun getAmountOfFood(strings: List<String>): Int {
    var amountOfFood = 0
    strings.forEach { string ->
        for (i in string.indices) {
            amountOfFood = if (string[i] == '.' || string[i] == 'o') {
                amountOfFood + 1
            } else amountOfFood
        }
    }
    return amountOfFood
}

fun convertPositionToPair(position: Position):Pair<Float,Float>{
    return Pair(position.positionX.toFloat(),position.positionY.toFloat())
}



fun transformLevelsDataIntoMaps(levelsData:Map<Int,LevelStartData>):Map<Int,Matrix<Char>>{
    val maps = mutableMapOf<Int,Matrix<Char>>()

    for(i in 0 until levelsData.size){
        maps[i] = transformIntoCharMatrix(
            levelsData[i]?.mapCharData?: emptyList(),
            rows = levelsData[i]?.height?:0,
            columns = levelsData[i]?.width?:0
        )
    }

    return maps
}

fun transformLevelsDataIntoListsOfDots(levelsData:Map<Int,LevelStartData>):List<Int>{
    val dots = mutableListOf<Int>()

    for(i in 0 until levelsData.size){
        dots.add(levelsData[i]?.amountOfFood?:0)
    }

    return dots
}

