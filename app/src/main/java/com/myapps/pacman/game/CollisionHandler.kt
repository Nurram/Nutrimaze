package com.myapps.pacman.game

import android.util.Log
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.GhostsIdentifiers
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.Quadruple
import com.myapps.pacman.utils.TypeOfCollision
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CollisionHandler(
    private val coroutineDispatcher: CoroutineDispatcher
): ICollisionHandler {
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> get() = _isPaused

    // Basic collision handlers
    override var handlePelletCollision: (Position) -> Unit = {}
    override var handleEnergizerCollision: (Position) -> Unit = {}
    override var handleBellCollision: (Position) -> Unit = {}
    override var handleGhostEaten: (GhostData) -> Unit = {}
    override var handlePacmanDeath: () -> Unit = {}

    // Legacy healthy food handlers (backwards compatibility)
    override var handleRiceCollision: (Position) -> Unit = {}
    override var handleFishCollision: (Position) -> Unit = {}
    override var handleVegetableCollision: (Position) -> Unit = {}
    override var handleFruitCollision: (Position) -> Unit = {}

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - KARBOHIDRAT
    // ===========================================
    override var handleNasiCollision: (Position) -> Unit = {}
    override var handleUbiCollision: (Position) -> Unit = {}
    override var handleKentangCollision: (Position) -> Unit = {}
    override var handleSingkongCollision: (Position) -> Unit = {}
    override var handleJagungCollision: (Position) -> Unit = {}

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - PROTEIN
    // ===========================================
    override var handleIkanCollision: (Position) -> Unit = {}
    override var handleAyamCollision: (Position) -> Unit = {}
    override var handleTempeCollision: (Position) -> Unit = {}
    override var handleTahuCollision: (Position) -> Unit = {}
    override var handleKacangCollision: (Position) -> Unit = {}

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - SAYURAN
    // ===========================================
    override var handleBayamCollision: (Position) -> Unit = {}
    override var handleBrokoliCollision: (Position) -> Unit = {}
    override var handleWortelCollision: (Position) -> Unit = {}
    override var handleKangkungCollision: (Position) -> Unit = {}
    override var handleSawiCollision: (Position) -> Unit = {}

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - BUAH
    // ===========================================
    override var handleApelCollision: (Position) -> Unit = {}
    override var handlePisangCollision: (Position) -> Unit = {}
    override var handleJerukCollision: (Position) -> Unit = {}
    override var handlePepayaCollision: (Position) -> Unit = {}
    override var handleManggaCollision: (Position) -> Unit = {}

    private var job: Job? = null

    override fun startObservingCollisions(
        pacmanState: StateFlow<PacmanData>,
        ghostStates: List<StateFlow<GhostData>>,
        mapState: StateFlow<BoardData>
    ) {
        _isPaused.value = false
        job?.cancel()
        job = CoroutineScope(coroutineDispatcher).launch {
            combine(
                pacmanState,
                combineGhostsStateFlows(ghostStates),
                mapState,
                isPaused
            ) { pacman, ghostsList, map, paused ->
                Quadruple(pacman, ghostsList, map, paused)
            }.distinctUntilChanged()
                .collect { (pacman, ghostsList, map, paused) ->
                    if (!paused) {
                        handlePacmanCollisions(pacman, ghostsList, map.currentLevel)
                        handlePacmanFoodCollisions(pacman, map)
                    }
                }
        }
    }

    private fun handlePacmanFoodCollisions(pacman: PacmanData, boardData: BoardData) {
        when (checkCollisionWithFood(boardData.gameBoardData, pacman.pacmanPosition)) {
            TypeOfCollision.PELLET -> handlePelletCollision(pacman.pacmanPosition)
            TypeOfCollision.ENERGIZER -> handleEnergizerCollision(pacman.pacmanPosition)
            TypeOfCollision.BELL -> handleBellCollision(pacman.pacmanPosition)

            // Legacy foods
            TypeOfCollision.RICE -> handleRiceCollision(pacman.pacmanPosition)
            TypeOfCollision.FISH -> handleFishCollision(pacman.pacmanPosition)
            TypeOfCollision.VEGETABLE -> handleVegetableCollision(pacman.pacmanPosition)
            TypeOfCollision.FRUIT -> handleFruitCollision(pacman.pacmanPosition)

            // Dynamic foods - Karbohidrat
            TypeOfCollision.NASI -> handleNasiCollision(pacman.pacmanPosition)
            TypeOfCollision.UBI -> handleUbiCollision(pacman.pacmanPosition)
            TypeOfCollision.KENTANG -> handleKentangCollision(pacman.pacmanPosition)
            TypeOfCollision.SINGKONG -> handleSingkongCollision(pacman.pacmanPosition)
            TypeOfCollision.JAGUNG -> handleJagungCollision(pacman.pacmanPosition)

            // Dynamic foods - Protein
            TypeOfCollision.IKAN -> handleIkanCollision(pacman.pacmanPosition)
            TypeOfCollision.AYAM -> handleAyamCollision(pacman.pacmanPosition)
            TypeOfCollision.TEMPE -> handleTempeCollision(pacman.pacmanPosition)
            TypeOfCollision.TAHU -> handleTahuCollision(pacman.pacmanPosition)
            TypeOfCollision.KACANG -> handleKacangCollision(pacman.pacmanPosition)

            // Dynamic foods - Sayuran
            TypeOfCollision.BAYAM -> handleBayamCollision(pacman.pacmanPosition)
            TypeOfCollision.BROKOLI -> handleBrokoliCollision(pacman.pacmanPosition)
            TypeOfCollision.WORTEL -> handleWortelCollision(pacman.pacmanPosition)
            TypeOfCollision.KANGKUNG -> handleKangkungCollision(pacman.pacmanPosition)
            TypeOfCollision.SAWI -> handleSawiCollision(pacman.pacmanPosition)

            // Dynamic foods - Buah
            TypeOfCollision.APEL -> handleApelCollision(pacman.pacmanPosition)
            TypeOfCollision.PISANG -> handlePisangCollision(pacman.pacmanPosition)
            TypeOfCollision.JERUK -> handleJerukCollision(pacman.pacmanPosition)
            TypeOfCollision.PEPAYA -> handlePepayaCollision(pacman.pacmanPosition)
            TypeOfCollision.MANGGA -> handleManggaCollision(pacman.pacmanPosition)

            else -> {}
        }
    }

    private fun handlePacmanCollisions(pacman: PacmanData, ghosts: List<GhostData>, currentLevel: Int) {
        ghosts.forEach { ghost ->
            if (checkCollision(ghost, pacman.pacmanPosition)) {
                if (pacman.energizerStatus) {
                    handleGhostEaten(ghost)
                } else {
                    if(currentLevel < 2 && ghost.identifier == GhostsIdentifiers.PINKY) {
                        return
                    } else if(currentLevel < 3 && ghost.identifier == GhostsIdentifiers.CLYDE) {
                        return
                    } else {
                        handlePacmanDeath()
                    }
                }
            }
        }
    }

    private fun checkCollision(ghost: GhostData, position: Position): Boolean {
        return ghost.ghostLifeStatement && ghost.ghostPosition == position
    }

    private fun checkCollisionWithFood(map: Matrix<Char>, position: Position): TypeOfCollision {
        return when (map.getElementByPosition(position.positionX, position.positionY)) {
            BoardController.PELLET_CHAR -> TypeOfCollision.PELLET
            BoardController.ENERGIZER_CHAR -> TypeOfCollision.ENERGIZER
            BoardController.BELL_CHAR -> TypeOfCollision.BELL

            // Legacy foods
            GameConstants.RICE_CHAR -> TypeOfCollision.RICE
            GameConstants.FISH_CHAR -> TypeOfCollision.FISH
            GameConstants.VEGETABLE_CHAR -> TypeOfCollision.VEGETABLE
            GameConstants.FRUIT_CHAR -> TypeOfCollision.FRUIT

            // Dynamic foods - Karbohidrat
            GameConstants.NASI_CHAR -> TypeOfCollision.NASI
            GameConstants.UBI_CHAR -> TypeOfCollision.UBI
            GameConstants.KENTANG_CHAR -> TypeOfCollision.KENTANG
            GameConstants.SINGKONG_CHAR -> TypeOfCollision.SINGKONG
            GameConstants.JAGUNG_CHAR -> TypeOfCollision.JAGUNG

            // Dynamic foods - Protein
            GameConstants.IKAN_CHAR -> TypeOfCollision.IKAN
            GameConstants.AYAM_CHAR -> TypeOfCollision.AYAM
            GameConstants.TEMPE_CHAR -> TypeOfCollision.TEMPE
            GameConstants.TAHU_CHAR -> TypeOfCollision.TAHU
            GameConstants.KACANG_CHAR -> TypeOfCollision.KACANG

            // Dynamic foods - Sayuran
            GameConstants.BAYAM_CHAR -> TypeOfCollision.BAYAM
            GameConstants.BROKOLI_CHAR -> TypeOfCollision.BROKOLI
            GameConstants.WORTEL_CHAR -> TypeOfCollision.WORTEL
            GameConstants.KANGKUNG_CHAR -> TypeOfCollision.KANGKUNG
            GameConstants.SAWI_CHAR -> TypeOfCollision.SAWI

            // Dynamic foods - Buah
            GameConstants.APEL_CHAR -> TypeOfCollision.APEL
            GameConstants.PISANG_CHAR -> TypeOfCollision.PISANG
            GameConstants.JERUK_CHAR -> TypeOfCollision.JERUK
            GameConstants.PEPAYA_CHAR -> TypeOfCollision.PEPAYA
            GameConstants.MANGGA_CHAR -> TypeOfCollision.MANGGA

            else -> TypeOfCollision.NONE
        }
    }

    override fun cancelCollisionObservation(){
        job?.cancel()
        job = null
    }

    private fun combineGhostsStateFlows(ghostsStateFlows: List<StateFlow<GhostData>>): StateFlow<List<GhostData>> {
        return combine(ghostsStateFlows) { ghostArray ->
            ghostArray.toList()
        }.stateIn(CoroutineScope(coroutineDispatcher), SharingStarted.Lazily, emptyList())
    }

    override fun pauseCollisionObservation(){
        _isPaused.value = true
    }

    override fun resumeCollisionObservation(){
        _isPaused.value = false
    }
}