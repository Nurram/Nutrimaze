package com.myapps.pacman.game

import java.util.concurrent.CopyOnWriteArrayList

class PauseController {
    private val listeners = CopyOnWriteArrayList<PauseListener>()
    var isPaused = false
        private set


    fun addListener(listener: PauseListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PauseListener) {
        listeners.remove(listener)
    }

    fun pause(action:(()->Unit)?=null) {
        isPaused = true
        action?.invoke()
        listeners.forEach { it.onPause() }
    }

    fun resume(action:(()->Unit)?=null) {
        isPaused = false
        action?.invoke()
        listeners.forEach { it.onResume() }
    }
}
