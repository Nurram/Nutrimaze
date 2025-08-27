package com.myapps.pacman.timer

interface   ICentralTimerController {
    fun initTimerFunction()
    fun stopTimerFunction()
    fun addNewTimerController(timerId:String)
    fun startTimerController(timerId:String)
    fun startAllTimersController(timerId: String)
    fun restartTimerController(timerId: String)
    fun pauseTimerController(timerId: String)
    fun unpauseTimerController(timerId: String)
    fun getTimerTicksController(timerId: String): Int
    fun stopTimerController(timerId: String)
    fun stopAllTimersController()
    fun pauseAllTimersController()
    fun unpauseAllTimersController()
    fun removeTimerController(timerId:String)
    fun removeAllTimersController()
}
