package com.myapps.pacman.ghost

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.GhostsIdentifiers
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Position
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Pinky(
    currentPosition: Position,
    target: Position,
    scatterTarget: Position,
    doorTarget: Position,
    home: Position,
    homeXRange: IntRange,
    homeYRange: IntRange,
    direction: Direction,
    private val actorsMovementsTimerController: ActorsMovementsTimerController
) : Ghost(
    currentPosition = currentPosition,
    target = target,
    scatterTarget = scatterTarget,
    doorTarget = doorTarget,
    home = home,
    homeXRange = homeXRange,
    homeYRange = homeYRange,
    direction = direction
) {

    private val _pinkyState = MutableStateFlow(
        GhostData(
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement,
            ghostDelay = actorsMovementsTimerController.getPinkySpeedDelay().toLong(),
            GhostsIdentifiers.PINKY
        )
    )
    val pinkyState: StateFlow<GhostData> get() = _pinkyState
    private fun calculateTarget(pacman: Pacman) {
        val offset = 4
        when (pacman.pacmanState.value.pacmanDirection) {
            Direction.RIGHT -> {
                target =
                    pacman.pacmanState.value.pacmanPosition.copy(positionY = pacman.pacmanState.value.pacmanPosition.positionY + offset)
            }

            Direction.UP -> {
                target =
                    pacman.pacmanState.value.pacmanPosition.copy(positionX = pacman.pacmanState.value.pacmanPosition.positionX - offset)
            }

            Direction.DOWN -> {
                target =
                    pacman.pacmanState.value.pacmanPosition.copy(positionX = pacman.pacmanState.value.pacmanPosition.positionX + offset)
            }

            Direction.LEFT -> {
                target =
                    pacman.pacmanState.value.pacmanPosition.copy(positionY = pacman.pacmanState.value.pacmanPosition.positionY - offset)
            }

            Direction.NOWHERE -> {
                target = pacman.pacmanState.value.pacmanPosition
            }
        }
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: ()->GhostMode
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.PINKY_ENTITY_TYPE){
            updatePosition(
                currentMap(),
                pacman(),
                ghostMode()
            )
        }
    }

    fun updatePosition(
        currentMap: Matrix<Char>,
        pacman: Pacman,
        ghostMode: GhostMode
    ){
        if (checkTransfer(this.currentPosition, this.direction, currentMap)) {
            updateState()
            return
        }
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(this.direction)
        updateSpeedDelay(pacman)
        updateState()
    }

    private fun updateSpeedDelay(pacman: Pacman){
        val newSpeed = when {
            !this.lifeStatement -> ActorsMovementsTimerController.DEATH_SPEED_DELAY
            pacman.pacmanState.value.energizerStatus -> ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
            else -> ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
        }

        if(actorsMovementsTimerController.getPinkySpeedDelay() != newSpeed){
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.PINKY_ENTITY_TYPE,
                newSpeed
            )
        }

        _pinkyState.value = _pinkyState.value.copy(
            ghostDelay = actorsMovementsTimerController.getPinkySpeedDelay().toLong()
        )
    }

    private fun updateState(){
        _pinkyState.value = _pinkyState.value.copy(
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement
        )
    }
    fun updateLifeStatement(lifeStatement: Boolean) {
        this.lifeStatement = lifeStatement
        _pinkyState.value = _pinkyState.value.copy(
            ghostLifeStatement = this.lifeStatement
        )
    }

    fun updateDirection(direction: Direction) {
        this.direction = direction
        _pinkyState.value = _pinkyState.value.copy(
            ghostDirection = this.direction
        )
    }

    fun updatePosition(position: Position) {
        this.currentPosition = position
        _pinkyState.value = _pinkyState.value.copy(
            ghostPosition = this.currentPosition
        )
    }

    fun changeSpeedDelay(speedDelay: Long) {
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PINKY_ENTITY_TYPE,
            speedDelay.toInt()
        )
        _pinkyState.value = _pinkyState.value.copy(
            ghostDelay = speedDelay
        )
    }
}
