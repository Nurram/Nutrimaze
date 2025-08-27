package com.myapps.pacman.timer


interface TimerInterface {
    fun init()
    fun pauseUnpauseTime()
    fun resetCounter()
    fun stop()
    fun getCurrentTicks():Int
}
