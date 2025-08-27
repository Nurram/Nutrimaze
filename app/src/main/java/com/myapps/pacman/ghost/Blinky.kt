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

class Blinky(
    currentPosition: Position,
    target: Position,
    scatterTarget: Position,
    doorTarget: Position,
    home: Position,
    homeXRange: IntRange,
    homeYRange: IntRange,
    direction: Direction,
    var blinkyStandardSpeedDelay: Int,
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

    private val _blinkyState = MutableStateFlow(
        GhostData(
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement,
            ghostDelay = actorsMovementsTimerController.getBlinkySpeedDelay().toLong(),
            GhostsIdentifiers.BLINKY
        )
    )
    val blinkyState: StateFlow<GhostData> get() = _blinkyState


    private fun calculateTarget(pacman: Pacman) {
        this.target = pacman.pacmanState.value.pacmanPosition
    }

    suspend fun startMoving(
        currentMap:()-> Matrix<Char>,
        pacman: ()-> Pacman,
        ghostMode: () -> GhostMode
    ) {
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.BLINKY_ENTITY_TYPE) {
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
            this.calculateTarget(pacman)
        }
        calculateDirections(currentMap)
        this.move(this.direction)
        updateSpeedDelay(pacman)
        updateState()
    }

    private fun updateSpeedDelay(pacman: Pacman) {
        val newSpeedDelay = when {
            !this.lifeStatement -> ActorsMovementsTimerController.DEATH_SPEED_DELAY
            pacman.pacmanState.value.energizerStatus -> ActorsMovementsTimerController.FRIGHTENED_SPEED_DELAY
            else -> blinkyStandardSpeedDelay
        }

        if (actorsMovementsTimerController.getBlinkySpeedDelay() != newSpeedDelay) {
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                newSpeedDelay
            )
        }

        _blinkyState.value = _blinkyState.value.copy(
            ghostDelay = newSpeedDelay.toLong()
        )
    }

    private fun updateState() {
        _blinkyState.value = _blinkyState.value.copy(
            ghostPosition = this.currentPosition,
            ghostDirection = this.direction,
            ghostLifeStatement = this.lifeStatement
        )
    }
    fun updateLifeStatement(lifeStatement: Boolean) {
        this.lifeStatement = lifeStatement
        _blinkyState.value = _blinkyState.value.copy(
            ghostLifeStatement = this.lifeStatement
        )
    }

    fun updateDirection(direction: Direction) {
        this.direction = direction
        _blinkyState.value = _blinkyState.value.copy(
            ghostDirection = this.direction
        )
    }

    fun updatePosition(position: Position) {
        this.currentPosition = position
        _blinkyState.value = _blinkyState.value.copy(
            ghostPosition = this.currentPosition
        )
    }

    fun changeSpeedDelay(speedDelay: Long) {
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
            speedDelay.toInt()
        )
        _blinkyState.value = _blinkyState.value.copy(
            ghostDelay = speedDelay
        )
    }
}
