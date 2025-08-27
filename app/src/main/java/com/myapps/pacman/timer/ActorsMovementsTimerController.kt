package com.myapps.pacman.timer

import com.myapps.pacman.game.PauseController
import com.myapps.pacman.game.PauseListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ActorsMovementsTimerController {
    companion object {
        // Kecepatan ghost diperlambat (angka lebih besar = lebih lambat)
        const val FRIGHTENED_SPEED_DELAY = 600  // dari 500 ke 600
        const val DEATH_SPEED_DELAY = 120       // dari 100 ke 120
        const val BASE_GHOST_SPEED_DELAY = 300  // dari 250 ke 300
        const val BASE_PACMAN_SPEED_DELAY = 250 // pacman tetap sama
        const val PACMAN_ENTITY_TYPE = "Pacman"
        const val BLINKY_ENTITY_TYPE = "Blinky"
        const val INKY_ENTITY_TYPE = "Inky"
        const val PINKY_ENTITY_TYPE = "Pinky"
        const val CLYDE_ENTITY_TYPE = "Clyde"
    }

    private var movementsPause = PauseController()

    private var pacmanSpeedDelay = BASE_PACMAN_SPEED_DELAY
    private var blinkySpeedDelay = BASE_GHOST_SPEED_DELAY
    private var inkySpeedDelay = BASE_GHOST_SPEED_DELAY
    private var pinkySpeedDelay = BASE_GHOST_SPEED_DELAY
    private var clydeSpeedDelay = BASE_GHOST_SPEED_DELAY

    private fun calculateDelayFactor(entityType: String): Int {
        return when (entityType) {
            PACMAN_ENTITY_TYPE -> pacmanSpeedDelay
            BLINKY_ENTITY_TYPE -> blinkySpeedDelay
            PINKY_ENTITY_TYPE -> pinkySpeedDelay
            INKY_ENTITY_TYPE -> inkySpeedDelay
            CLYDE_ENTITY_TYPE -> clydeSpeedDelay
            else -> BASE_PACMAN_SPEED_DELAY
        }
    }

    fun pause() {
        movementsPause.pause()
    }

    fun resume() {
        movementsPause.resume()
    }

    fun setActorSpeedFactor(entityType: String, newSpeed: Int) {
        when (entityType) {
            PINKY_ENTITY_TYPE -> pinkySpeedDelay = newSpeed
            INKY_ENTITY_TYPE -> inkySpeedDelay = newSpeed
            BLINKY_ENTITY_TYPE -> blinkySpeedDelay = newSpeed
            CLYDE_ENTITY_TYPE -> clydeSpeedDelay = newSpeed
            PACMAN_ENTITY_TYPE -> pacmanSpeedDelay = newSpeed
        }
    }

    fun getBlinkySpeedDelay(): Int = blinkySpeedDelay
    fun getInkySpeedDelay(): Int = inkySpeedDelay
    fun getClydeSpeedDelay(): Int = clydeSpeedDelay
    fun getPinkySpeedDelay(): Int = pinkySpeedDelay
    fun getPacmanSpeedDelay(): Int = pacmanSpeedDelay

    suspend fun controlTime(entityType: String, action: () -> Unit) {
        while (true) {
            if (!movementsPause.isPaused) {
                action()
            }

            if(movementsPause.isPaused){
                awaitResume()
            }
            delay(calculateDelayFactor(entityType).toLong())
        }
    }

    private suspend fun awaitResume() = suspendCancellableCoroutine<Unit> { continuation->
        val listener = object : PauseListener{
            override fun onPause() {
            }

            override fun onResume() {
                if(continuation.isActive){
                    continuation.resume(Unit)
                }
            }
        }

        movementsPause.addListener(listener)

        continuation.invokeOnCancellation {
            movementsPause.removeListener(listener)
        }
    }
}