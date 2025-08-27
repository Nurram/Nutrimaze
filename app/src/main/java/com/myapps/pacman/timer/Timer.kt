package com.myapps.pacman.timer

import com.myapps.pacman.modules.qualifiers.DispatcherDefault
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class Timer @Inject constructor(
    @DispatcherDefault
    private val coroutineDispatcher: CoroutineDispatcher,
    private val intervalMillis: Long = 1000L
) : TimerInterface {

    private var currentTicks = AtomicInteger(0)
    private var job: Job? = null
    private var isPaused = false

    override fun init() {
        job?.cancel()
        job = CoroutineScope(coroutineDispatcher).launch {
            while (isActive) {
                if (!isPaused) {
                    currentTicks.incrementAndGet()
                }
                delay(intervalMillis)
            }
        }
    }

    override fun pauseUnpauseTime() {
        isPaused = !isPaused
    }

    override fun resetCounter() {
        currentTicks.set(0)
    }

    override fun stop() {
        job?.cancel()
        currentTicks.set(0)
    }

    override fun getCurrentTicks(): Int = currentTicks.get()
}
