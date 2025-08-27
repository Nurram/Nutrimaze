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

class Clyde(
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

    private val _clydeState = MutableStateFlow(
        GhostData(
            ghostPosition =  this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement,
            ghostDelay = actorsMovementsTimerController.getClydeSpeedDelay().toLong(),
            GhostsIdentifiers.CLYDE
        )
    )

    val clydeState:StateFlow<GhostData> get() = _clydeState
    private fun calculateTarget(pacman: Pacman) {
        val xRange =
            IntRange(pacman.pacmanState.value.pacmanPosition.positionX - 8, pacman.pacmanState.value.pacmanPosition.positionX  + 8)
        val yRange =
            IntRange(pacman.pacmanState.value.pacmanPosition.positionY - 8, pacman.pacmanState.value.pacmanPosition.positionY  + 8)

        target =
            if (xRange.contains(this.currentPosition.positionX) && yRange.contains(this.currentPosition.positionY)) {
                scatterTarget
            } else pacman.pacmanState.value.pacmanPosition
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: ()->GhostMode
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.CLYDE_ENTITY_TYPE){
            updatePosition(
                currentMap(),
                pacman(),
                ghostMode()
            )
        }
    }
    private fun updatePosition(
        currentMap: Matrix<Char>,
        pacman: Pacman,
        ghostMode: GhostMode
    ) {
        if (checkTransfer(this.currentPosition, this.direction, currentMap)) {
            updateState()
            return
        }
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            calculateTarget(pacman)
        }
        this.calculateDirections(currentMap)
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

        if(actorsMovementsTimerController.getClydeSpeedDelay() != newSpeed){
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.CLYDE_ENTITY_TYPE,
                newSpeed
            )
        }

        _clydeState.value = _clydeState.value.copy(
            ghostDelay = actorsMovementsTimerController.getClydeSpeedDelay().toLong()
        )
    }


    private fun updateState(){
        _clydeState.value = _clydeState.value.copy(
            ghostLifeStatement = this.lifeStatement,
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction
        )
    }
    fun updateLifeStatement(lifeStatement: Boolean) {
        this.lifeStatement = lifeStatement
        _clydeState.value = _clydeState.value.copy(
            ghostLifeStatement = this.lifeStatement
        )
    }

    fun updateDirection(direction: Direction) {
        this.direction = direction
        _clydeState.value = _clydeState.value.copy(
            ghostDirection = this.direction
        )
    }

    fun updatePosition(position: Position) {
        this.currentPosition = position
        _clydeState.value = _clydeState.value.copy(
            ghostPosition = this.currentPosition
        )
    }

    fun changeSpeedDelay(speedDelay: Long) {
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.CLYDE_ENTITY_TYPE,
            speedDelay.toInt()
        )
        _clydeState.value = _clydeState.value.copy(
            ghostDelay = speedDelay
        )
    }
}
