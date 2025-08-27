package com.myapps.pacman.ghost

import com.myapps.pacman.board.BoardController
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.matrix.Matrix
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.utils.Position
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

open class Ghost(
    var currentPosition: Position,
    var lifeStatement: Boolean = true,
    var target: Position,
    var scatterTarget: Position,
    var doorTarget: Position,
    var home: Position,
    private var canUseDoor: Boolean = false,
    private var mode: GhostMode = GhostMode.CHASE,
    var homeXRange: IntRange,
    var homeYRange: IntRange,
    var direction: Direction
) {


    fun isTargetToCalculate(pacman: Pacman): Boolean {
        if (!this.lifeStatement) {
            this.canUseDoor = true
            this.target = home
            if (this.currentPosition == home) {
                this.lifeStatement = true
            }
            return false
        }
        if (this.isInHome(homeXRange, homeYRange) && pacman.pacmanState.value.energizerStatus) {
            if (currentPosition == home) {
                target = target.copy(positionY = currentPosition.positionY - 1)
            } else if (currentPosition.positionX == home.positionX && currentPosition.positionY == home.positionY - 1) {
                target = target.copy(positionY = home.positionY)
            }
            return false
        }
        if (isInHome(homeXRange, homeYRange) && lifeStatement && !pacman.pacmanState.value.energizerStatus) {
            canUseDoor = true;
            target = doorTarget
            return false;
        }

        canUseDoor = false;

        return when (mode) {
            GhostMode.CHASE -> true
            GhostMode.SCATTER -> {
                target = scatterTarget
                false
            }
        }
    }


    private fun isInHome(homeXRange: IntRange, homeYRange: IntRange): Boolean {
        if (
            homeXRange.contains(this.currentPosition.positionX) &&
            homeYRange.contains(this.currentPosition.positionY)
        ) {
            return true
        }
        return false
    }

    fun updateStatus(pacman: Pacman, status: GhostMode) {
        if (pacman.pacmanState.value.energizerStatus) {
            this.mode = GhostMode.SCATTER
            return
        }
        this.mode = status
    }


    private fun isWallCollision(position: Position, currentMap: Matrix<Char>): Boolean {
        val mapElement = currentMap.getElementByPosition(position.positionX, position.positionY)
        return (mapElement == BoardController.WALL_CHAR || (mapElement == BoardController.GHOST_DOOR_CHAR && !canUseDoor))
    }

    fun getGhostPossiblePosition(position: Position, direction: Direction): Position =
        when (direction) {
            Direction.RIGHT -> position.copy(positionY = position.positionY + 1)
            Direction.LEFT -> position.copy(positionY = position.positionY - 1)
            Direction.DOWN -> position.copy(positionX = position.positionX + 1)
            Direction.UP -> position.copy(positionX = position.positionX - 1)
            Direction.NOWHERE -> position
        }

    fun calculateDirections(currentMap: Matrix<Char>) {
        val distances: MutableList<Float> = mutableListOf()
        val possibleDirections: MutableList<Direction> = mutableListOf()

        for (i in 0..<4) {
            var position = currentPosition
            val direction = getDirectionByNumber(i)
            position = getGhostPossiblePosition(position, direction)

            if (!isWallCollision(position, currentMap)) {
                var distanceX = abs(position.positionY - target.positionY)

                if (distanceX > currentMap.getColumns() / 2) {
                    distanceX = currentMap.getColumns() - distanceX
                }

                val distance = sqrt(
                    distanceX.toFloat().pow(2) + (position.positionX - target.positionX).toFloat()
                        .pow(2)
                )
                distances.add(distance)
                possibleDirections.add(direction)
            }
        }

        if (possibleDirections.size == 1) {
            this.direction = possibleDirections[0]
            return
        }

        sortDirections(distances, possibleDirections)

        for (i in possibleDirections.indices) {
            if (possibleDirections[i] != getOppositeDirection(this.direction)) {
                this.direction = possibleDirections[i]
                return
            }
        }
    }


    private fun getDirectionByNumber(index: Int): Direction =
        when (index) {
            0 -> Direction.RIGHT
            1 -> Direction.UP
            2 -> Direction.LEFT
            3 -> Direction.DOWN
            else -> Direction.NOWHERE
        }


    private fun sortDirections(
        distances: MutableList<Float>,
        possibleDirections: MutableList<Direction>
    ) {
        for (i in 0..<distances.size) {
            for (j in 0..<distances.size) {

                if (distances[i] < distances[j]) {
                    val dist = distances[i]
                    distances[i] = distances[j]
                    distances[j] = dist

                    val dir = possibleDirections[i]
                    possibleDirections[i] = possibleDirections[j]
                    possibleDirections[j] = dir
                }

            }
        }
    }

    private fun getOppositeDirection(direction: Direction): Direction =
        when (direction) {
            Direction.RIGHT -> Direction.LEFT
            Direction.UP -> Direction.DOWN
            Direction.LEFT -> Direction.RIGHT
            Direction.DOWN -> Direction.UP
            Direction.NOWHERE -> Direction.NOWHERE
        }

    fun move(direction: Direction) {
        when (direction) {
            Direction.RIGHT -> currentPosition =
                currentPosition.copy(positionY = currentPosition.positionY + 1)

            Direction.LEFT -> currentPosition =
                currentPosition.copy(positionY = currentPosition.positionY - 1)

            Direction.UP -> currentPosition =
                currentPosition.copy(positionX = currentPosition.positionX - 1)

            Direction.DOWN -> currentPosition =
                currentPosition.copy(positionX = currentPosition.positionX + 1)

            Direction.NOWHERE -> {}
        }
    }


    fun checkTransfer(
        position: Position,
        direction: Direction,
        currentMap: Matrix<Char>
    ): Boolean = when (direction) {
        Direction.RIGHT -> {
            if (position.positionY >= currentMap.getColumns()) {
                currentPosition = currentPosition.copy(positionY = 0)
                true
            } else false
        }
        Direction.LEFT -> {
            if (position.positionY < 0) {
                currentPosition = currentPosition.copy(positionY = currentMap.getColumns() - 1)
                true
            } else false
        }
        else -> false
    }
}
