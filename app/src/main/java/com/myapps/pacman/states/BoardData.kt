    package com.myapps.pacman.states

    import com.myapps.pacman.utils.matrix.Matrix

    data class  BoardData(
        val gameBoardData: Matrix<Char> = Matrix(0,0),
        val score:Int = 0,
        val pacmanLives:Int = 0,
        val currentLevel:Int = 0,
        val gameStatus: GameStatus = GameStatus.ONGOING,
        val remainFood:Int = 0
    )
