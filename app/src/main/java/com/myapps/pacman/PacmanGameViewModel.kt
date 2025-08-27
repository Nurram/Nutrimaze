package com.myapps.pacman

import android.util.Log
import androidx.lifecycle.ViewModel
import com.myapps.pacman.game.PacmanGame
import com.myapps.pacman.utils.Direction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PacmanGameViewModel @Inject constructor(
    private val pacmanGame: PacmanGame
) : ViewModel() {

    private val _gameIsStarted = MutableStateFlow(false)
    val gameIsStarted: StateFlow<Boolean> = _gameIsStarted.asStateFlow()

    private val _gameIsPaused = MutableStateFlow(false)
    val gameIsPaused: StateFlow<Boolean> = _gameIsPaused.asStateFlow()

    private val _gameIsMute = MutableStateFlow(false)
    val gameIsMute: StateFlow<Boolean> = _gameIsMute.asStateFlow()

    // StateFlow untuk quiz
    private val _showQuiz = MutableStateFlow(0)
    val showQuiz: StateFlow<Int> = _showQuiz.asStateFlow()

    // StateFlow untuk tracking level completion
    private val _levelCompleted = MutableStateFlow(false)
    val levelCompleted: StateFlow<Boolean> = _levelCompleted.asStateFlow()

    val mapBoardData = pacmanGame.boardState
    val pacmanData = pacmanGame.pacmanState
    val blinkyData = pacmanGame.blinkyState
    val inkyData = pacmanGame.inkyState
    val pinkyData = pacmanGame.pinkyState
    val clydeData = pacmanGame.clydeState

    private val movements = mutableListOf<Direction>()
    private var pendingLevelUp = false
    private var currentLevel = 0

    init {
        // Observer untuk level completion
        observeLevelCompletion()
    }

    private fun observeLevelCompletion() {
        // Monitor board state untuk level completion
        // Ini akan dipanggil dari PacmanGame ketika level selesai
    }

    // Method untuk setup level completion callback
    fun setupLevelCompletionCallback() {
        pacmanGame.setLevelCompletionCallback {
            Log.d("PacmanGameViewModel", "Level completed, showing quiz")
            onEvents(PacmanEvents.ShowQuiz)
        }
    }

    fun onEvents(event: PacmanEvents) {
        Log.d("PacmanGameViewModel", "Event received: $event")
        when (event) {
            PacmanEvents.Start -> {
                if (!_gameIsStarted.value) {
                    clearMovementsAndAddRight()
                    pacmanGame.initGame(movements)
                    _gameIsStarted.value = true
                    currentLevel = 0
                    Log.d("PacmanGameViewModel", "Game started")
                }
            }
            PacmanEvents.Stop -> {
                if (_gameIsStarted.value) {
                    pacmanGame.stopGame()
                    _gameIsStarted.value = false
                    _gameIsPaused.value = false
                    currentLevel = 0
                    pendingLevelUp = false
                    Log.d("PacmanGameViewModel", "Game stopped")
                }
            }
            PacmanEvents.Pause -> {
                if (_gameIsStarted.value && !_gameIsPaused.value) {
                    pacmanGame.onPause()
                    _gameIsPaused.value = true
                    Log.d("PacmanGameViewModel", "Game paused")
                }
            }
            PacmanEvents.Resume -> {
                if (_gameIsStarted.value && _gameIsPaused.value) {
                    pacmanGame.onResume()
                    _gameIsPaused.value = false
                    Log.d("PacmanGameViewModel", "Game resumed")
                }
            }
            PacmanEvents.RightDirection -> {
                if (_gameIsStarted.value && !_gameIsPaused.value) {
                    Log.d("PacmanGameViewModel", "Adding RIGHT movement")
                    addMovement(Direction.RIGHT)
                }
            }
            PacmanEvents.LeftDirection -> {
                if (_gameIsStarted.value && !_gameIsPaused.value) {
                    Log.d("PacmanGameViewModel", "Adding LEFT movement")
                    addMovement(Direction.LEFT)
                }
            }
            PacmanEvents.UpDirection -> {
                if (_gameIsStarted.value && !_gameIsPaused.value) {
                    Log.d("PacmanGameViewModel", "Adding UP movement")
                    addMovement(Direction.UP)
                }
            }
            PacmanEvents.DownDirection -> {
                if (_gameIsStarted.value && !_gameIsPaused.value) {
                    Log.d("PacmanGameViewModel", "Adding DOWN movement")
                    addMovement(Direction.DOWN)
                }
            }
            PacmanEvents.MuteSounds -> {
                pacmanGame.muteSounds()
                _gameIsMute.value = true
                Log.d("PacmanGameViewModel", "Sounds muted")
            }
            PacmanEvents.RecoverSounds -> {
                pacmanGame.recoverSounds()
                _gameIsMute.value = false
                Log.d("PacmanGameViewModel", "Sounds recovered")
            }
            PacmanEvents.ShowQuiz -> {
                // Dipanggil ketika level selesai dan perlu quiz
                Log.d("PacmanGameViewModel", "ShowQuiz event - current level: $currentLevel")
                //HERE NO 2
                if (currentLevel < 3) { // Level 0-2 perlu quiz, level 2 adalah level terakhir
                    _showQuiz.value = currentLevel + 1
                    pendingLevelUp = true
                    Log.d("PacmanGameViewModel", "Quiz triggered for level: ${currentLevel + 1}")
                    // Pause game sementara untuk quiz
                    if (!_gameIsPaused.value) {
                        pacmanGame.onPause()
                        _gameIsPaused.value = true
                    }
                }
            }
            PacmanEvents.ContinueToNextLevel -> {
                // Quiz berhasil, lanjut ke level berikutnya
                Log.d("PacmanGameViewModel", "ContinueToNextLevel - proceeding from level $currentLevel")
                currentLevel++
                pendingLevelUp = false
                _showQuiz.value = 0

                // Resume game dan proceed ke level berikutnya
                if (_gameIsPaused.value) {
                    pacmanGame.onResume()
                    _gameIsPaused.value = false
                }

                // Allow game to proceed to next level
                pacmanGame.proceedToNextLevel()
                Log.d("PacmanGameViewModel", "Proceeded to level: $currentLevel")
            }
            PacmanEvents.FailedQuiz -> {
                // Quiz gagal, kembali ke level sebelumnya atau restart
                Log.d("PacmanGameViewModel", "FailedQuiz - resetting from level $currentLevel")
                pendingLevelUp = false
                _showQuiz.value = 0

                // Reset to previous level atau restart game
                pacmanGame.resetToPreviousLevel()

                if (_gameIsPaused.value) {
                    pacmanGame.onResume()
                    _gameIsPaused.value = false
                }
                Log.d("PacmanGameViewModel", "Quiz failed, game reset")
            }
        }
    }

    // Method untuk dipanggil dari PacmanGame ketika level selesai
    fun onLevelCompleted() {
        Log.d("PacmanGameViewModel", "onLevelCompleted called")
        if (currentLevel < 2) { // Jika bukan level terakhir
            onEvents(PacmanEvents.ShowQuiz)
        }
    }

    // FIXED: Logic movement yang diperbaiki
    private fun addMovement(direction: Direction) {
        Log.d("PacmanGameViewModel", "addMovement called with direction: $direction")
        Log.d("PacmanGameViewModel", "Current movements before: $movements")

        // Simplified logic - always allow new direction input
        when {
            movements.isEmpty() -> {
                // Jika list kosong, tambahkan direction baru
                movements.add(direction)
                Log.d("PacmanGameViewModel", "Added first movement: $direction")
            }
            movements.size == 1 -> {
                // Jika ada 1 movement, cek apakah berbeda
                if (movements[0] != direction) {
                    movements.add(direction)
                    Log.d("PacmanGameViewModel", "Added second movement: $direction")
                } else {
                    Log.d("PacmanGameViewModel", "Same direction as current, not adding")
                }
            }
            movements.size >= 2 -> {
                // Jika sudah ada 2 movements, replace yang pertama dengan yang baru
                // Tapi hanya jika berbeda dengan yang terakhir
                if (movements.last() != direction) {
                    movements[0] = direction
                    Log.d("PacmanGameViewModel", "Replaced first movement with: $direction")
                } else {
                    Log.d("PacmanGameViewModel", "Same as last direction, not replacing")
                }
            }
        }

        Log.d("PacmanGameViewModel", "Current movements after: $movements")
    }

    private fun clearMovementsAndAddRight() {
        movements.clear()
        movements.add(Direction.RIGHT)
        Log.d("PacmanGameViewModel", "Movements cleared and set to RIGHT")
    }

    override fun onCleared() {
        super.onCleared()
        if (_gameIsStarted.value) {
            pacmanGame.stopGame()
        }
        Log.d("PacmanGameViewModel", "ViewModel cleared")
    }
}