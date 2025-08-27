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

class Inky(
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


    private val _inkyState = MutableStateFlow(
        GhostData(
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement,
            ghostDelay = actorsMovementsTimerController.getInkySpeedDelay().toLong(),
            GhostsIdentifiers.INKY
        )
    )
    val inkyState: StateFlow<GhostData> get() = _inkyState

    private fun calculateTarget(pacman: Pacman, blinkyPosition: Position) {
        var posX = pacman.pacmanState.value.pacmanPosition.positionX
        var posy = pacman.pacmanState.value.pacmanPosition.positionY
        when (pacman.pacmanState.value.pacmanDirection) {
            Direction.RIGHT -> {
                posy += 2
            }

            Direction.LEFT -> {
                posy -= 2
            }

            Direction.UP -> {
                posX -= 2
            }

            Direction.DOWN -> {
                posX += 2
            }

            Direction.NOWHERE -> {}
        }

        val posX1 = posX - blinkyPosition.positionX
        val posY1 = posy - blinkyPosition.positionY

        target = target.copy(positionX = posX1, positionY = posY1)
    }


    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: () -> GhostMode,
        blinkyPosition: () -> Position
    ) {
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.INKY_ENTITY_TYPE) {
            updatePosition(
                currentMap(),
                pacman(),
                ghostMode(),
                blinkyPosition()
            )
        }
    }

    fun updatePosition(
        currentMap: Matrix<Char>,
        pacman: Pacman,
        ghostMode: GhostMode,
        blinkyPosition: Position
    ){
        if (checkTransfer(this.currentPosition, this.direction, currentMap)) {
            updateState()
            return
        }
        this.updateStatus(pacman, ghostMode)
        if (isTargetToCalculate(pacman)) {
            calculateTarget(pacman, blinkyPosition)
        }
        this.calculateDirections(currentMap)
        this.move(this.direction)
        updateSpeedDelay(pacman)
        updateState()
    }

    private fun updateSpeedDelay(pacman: Pacman) {
        val newSpeed = when {
            !this.lifeStatement -> ActorsMovementsTimerController.DEATH_SPEED_DELAY
            pacman.pacmanState.value.energizerStatus -> ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
            else -> ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
        }

        if(actorsMovementsTimerController.getInkySpeedDelay() != newSpeed){
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.INKY_ENTITY_TYPE,
                newSpeed
            )
        }

        _inkyState.value = _inkyState.value.copy(
            ghostDelay = actorsMovementsTimerController.getInkySpeedDelay().toLong()
        )
    }

    private fun updateState(){
        _inkyState.value = _inkyState.value.copy(
            ghostLifeStatement = this.lifeStatement,
            ghostDirection = this.direction,
            ghostPosition = this.currentPosition
        )
    }
    fun updateLifeStatement(lifeStatement: Boolean) {
        this.lifeStatement = lifeStatement
        _inkyState.value = _inkyState.value.copy(
            ghostLifeStatement = this.lifeStatement
        )
    }

    fun updateDirection(direction: Direction) {
        this.direction = direction
        _inkyState.value = _inkyState.value.copy(
            ghostDirection = this.direction
        )
    }

    fun updatePosition(position: Position) {
        this.currentPosition = position
        _inkyState.value = _inkyState.value.copy(
            ghostPosition = this.currentPosition
        )
    }

    fun changeSpeedDelay(speedDelay: Long) {
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.INKY_ENTITY_TYPE,
            speedDelay.toInt()
        )
        _inkyState.value = _inkyState.value.copy(
            ghostDelay = speedDelay
        )
    }

}
