package com.myapps.pacman.timer

import javax.inject.Inject

class TimerController(
    val timer: TimerInterface
) {
    private var startTicks = 0
    private var pausedTicks = 0
    private var isPaused = false
    private var isStarted = false

    fun start() {
        isStarted = true
        isPaused = false
        startTicks = timer.getCurrentTicks()
        pausedTicks = 0
    }

    fun reset() {
        startTicks = 0
        pausedTicks = 0
        isPaused = false
        isStarted = false
    }

    fun restart() {
        this.reset()
        this.start()
    }

    fun pause() {
        if (isStarted && !isPaused) {
            isPaused = true
            pausedTicks = timer.getCurrentTicks() - startTicks
            startTicks = 0
        }
    }

    fun unpause() {
        if (isStarted && isPaused) {
            isPaused = false
            startTicks = timer.getCurrentTicks() - pausedTicks
            pausedTicks = 0
        }
    }

    fun getTicks(): Int {
        return if (isStarted) {
            if (isPaused) {
                pausedTicks
            } else {
                timer.getCurrentTicks() - startTicks
            }
        } else 0
    }
}
