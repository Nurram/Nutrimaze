package com.myapps.pacman.board

import com.myapps.pacman.game.GameConstants
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BoardController(
    private val maps: Map<Int, Matrix<Char>>,
    private var currentLevel: Int = 0,
    private var pacmanLives: Int = GameConstants.PACMAN_LIVES,
    private var score: Int = 0,
    private var dots: List<Int>,
    private var gameStatus: GameStatus = GameStatus.ONGOING
) {

    companion object {
        const val WALL_CHAR = '|'
        const val EMPTY_SPACE = ' '
        const val BLANK_SPACE = '_'
        const val PELLET_CHAR = '.'
        const val ENERGIZER_CHAR = 'o'
        const val BELL_CHAR = 'c'
        const val GHOST_DOOR_CHAR = '='

        // Tambahkan karakter makanan sehat
        const val RICE_CHAR = 'n'
        const val FISH_CHAR = 'i'
        const val VEGETABLE_CHAR = 'v'
        const val FRUIT_CHAR = 'b'
    }

    private var currentMap = maps[currentLevel]?.copy()
    private var currentDots = dots[currentLevel]

    private val _boardState = MutableStateFlow(
        BoardData(
            gameBoardData = currentMap ?: Matrix(0, 0),
            score = score,
            pacmanLives = pacmanLives,
            currentLevel = currentLevel,
            gameStatus = gameStatus,
            remainFood = this.currentDots
        )
    )

    val boardState: StateFlow<BoardData> get() = _boardState

    fun updateCurrentLevel() {
        this.currentLevel += 1
        this.currentMap = maps[currentLevel]?.copy()
        this.currentDots = dots[currentLevel]
        this.pacmanLives = GameConstants.PACMAN_LIVES
        _boardState.value = _boardState.value.copy(
            currentLevel = this.currentLevel,
            gameBoardData = this.currentMap ?: Matrix(0, 0),
            remainFood = this.currentDots,
            pacmanLives = this.pacmanLives
        )
    }

    fun decreasePacmanLives() {
        if(this.pacmanLives>0) this.pacmanLives -= 1
        _boardState.value = _boardState.value.copy(
            pacmanLives = this.pacmanLives
        )
        if (this.pacmanLives == 0) {
            _boardState.value = _boardState.value.copy(
                gameStatus = GameStatus.LOSE
            )
        }
    }

    fun resetBoardData() {
        this.score = 0
        this.currentLevel = 0
        this.pacmanLives = GameConstants.PACMAN_LIVES
        this.gameStatus = GameStatus.ONGOING
        this.currentDots = dots[currentLevel]
        this.currentMap = maps[currentLevel]?.copy()
        _boardState.value = _boardState.value.copy(
            gameBoardData =  currentMap?:Matrix(0,0),
            score = score,
            pacmanLives = this.pacmanLives,
            currentLevel = this.currentLevel,
            gameStatus = this.gameStatus,
            remainFood = this.currentDots
        )
    }

    private fun checkGameWin(): Boolean {
        return (this.currentDots == 0 && currentLevel == maps.size - 1)
    }

    fun entityGetsEat(position: Position, scoreAddition: Int) {
        val element = currentMap?.getElementByPosition(position.positionX,position.positionY)
        currentMap?.insertElement(EMPTY_SPACE, position.positionX, position.positionY)
        if((element == PELLET_CHAR || element == ENERGIZER_CHAR) && this.currentDots>0) this.currentDots -= 1
        this.score += scoreAddition
        _boardState.value = _boardState.value.copy(
            gameBoardData = currentMap?.copy()?: Matrix(0, 0),
            score = this.score,
            remainFood = this.currentDots
        )
        if (checkGameWin()) {
            _boardState.value = _boardState.value.copy(
                gameStatus = GameStatus.WON
            )
        }
    }

    fun updateScorer(scoreAddition: Int){
        this.score += scoreAddition
        _boardState.value = _boardState.value.copy(
            score = this.score
        )
    }

    fun updateCurrentMap(position: Position,value:Char){
        currentMap?.insertElement(value, position.positionX, position.positionY)
        _boardState.value = _boardState.value.copy(
            gameBoardData = currentMap?.copy()?: Matrix(0,0)
        )
    }

    // Tambahkan method untuk update game status
    fun updateGameStatus(status: GameStatus) {
        this.gameStatus = status
        _boardState.value = _boardState.value.copy(
            gameStatus = this.gameStatus
        )
    }
}