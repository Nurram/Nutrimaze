package com.myapps.pacman.game

import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Position
import kotlinx.coroutines.flow.StateFlow

interface ICollisionHandler {

    // Basic collision handlers
    var handlePelletCollision: (Position) -> Unit
    var handleEnergizerCollision: (Position) -> Unit
    var handleBellCollision: (Position) -> Unit
    var handleGhostEaten: (GhostData) -> Unit
    var handlePacmanDeath: () -> Unit

    // Legacy healthy food handlers (backwards compatibility)
    var handleRiceCollision: (Position) -> Unit
    var handleFishCollision: (Position) -> Unit
    var handleVegetableCollision: (Position) -> Unit
    var handleFruitCollision: (Position) -> Unit

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - KARBOHIDRAT
    // ===========================================
    var handleNasiCollision: (Position) -> Unit
    var handleUbiCollision: (Position) -> Unit
    var handleKentangCollision: (Position) -> Unit
    var handleSingkongCollision: (Position) -> Unit
    var handleJagungCollision: (Position) -> Unit

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - PROTEIN
    // ===========================================
    var handleIkanCollision: (Position) -> Unit
    var handleAyamCollision: (Position) -> Unit
    var handleTempeCollision: (Position) -> Unit
    var handleTahuCollision: (Position) -> Unit
    var handleKacangCollision: (Position) -> Unit

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - SAYURAN
    // ===========================================
    var handleBayamCollision: (Position) -> Unit
    var handleBrokoliCollision: (Position) -> Unit
    var handleWortelCollision: (Position) -> Unit
    var handleKangkungCollision: (Position) -> Unit
    var handleSawiCollision: (Position) -> Unit

    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLERS - BUAH
    // ===========================================
    var handleApelCollision: (Position) -> Unit
    var handlePisangCollision: (Position) -> Unit
    var handleJerukCollision: (Position) -> Unit
    var handlePepayaCollision: (Position) -> Unit
    var handleManggaCollision: (Position) -> Unit

    // Core collision management methods
    fun startObservingCollisions(
        pacmanState: StateFlow<PacmanData>,
        ghostStates: List<StateFlow<GhostData>>,
        mapState: StateFlow<BoardData>
    )

    fun cancelCollisionObservation()
    fun pauseCollisionObservation()
    fun resumeCollisionObservation()
}