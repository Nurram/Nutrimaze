package com.myapps.pacman.levels

import android.content.Context

interface MapProvider {
    fun getMaps():Map<Int,LevelStartData>
}
