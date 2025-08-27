package com.myapps.pacman.game.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.CoroutineContext

class CoroutineSupervisor(private val coroutineDispatcher: CoroutineDispatcher):CoroutineScope {
    private var job = SupervisorJob()
    override var coroutineContext: CoroutineContext =  coroutineDispatcher + job

    fun cancelAll() {
        job.cancelChildren()
    }
    fun onDestroy() {
        job.cancel()
    }

    fun restartJob() {
        if (job.isCancelled) {
            job = SupervisorJob()
            coroutineContext = coroutineDispatcher + job
        }
    }
}
