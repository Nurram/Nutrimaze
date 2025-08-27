package com.myapps.pacman.game
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.R
import com.myapps.pacman.game.coroutines.CoroutineSupervisor
import com.myapps.pacman.ghost.Blinky
import com.myapps.pacman.ghost.Clyde
import com.myapps.pacman.ghost.GhostMode
import com.myapps.pacman.ghost.Inky
import com.myapps.pacman.ghost.Pinky
import com.myapps.pacman.levels.LevelStartData
import com.myapps.pacman.levels.MapProvider
import com.myapps.pacman.pacman.Pacman
import com.myapps.pacman.sound.GameSoundService
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.GhostsIdentifiers
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.timer.CentralTimerController
import com.myapps.pacman.timer.ICentralTimerController
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.transformIntoCharMatrix
import com.myapps.pacman.utils.transformLevelsDataIntoListsOfDots
import com.myapps.pacman.utils.transformLevelsDataIntoMaps
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.currentCoroutineContext
import android.util.Log
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random
// File ini merupakan bagian utama perbaikan PacmanGame.kt
// Tambahkan import dan modifikasi constructor:
import com.myapps.pacman.game.FoodSpawnManager
class PacmanGame @Inject constructor(
    private val centralTimerController: ICentralTimerController,
    private val gameSoundService: GameSoundService,
    mapProvider: MapProvider,
    private val collisionHandler: ICollisionHandler,
    private val coroutineSupervisor: CoroutineSupervisor
) {
    //game coroutines and main game controllers
    private var gameJob: Job? = null
    private var isGameStarted = false
    private var gameJobIsPaused = false
    private var pacmanMovementJob: Job? = null
    private var blinkyMovementJob: Job? = null
    private var inkyMovementJob: Job? = null
    private var pinkyMovementJob: Job? = null
    private var clydeMovementJob: Job? = null
    private var sirenSoundJob: Job? = null
    private var pauseController = PauseController()
    private var levelsData: Map<Int, LevelStartData> = emptyMap()
    //game variables
    private var ghostTimerTarget = GameConstants.SCATTER_TIME
    private var ghostMode = GhostMode.SCATTER
    private var bellsEaten = 0
    private var counterEatingGhost = 0
    private var pacmanSpeedDelay = 250
    private var isBellAppear = false
    private var sirenSoundPause = PauseController()
    // Quiz integration
    private var levelCompletionCallback: (() -> Unit)? = null
    private var waitingForQuiz = false
    private var canProceedToNextLevel = false
    // Maksimal 3 level (0-2)
    private val MAX_LEVELS = 3
    // Tracking food positions for respawn
    // private val ricePositions = mutableListOf<Position>() // Dihapus, diganti FoodSpawnManager
    // private val fishPositions = mutableListOf<Position>() // Dihapus, diganti FoodSpawnManager
    // private val vegetablePositions = mutableListOf<Position>() // Dihapus, diganti FoodSpawnManager
    // private val fruitPositions = mutableListOf<Position>() // Dihapus, diganti FoodSpawnManager
    // Active food positions
    // private var activeRicePosition: Position? = null // Dihapus, diganti FoodSpawnManager
    // private var activeFishPosition: Position? = null // Dihapus, diganti FoodSpawnManager
    // private var activeVegetablePosition: Position? = null // Dihapus, diganti FoodSpawnManager
    // private var activeFruitPosition: Position? = null // Dihapus, diganti FoodSpawnManager
    private var actorsMovementsTimerController = ActorsMovementsTimerController()
    // Dynamic food system
    private val foodSpawnManager = FoodSpawnManager()
    //game Actors
    private var pacman: Pacman
    private var blinky: Blinky
    private var inky: Inky
    private var pinky: Pinky
    private var clyde: Clyde
    private var boardController: BoardController
    val pacmanState: StateFlow<PacmanData> get() = pacman.pacmanState
    val blinkyState: StateFlow<GhostData> get() = blinky.blinkyState
    val inkyState: StateFlow<GhostData> get() = inky.inkyState
    val pinkyState: StateFlow<GhostData> get() = pinky.pinkyState
    val clydeState: StateFlow<GhostData> get() = clyde.clydeState
    val boardState: StateFlow<BoardData> get() = boardController.boardState
    // ===========================================
    // PERBAIKAN INIT BLOCK - DYNAMIC COLLISION HANDLERS
    // ===========================================
    // Ganti init block yang lama dengan yang baru ini:
    init {
        levelsData = mapProvider.getMaps()
        boardController = BoardController(
            maps = transformLevelsDataIntoMaps(levelsData),
            dots = transformLevelsDataIntoListsOfDots(levelsData)
        )
        // ... pacman, blinky, inky, pinky, clyde initialization tetap sama ...
        pacman = Pacman(
            initialPosition = levelsData[boardController.boardState.value.currentLevel]?.pacmanDefaultPosition
                ?: Position(
                    -1, -1
                ), actorsMovementsTimerController = actorsMovementsTimerController
        )
        blinky = Blinky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.blinkyDefaultPosition
                ?: Position(
                    -1, -1
                ),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.blinkyScatterPosition
                ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget
                ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition
                ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange
                ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange
                ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            levelsData[boardController.boardState.value.currentLevel]?.blinkySpeedDelay
                ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY,
            actorsMovementsTimerController
        )
        inky = Inky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.inkyDefaultPosition
                ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.inkyScatterPosition
                ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget
                ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition
                ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange
                ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange
                ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )
        pinky = Pinky(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.pinkyDefaultPosition
                ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.pinkyScatterPosition
                ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget
                ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition
                ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange
                ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange
                ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )
        clyde = Clyde(
            currentPosition = levelsData[boardController.boardState.value.currentLevel]?.clydeDefaultPosition
                ?: Position(-1, -1),
            target = Position(0, 0),
            scatterTarget = levelsData[boardController.boardState.value.currentLevel]?.clydeScatterPosition
                ?: Position(-1, -1),
            doorTarget = levelsData[boardController.boardState.value.currentLevel]?.doorTarget
                ?: Position(-1, -1),
            home = levelsData[boardController.boardState.value.currentLevel]?.homeTargetPosition
                ?: Position(-1, -1),
            homeXRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeXRange
                ?: IntRange(-1, 0),
            homeYRange = levelsData[boardController.boardState.value.currentLevel]?.ghostHomeYRange
                ?: IntRange(-1, 0),
            direction = Direction.NOWHERE,
            actorsMovementsTimerController
        )
        pacman.updateSpeedDelay(pacmanSpeedDelay)
        // Setup basic collision handlers
        collisionHandler.handleBellCollision = { handleBellCollision(it) }
        collisionHandler.handlePelletCollision = { handlePelletCollision(it) }
        collisionHandler.handleEnergizerCollision = { handleEnergizerCollision(it) }
        collisionHandler.handlePacmanDeath = { handlePacmanHit() }
        collisionHandler.handleGhostEaten = { handleGhostEaten(it) }
        // Setup legacy food handlers (backwards compatibility)
        collisionHandler.handleRiceCollision = { handleRiceCollision(it) }
        collisionHandler.handleFishCollision = { handleFishCollision(it) }
        collisionHandler.handleVegetableCollision = { handleVegetableCollision(it) }
        collisionHandler.handleFruitCollision = { handleFruitCollision(it) }
        // ===========================================
        // SETUP DYNAMIC FOOD COLLISION HANDLERS - KARBOHIDRAT
        // ===========================================
        collisionHandler.handleNasiCollision = { handleDynamicFoodCollision(it, GameConstants.NASI_CHAR) }
        collisionHandler.handleUbiCollision = { handleDynamicFoodCollision(it, GameConstants.UBI_CHAR) }
        collisionHandler.handleKentangCollision = { handleDynamicFoodCollision(it, GameConstants.KENTANG_CHAR) }
        collisionHandler.handleSingkongCollision = { handleDynamicFoodCollision(it, GameConstants.SINGKONG_CHAR) }
        collisionHandler.handleJagungCollision = { handleDynamicFoodCollision(it, GameConstants.JAGUNG_CHAR) }
        // ===========================================
        // SETUP DYNAMIC FOOD COLLISION HANDLERS - PROTEIN
        // ===========================================
        collisionHandler.handleIkanCollision = { handleDynamicFoodCollision(it, GameConstants.IKAN_CHAR) }
        collisionHandler.handleAyamCollision = { handleDynamicFoodCollision(it, GameConstants.AYAM_CHAR) }
        collisionHandler.handleTempeCollision = { handleDynamicFoodCollision(it, GameConstants.TEMPE_CHAR) }
        collisionHandler.handleTahuCollision = { handleDynamicFoodCollision(it, GameConstants.TAHU_CHAR) }
        collisionHandler.handleKacangCollision = { handleDynamicFoodCollision(it, GameConstants.KACANG_CHAR) }
        // ===========================================
        // SETUP DYNAMIC FOOD COLLISION HANDLERS - SAYURAN
        // ===========================================
        collisionHandler.handleBayamCollision = { handleDynamicFoodCollision(it, GameConstants.BAYAM_CHAR) }
        collisionHandler.handleBrokoliCollision = { handleDynamicFoodCollision(it, GameConstants.BROKOLI_CHAR) }
        collisionHandler.handleWortelCollision = { handleDynamicFoodCollision(it, GameConstants.WORTEL_CHAR) }
        collisionHandler.handleKangkungCollision = { handleDynamicFoodCollision(it, GameConstants.KANGKUNG_CHAR) }
        collisionHandler.handleSawiCollision = { handleDynamicFoodCollision(it, GameConstants.SAWI_CHAR) }
        // ===========================================
        // SETUP DYNAMIC FOOD COLLISION HANDLERS - BUAH
        // ===========================================
        collisionHandler.handleApelCollision = { handleDynamicFoodCollision(it, GameConstants.APEL_CHAR) }
        collisionHandler.handlePisangCollision = { handleDynamicFoodCollision(it, GameConstants.PISANG_CHAR) }
        collisionHandler.handleJerukCollision = { handleDynamicFoodCollision(it, GameConstants.JERUK_CHAR) }
        collisionHandler.handlePepayaCollision = { handleDynamicFoodCollision(it, GameConstants.PEPAYA_CHAR) }
        collisionHandler.handleManggaCollision = { handleDynamicFoodCollision(it, GameConstants.MANGGA_CHAR) }
        centralTimerController.addNewTimerController(CentralTimerController.GHOST_TIMER)
        centralTimerController.addNewTimerController(CentralTimerController.ENERGIZER_TIMER)
        centralTimerController.addNewTimerController(CentralTimerController.BELL_TIMER)
    }
    // Method untuk set callback level completion
    fun setLevelCompletionCallback(callback: () -> Unit) {
        levelCompletionCallback = callback
    }
    // Method untuk handle quiz result
    fun proceedToNextLevel() {
        canProceedToNextLevel = true
        waitingForQuiz = false
    }
    fun resetToPreviousLevel() {
        canProceedToNextLevel = false
        waitingForQuiz = false
        // Reset ke level sebelumnya atau restart
        if (boardController.boardState.value.currentLevel > 0) {
            // Bisa implement logic untuk kembali ke level sebelumnya
            // Untuk sekarang, kita restart game
            resetGame()
        }
    }
    // Inisialisasi posisi makanan dari map
    // Private fun initFoodPositions() { ... } // Dihapus, diganti initializeDynamicFoodSystem()
    // Private fun cleanHealthyFoodsFromMap() { ... } // Dihapus, diganti cleanExistingFoodsFromMap()
    // Private fun spawnHealthyFoods() { ... } // Dihapus, diganti initializeDynamicFoodSystem()
    // ===========================================
    // DYNAMIC FOOD COLLISION HANDLER - UNIVERSAL
    // ===========================================
    private fun handleDynamicFoodCollision(position: Position, foodChar: Char) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        updateIndividualFoodCounter(foodChar)
        checkPortionLimitsAndHealth(foodChar)
        foodSpawnManager.respawnSameTypeFood(position, foodChar, boardController)

        if (isHealthyFoodTargetMet()) {
            gameSoundService.playSound(R.raw.pacman_energizer_mode)
        }
        Log.d("PacmanGame", "Consumed ${GameConstants.getFoodName(foodChar)} - Same-type respawn triggered")
    }

    // ===========================================
    // INDIVIDUAL FOOD COUNTER UPDATES
    // ===========================================
    private fun updateIndividualFoodCounter(foodChar: Char) {
        when (foodChar) {
            GameConstants.NASI_CHAR -> pacman.updateNasiEaten(pacman.pacmanState.value.nasiEaten + 1)
            GameConstants.UBI_CHAR -> pacman.updateUbiEaten(pacman.pacmanState.value.ubiEaten + 1)
            GameConstants.KENTANG_CHAR -> pacman.updateKentangEaten(pacman.pacmanState.value.kentangEaten + 1)
            GameConstants.SINGKONG_CHAR -> pacman.updateSingkongEaten(pacman.pacmanState.value.singkongEaten + 1)
            GameConstants.JAGUNG_CHAR -> pacman.updateJagungEaten(pacman.pacmanState.value.jagungEaten + 1)

            GameConstants.IKAN_CHAR -> pacman.updateIkanEaten(pacman.pacmanState.value.ikanEaten + 1)
            GameConstants.AYAM_CHAR -> pacman.updateAyamEaten(pacman.pacmanState.value.ayamEaten + 1)
            GameConstants.TEMPE_CHAR -> pacman.updateTempeEaten(pacman.pacmanState.value.tempeEaten + 1)
            GameConstants.TAHU_CHAR -> pacman.updateTahuEaten(pacman.pacmanState.value.tahuEaten + 1)
            GameConstants.KACANG_CHAR -> pacman.updateKacangEaten(pacman.pacmanState.value.kacangEaten + 1)

            GameConstants.BAYAM_CHAR -> pacman.updateBayamEaten(pacman.pacmanState.value.bayamEaten + 1)
            GameConstants.BROKOLI_CHAR -> pacman.updateBrokoliEaten(pacman.pacmanState.value.brokoliEaten + 1)
            GameConstants.WORTEL_CHAR -> pacman.updateWortelEaten(pacman.pacmanState.value.wortelEaten + 1)
            GameConstants.KANGKUNG_CHAR -> pacman.updateKangkungEaten(pacman.pacmanState.value.kangkungEaten + 1)
            GameConstants.SAWI_CHAR -> pacman.updateSawiEaten(pacman.pacmanState.value.sawiEaten + 1)

            GameConstants.APEL_CHAR -> pacman.updateApelEaten(pacman.pacmanState.value.apelEaten + 1)
            GameConstants.PISANG_CHAR -> pacman.updatePisangEaten(pacman.pacmanState.value.pisangEaten + 1)
            GameConstants.JERUK_CHAR -> pacman.updateJerukEaten(pacman.pacmanState.value.jerukEaten + 1)
            GameConstants.PEPAYA_CHAR -> pacman.updatePepayaEaten(pacman.pacmanState.value.pepayaEaten + 1)
            GameConstants.MANGGA_CHAR -> pacman.updateManggaEaten(pacman.pacmanState.value.manggaEaten + 1)
        }
        updateLegacyTotals(foodChar)
    }

    private fun updateLegacyTotals(foodChar: Char) {
        val pacmanData = pacman.pacmanState.value
        when {
            GameConstants.isCarbFood(foodChar) -> pacman.updateRiceEaten(pacmanData.totalCarbsEaten)
            GameConstants.isProteinFood(foodChar) -> pacman.updateFishEaten(pacmanData.totalProteinEaten)
            GameConstants.isVegetableFood(foodChar) -> pacman.updateVegetableEaten(pacmanData.totalVegetablesEaten)
            GameConstants.isFruitFood(foodChar) -> pacman.updateFruitEaten(pacmanData.totalFruitsEaten)
        }
    }

    // ===========================================
    // PORTION LIMITS AND HEALTH CHECKING
    // ===========================================
    private fun checkPortionLimitsAndHealth(foodChar: Char) {
        val pacmanData = pacman.pacmanState.value
        val categoryTotal = when {
            GameConstants.isCarbFood(foodChar) -> pacmanData.totalCarbsEaten
            GameConstants.isProteinFood(foodChar) -> pacmanData.totalProteinEaten
            GameConstants.isVegetableFood(foodChar) -> pacmanData.totalVegetablesEaten
            GameConstants.isFruitFood(foodChar) -> pacmanData.totalFruitsEaten
            else -> 0
        }

        val maxLimit = when {
            GameConstants.isCarbFood(foodChar) -> GameConstants.MAX_RICE_PORTION
            GameConstants.isProteinFood(foodChar) -> GameConstants.MAX_FISH_PORTION
            GameConstants.isVegetableFood(foodChar) -> GameConstants.MAX_VEGETABLE_PORTION
            GameConstants.isFruitFood(foodChar) -> GameConstants.MAX_FRUIT_PORTION
            else -> Int.MAX_VALUE
        }

        if (categoryTotal > maxLimit) {
            pacman.updateHealth(pacman.pacmanState.value.health - 0.5f)
            gameSoundService.playSound(R.raw.pacman_death)
            Log.w("PacmanGame", "Exceeded limit for ${GameConstants.getFoodCategory(foodChar)}: $categoryTotal > $maxLimit")
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }
    }

    // ===========================================
    // LEVEL TARGET CHECKING - UPDATED
    // ===========================================
    private fun isHealthyFoodTargetMet(): Boolean {
        val pacmanData = pacman.pacmanState.value
        val targetRice = 3
        val targetFish = 2
        val targetVegetable = 3
        val targetFruit = 2

        val totalCarbs = pacmanData.totalCarbsEaten
        val totalProtein = pacmanData.totalProteinEaten
        val totalVegetables = pacmanData.totalVegetablesEaten
        val totalFruits = pacmanData.totalFruitsEaten

        val riceOk = totalCarbs >= targetRice && totalCarbs <= GameConstants.MAX_RICE_PORTION
        val fishOk = totalProtein >= targetFish && totalProtein <= GameConstants.MAX_FISH_PORTION
        val vegetableOk = totalVegetables >= targetVegetable && totalVegetables <= GameConstants.MAX_VEGETABLE_PORTION
        val fruitOk = totalFruits >= targetFruit && totalFruits <= GameConstants.MAX_FRUIT_PORTION

        Log.d("PacmanGame", "Level completion check:")
        Log.d("PacmanGame", "Carbs: $totalCarbs/$targetRice (max ${GameConstants.MAX_RICE_PORTION}) - OK: $riceOk")
        Log.d("PacmanGame", "Protein: $totalProtein/$targetFish (max ${GameConstants.MAX_FISH_PORTION}) - OK: $fishOk")
        Log.d("PacmanGame", "Vegetables: $totalVegetables/$targetVegetable (max ${GameConstants.MAX_VEGETABLE_PORTION}) - OK: $vegetableOk")
        Log.d("PacmanGame", "Fruits: $totalFruits/$targetFruit (max ${GameConstants.MAX_FRUIT_PORTION}) - OK: $fruitOk")

        return riceOk && fishOk && vegetableOk && fruitOk
    }

    private suspend fun awaitSirenSoundResume() =
        suspendCancellableCoroutine<Unit> { continuation ->
            val listener = object : PauseListener {
                override fun onPause() {}
                override fun onResume() {
                    if (continuation.isActive) {
                        continuation.resume(Unit)
                    }
                }
            }
            sirenSoundPause.addListener(listener)
            continuation.invokeOnCancellation {
                sirenSoundPause.removeListener(listener)
            }
        }
    private suspend fun awaitGamePauseResume() = suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : PauseListener {
            override fun onPause() {}
            override fun onResume() {
                if (continuation.isActive) {
                    continuation.resume(Unit)
                }
            }
        }
        pauseController.addListener(listener)
        continuation.invokeOnCancellation {
            pauseController.removeListener(listener)
        }
    }
    // call this method to start the game
    fun initGame(movements: MutableList<Direction>) {
        coroutineSupervisor.restartJob()
        gameJob = coroutineSupervisor.launch {
            try {
                centralTimerController.initTimerFunction()
                startGame(movements)
                while (currentCoroutineContext().isActive) {
                    if (boardController.boardState.value.gameStatus != GameStatus.ONGOING) {
                        break
                    }
                    val startMillis = System.currentTimeMillis()
                    if (!gameJobIsPaused) {
                        checkPacmanDeath(movements)
                        clockManagement()
                        checkBellAppear()
                        loadNextLevel(movements)
                    }
                    val frameTime = System.currentTimeMillis() - startMillis
                    val delayTime = 16 - frameTime
                    if (delayTime > 0) {
                        delay(delayTime)
                    }
                    if (gameJobIsPaused) {
                        awaitGamePauseResume()
                    }
                }
            }
            finally {
                centralTimerController.stopAllTimersController()
                centralTimerController.stopTimerFunction()
                collisionHandler.cancelCollisionObservation()
                coroutineSupervisor.cancelAll()
                coroutineSupervisor.onDestroy()
            }
        }
    }
    // ===========================================
    // PERBAIKAN STARTGAME METHOD - DENGAN DYNAMIC FOOD SYSTEM
    // ===========================================
    private suspend fun startGame(movements: MutableList<Direction>) {
        if (!isGameStarted) {
            delay(2000)
            gameSoundService.playSound(R.raw.pacman_intro)
            delay(4000)
            centralTimerController.startTimerController(CentralTimerController.GHOST_TIMER)
            isGameStarted = true
            // Inisialisasi sistem makanan dinamis
            initializeDynamicFoodSystem()
            startActorsMovements(movements)
            collisionHandler.startObservingCollisions(
                pacman.pacmanState, listOf(
                    blinky.blinkyState, pinky.pinkyState, inky.inkyState, clyde.clydeState
                ), boardController.boardState
            )
            sirenSoundStart()
        }
    }
    fun stopGame() {
        isGameStarted = false
        gameJobIsPaused = false
        actorsMovementsTimerController.resume()
        if (boardState.value.gameStatus == GameStatus.ONGOING) {
            collisionHandler.cancelCollisionObservation()
            centralTimerController.stopAllTimersController()
            centralTimerController.stopTimerFunction()
            coroutineSupervisor.cancelAll()
            coroutineSupervisor.onDestroy()
        }
        gameJob = null
        pacmanMovementJob = null
        resetGame()
    }
    // ===========================================
    // RESET GAME WITH DYNAMIC FOOD SYSTEM
    // ===========================================
    private fun resetGame() {
        ghostTimerTarget = GameConstants.SCATTER_TIME
        pacman.updateLifeStatement(true)
        pacman.updateDirection(Direction.RIGHT)
        pacman.updateHealth(GameConstants.PACMAN_LIVES.toFloat())
        // Reset all food counters (legacy + dynamic)
        pacman.resetAllFoodCounters()
        isBellAppear = false
        boardController.resetBoardData()
        bellsEaten = 0
        configureGhostAndPacmanLevelDefaults(boardController.boardState.value.currentLevel)
        resetPositions(boardController.boardState.value.currentLevel)
        // Reinitialize dynamic food system
        initializeDynamicFoodSystem()
        Log.d("PacmanGame", "Game reset with dynamic food system")
    }
    private fun cancelActorMovements() {
        blinkyMovementJob?.cancel()
        inkyMovementJob?.cancel()
        pinkyMovementJob?.cancel()
        clydeMovementJob?.cancel()
        pacmanMovementJob?.cancel()
    }
    private fun clearMovements(movements: MutableList<Direction>) {
        movements.clear()
        movements.add(Direction.RIGHT)
    }
    private suspend fun checkPacmanDeath(movements: MutableList<Direction>) {
        if (boardController.boardState.value.pacmanLives > 0 && !pacman.pacmanState.value.lifeStatement) {
            sirenSoundPause.pause()
            delay(2000)
            gameSoundService.playSound(R.raw.pacman_death)
            clearMovements(movements)
            pacman.updateDirection(Direction.RIGHT)
            pacman.updateLifeStatement(true)
            resetGhostStatements()
            delay(3000)
            resetPositions(boardController.boardState.value.currentLevel)
            actorsMovementsTimerController.resume()
            collisionHandler.resumeCollisionObservation()
            sirenSoundPause.resume()
        }
    }
    // ===========================================
    // LEVEL PROGRESSION WITH DYNAMIC FOODS
    // ===========================================
    private suspend fun loadNextLevel(movements: MutableList<Direction>) {
        // Cek apakah makanan sehat sudah dikonsumsi sesuai target porsi untuk level ini
        if (isHealthyFoodTargetMet() && boardController.boardState.value.gameStatus == GameStatus.ONGOING) {
            val currentLevel = boardController.boardState.value.currentLevel
            // Cek apakah sudah level terakhir (level 4, karena index 0-4)
            if (currentLevel >= MAX_LEVELS - 1) {
                // Game selesai, menang
                boardController.updateGameStatus(GameStatus.WON)
                return
            }
            // Pause game untuk quiz (kecuali level terakhir)
            actorsMovementsTimerController.pause()
            centralTimerController.stopAllTimersController()
            sirenSoundPause.pause()
            // Trigger quiz callback
            levelCompletionCallback?.invoke()
            waitingForQuiz = true
            // Wait until quiz is completed
            while (waitingForQuiz && currentCoroutineContext().isActive) {
                delay(100)
            }
            if (canProceedToNextLevel) {
                delay(2000)
                ghostTimerTarget = GameConstants.SCATTER_TIME
                boardController.updateCurrentLevel()
                isBellAppear = false
                configureGhostAndPacmanLevelDefaults(boardController.boardState.value.currentLevel)
                resetPositions(boardController.boardState.value.currentLevel)
                resetGhostStatements()
                clearMovements(movements)
                pacman.updateDirection(Direction.RIGHT)
                // Reset all food tracking untuk level baru
                pacman.resetAllFoodCounters()
                // Reinitialize dynamic food system untuk level baru
                initializeDynamicFoodSystem()
                gameSoundService.playSound(R.raw.pacman_intro)
                delay(4000)
                centralTimerController.startTimerController(CentralTimerController.GHOST_TIMER)
                actorsMovementsTimerController.resume()
                sirenSoundPause.resume()
                canProceedToNextLevel = false
                Log.d("PacmanGame", "Advanced to level ${boardController.boardState.value.currentLevel + 1}")
            }
        }
    }
    private fun startActorsMovements(movements: MutableList<Direction>) {
        pacmanMovementJob = coroutineSupervisor.launch {
            pacman.startMoving(movements) { boardController.boardState.value.gameBoardData }
        }
        blinkyMovementJob = coroutineSupervisor.launch {
            blinky.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
            )
        }
        inkyMovementJob = coroutineSupervisor.launch {
            inky.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
                blinkyPosition = { blinky.currentPosition },
            )
        }
        pinkyMovementJob = coroutineSupervisor.launch {
            pinky.startMoving(currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode })
        }
        clydeMovementJob = coroutineSupervisor.launch {
            clyde.startMoving(
                currentMap = { boardController.boardState.value.gameBoardData },
                { pacman },
                ghostMode = { ghostMode },
            )
        }
    }
    private fun sirenSoundStart() {
        sirenSoundJob = coroutineSupervisor.launch {
            while (currentCoroutineContext().isActive) {
                if (!sirenSoundPause.isPaused) {
                    gameSoundService.playSound(R.raw.ghost_siren)
                }
                if (sirenSoundPause.isPaused) {
                    awaitSirenSoundResume()
                } else {
                    delay(GameConstants.SIREN_DELAY)
                }
            }
        }
    }
    private fun handlePelletCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        gameSoundService.playSound(R.raw.pacman_eating_pellet)
    }
    private fun handleEnergizerCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.ENERGIZER_POINTS)
        pacman.updateEnergizerStatus(true)
        centralTimerController.startTimerController(CentralTimerController.ENERGIZER_TIMER)
        centralTimerController.pauseTimerController(CentralTimerController.GHOST_TIMER)
        gameSoundService.playSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.pause { }
    }
    private fun handleBellCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.BELL_POINTS)
        pacmanSpeedDelay -= GameConstants.BELL_REDUCTION_TIME
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PACMAN_ENTITY_TYPE, pacmanSpeedDelay
        )
        pacman.updateSpeedDelay(actorsMovementsTimerController.getPacmanSpeedDelay())
        gameSoundService.playSound(R.raw.pacman_eating_fruit)
    }
    private fun handleGhostEaten(ghost: GhostData) {
        if (!pacman.pacmanState.value.energizerStatus) {
            // Ghost is junk food - reduce health
            pacman.updateHealth(pacman.pacmanState.value.health - 1.0f)
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }
        when (ghost.identifier) {
            GhostsIdentifiers.BLINKY -> blinky.updateLifeStatement(false)
            GhostsIdentifiers.INKY -> inky.updateLifeStatement(false)
            GhostsIdentifiers.BLINKY -> pinky.updateLifeStatement(false)
            GhostsIdentifiers.CLYDE -> clyde.updateLifeStatement(false)
        }
        gameSoundService.playSound(R.raw.pacman_eatghost)
        counterEatingGhost++
        boardController.updateScorer(calculateEatGhostScorer())
    }
    private fun handlePacmanHit() {
        actorsMovementsTimerController.pause()
        collisionHandler.pauseCollisionObservation()
        pacman.updateLifeStatement(false)
        boardController.decreasePacmanLives()
    }
    private fun resetGhostStatements() {
        blinky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        inky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        pinky.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
        clyde.apply {
            updateDirection(Direction.NOWHERE)
            updateLifeStatement(true)
        }
    }
    private fun resetPositions(currentLevel: Int) {
        pacman.updatePosition(
            levelsData[currentLevel]?.pacmanDefaultPosition ?: Position(-1, -1)
        )
        blinky.updatePosition(
            levelsData[currentLevel]?.blinkyDefaultPosition ?: Position(-1, -1)
        )
        inky.updatePosition(
            levelsData[currentLevel]?.inkyDefaultPosition ?: Position(-1, -1)
        )
        pinky.updatePosition(
            levelsData[currentLevel]?.pinkyDefaultPosition ?: Position(-1, -1)
        )
        clyde.updatePosition(
            levelsData[currentLevel]?.clydeDefaultPosition ?: Position(-1, -1)
        )
    }
    private fun handleGhostTimer() {
        if (ghostTimerTarget == GameConstants.SCATTER_TIME) {
            ghostMode = GhostMode.CHASE
            ghostTimerTarget = GameConstants.CHASE_TIME
            centralTimerController.restartTimerController(CentralTimerController.GHOST_TIMER)
        } else {
            if (ghostTimerTarget == GameConstants.CHASE_TIME) {
                ghostMode = GhostMode.SCATTER
                ghostTimerTarget = GameConstants.SCATTER_TIME
                centralTimerController.restartTimerController(CentralTimerController.GHOST_TIMER)
            }
        }
    }
    private fun handleEnergizerTimer() {
        pacman.updateEnergizerStatus(false)
        gameSoundService.stopSound(R.raw.pacman_energizer_mode)
        sirenSoundPause.resume()
        counterEatingGhost = 0
        if (blinky.blinkyState.value.ghostLifeStatement) {
            actorsMovementsTimerController.setActorSpeedFactor(
                ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
                levelsData[boardController.boardState.value.currentLevel]?.blinkySpeedDelay
                    ?: ActorsMovementsTimerController.BASE_GHOST_SPEED_DELAY
            )
            blinky.changeSpeedDelay(actorsMovementsTimerController.getBlinkySpeedDelay().toLong())
        }
        centralTimerController.unpauseTimerController(CentralTimerController.GHOST_TIMER)
        centralTimerController.stopTimerController(CentralTimerController.ENERGIZER_TIMER)
    }
    private fun handleBellTimer() {
        boardController.updateCurrentMap(
            levelsData[boardController.boardState.value.currentLevel]?.pacmanDefaultPosition
                ?: Position(-1, -1), BoardController.EMPTY_SPACE
        )
        centralTimerController.stopTimerController(CentralTimerController.BELL_TIMER)
    }
    private fun clockManagement() {
        if (centralTimerController.getTimerTicksController(CentralTimerController.GHOST_TIMER) > ghostTimerTarget) {
            handleGhostTimer()
        }
        if (centralTimerController.getTimerTicksController(CentralTimerController.ENERGIZER_TIMER) > GameConstants.ENERGIZER_TIME) {
            handleEnergizerTimer()
        }
        if (centralTimerController.getTimerTicksController(CentralTimerController.BELL_TIMER) > GameConstants.BELL_TIME) {
            handleBellTimer()
        }
    }
    private fun calculateEatGhostScorer(): Int = when (counterEatingGhost) {
        1 -> 200
        2 -> 400
        3 -> 800
        4 -> 1600
        else -> 0
    }
    private fun configureGhostAndPacmanLevelDefaults(currentLevel: Int) {
        ghostMode = GhostMode.SCATTER
        pacmanSpeedDelay = pacmanSpeedDelay()
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.PACMAN_ENTITY_TYPE, pacmanSpeedDelay
        )
        blinky.scatterTarget = levelsData[currentLevel]?.blinkyScatterPosition ?: Position(-1, -1)
        inky.scatterTarget = levelsData[currentLevel]?.inkyScatterPosition ?: Position(-1, -1)
        pinky.scatterTarget = levelsData[currentLevel]?.pinkyScatterPosition ?: Position(-1, -1)
        clyde.scatterTarget = levelsData[currentLevel]?.clydeScatterPosition ?: Position(-1, -1)
        blinky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        inky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        pinky.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        clyde.home = levelsData[currentLevel]?.homeTargetPosition ?: Position(-1, -1)
        blinky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        blinky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        blinky.blinkyStandardSpeedDelay = levelsData[currentLevel]?.blinkySpeedDelay ?: 0
        actorsMovementsTimerController.setActorSpeedFactor(
            ActorsMovementsTimerController.BLINKY_ENTITY_TYPE,
            levelsData[currentLevel]?.blinkySpeedDelay ?: 0
        )
        inky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        inky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        pinky.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        pinky.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        clyde.homeXRange = levelsData[currentLevel]?.ghostHomeXRange ?: IntRange(-1, 0)
        clyde.homeYRange = levelsData[currentLevel]?.ghostHomeYRange ?: IntRange(-1, 0)
        pinky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        inky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        blinky.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
        clyde.doorTarget = levelsData[currentLevel]?.doorTarget ?: Position(-1, -1)
    }
    private fun pacmanSpeedDelay(): Int = when (bellsEaten) {
        0 -> 250
        1 -> 240
        2 -> 230
        3 -> 220
        4 -> 210
        else -> 200
    }
    private fun checkBellAppear() {
        val levelData = levelsData[boardController.boardState.value.currentLevel]
        if (levelData?.isBell != true) return
        if (boardController.boardState.value.remainFood > 140 || isBellAppear) return
        val bellPosition = levelData.pacmanDefaultPosition
        if (pacman.pacmanState.value.pacmanPosition == bellPosition || blinky.blinkyState.value.ghostPosition == bellPosition || pinky.pinkyState.value.ghostPosition == bellPosition || inky.inkyState.value.ghostPosition == bellPosition || clyde.clydeState.value.ghostPosition == bellPosition) return
        boardController.updateCurrentMap(bellPosition, BoardController.BELL_CHAR)
        isBellAppear = true
        centralTimerController.startTimerController(CentralTimerController.BELL_TIMER)
    }
    fun onPause() {
        if (boardController.boardState.value.gameStatus != GameStatus.ONGOING) return
        if (!isGameStarted) return
        pauseController.pause {
            gameJobIsPaused = true
            centralTimerController.pauseAllTimersController()
            actorsMovementsTimerController.pause()
            collisionHandler.pauseCollisionObservation()
            if (pacman.pacmanState.value.energizerStatus) gameSoundService.pauseSound(R.raw.pacman_energizer_mode)
            if (!sirenSoundPause.isPaused) sirenSoundPause.pause()
        }
    }
    fun onResume() {
        if (!gameJobIsPaused) return
        pauseController.resume {
            gameJobIsPaused = false
            centralTimerController.unpauseAllTimersController()
            actorsMovementsTimerController.resume()
            collisionHandler.resumeCollisionObservation()
            if (pacman.pacmanState.value.energizerStatus) gameSoundService.playSound(R.raw.pacman_energizer_mode)
            if (sirenSoundPause.isPaused) sirenSoundPause.resume()
        }
    }
    fun muteSounds() {
        gameSoundService.muteSounds()
    }
    fun recoverSounds() {
        gameSoundService.recoverSound()
    }
    // ===========================================
    // DYNAMIC FOOD SYSTEM INITIALIZATION
    // ===========================================
    private fun initializeDynamicFoodSystem() {
        val currentLevel = boardController.boardState.value.currentLevel
        // Get template map untuk scan posisi spawn
        val templateMap = transformIntoCharMatrix(
            levelsData[currentLevel]?.mapCharData ?: emptyList(),
            rows = levelsData[currentLevel]?.height ?: 0,
            columns = levelsData[currentLevel]?.width ?: 0
        )
        // Initialize spawn manager dengan template map
        foodSpawnManager.initializePositions(templateMap)
        // Validate spawn configuration
        if (!foodSpawnManager.validateSpawnConfiguration(currentLevel)) {
            Log.e("PacmanGame", "Invalid spawn configuration for level $currentLevel")
            return
        }
        // Clean existing foods dari map sebelum spawn baru
        cleanExistingFoodsFromMap()
        // Spawn foods untuk level ini
        foodSpawnManager.spawnFoodsForLevel(currentLevel, boardController)
        // Log status
        foodSpawnManager.logCurrentState()
        Log.d("PacmanGame", "Dynamic food system initialized for level $currentLevel")
    }
    private fun cleanExistingFoodsFromMap() {
        for (i in 0 until boardController.boardState.value.gameBoardData.getRows()) {
            for (j in 0 until boardController.boardState.value.gameBoardData.getColumns()) {
                val position = Position(i, j)
                val cell = boardController.boardState.value.gameBoardData.getElementByPosition(i, j)
                // Clean legacy food characters
                if (cell == GameConstants.RICE_CHAR || cell == GameConstants.FISH_CHAR ||
                    cell == GameConstants.VEGETABLE_CHAR || cell == GameConstants.FRUIT_CHAR) {
                    boardController.updateCurrentMap(position, BoardController.EMPTY_SPACE)
                }
                // Clean dynamic food characters
                if (GameConstants.isCarbFood(cell ?: ' ') || GameConstants.isProteinFood(cell ?: ' ') ||
                    GameConstants.isVegetableFood(cell ?: ' ') || GameConstants.isFruitFood(cell ?: ' ')) {
                    boardController.updateCurrentMap(position, BoardController.EMPTY_SPACE)
                }
            }
        }
        Log.d("PacmanGame", "Cleaned existing foods from map")
    }
    // ===========================================
    // LEGACY FOOD HANDLERS (BACKWARDS COMPATIBILITY)
    // ===========================================
    // Handler untuk makanan sehat legacy - Updated untuk compatibility
    private fun handleRiceCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        val currentRiceEaten = pacman.pacmanState.value.riceEaten + 1
        pacman.updateRiceEaten(currentRiceEaten)

        if (currentRiceEaten > GameConstants.MAX_RICE_PORTION) {
            pacman.updateHealth(pacman.pacmanState.value.health - 0.5f)
            gameSoundService.playSound(R.raw.pacman_death)
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }

        if (isLegacyFoodTargetMet()) {
            gameSoundService.playSound(R.raw.pacman_energizer_mode)
        }
    }

    private fun handleFishCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        val currentFishEaten = pacman.pacmanState.value.fishEaten + 1
        pacman.updateFishEaten(currentFishEaten)

        if (currentFishEaten > GameConstants.MAX_FISH_PORTION) {
            pacman.updateHealth(pacman.pacmanState.value.health - 0.5f)
            gameSoundService.playSound(R.raw.pacman_death)
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }

        if (isLegacyFoodTargetMet()) {
            gameSoundService.playSound(R.raw.pacman_energizer_mode)
        }
    }

    private fun handleVegetableCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        val currentVegetableEaten = pacman.pacmanState.value.vegetableEaten + 1
        pacman.updateVegetableEaten(currentVegetableEaten)

        if (currentVegetableEaten > GameConstants.MAX_VEGETABLE_PORTION) {
            pacman.updateHealth(pacman.pacmanState.value.health - 0.5f)
            gameSoundService.playSound(R.raw.pacman_death)
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }

        if (isLegacyFoodTargetMet()) {
            gameSoundService.playSound(R.raw.pacman_energizer_mode)
        }
    }

    private fun handleFruitCollision(position: Position) {
        boardController.entityGetsEat(position, GameConstants.PELLET_POINTS)
        val currentFruitEaten = pacman.pacmanState.value.fruitEaten + 1
        pacman.updateFruitEaten(currentFruitEaten)

        if (currentFruitEaten > GameConstants.MAX_FRUIT_PORTION) {
            pacman.updateHealth(pacman.pacmanState.value.health - 0.5f)
            gameSoundService.playSound(R.raw.pacman_death)
            if (pacman.pacmanState.value.health <= 0) {
                handlePacmanHit()
                return
            }
        }

        if (isLegacyFoodTargetMet()) {
            gameSoundService.playSound(R.raw.pacman_energizer_mode)
        }
    }

    // Tambahkan method baru
    private fun isLegacyFoodTargetMet(): Boolean {
        val pacmanData = pacman.pacmanState.value
        val targetRice = 3
        val targetFish = 2
        val targetVegetable = 3
        val targetFruit = 2

        val riceOk = pacmanData.riceEaten >= targetRice && pacmanData.riceEaten <= GameConstants.MAX_RICE_PORTION
        val fishOk = pacmanData.fishEaten >= targetFish && pacmanData.fishEaten <= GameConstants.MAX_FISH_PORTION
        val vegetableOk = pacmanData.vegetableEaten >= targetVegetable && pacmanData.vegetableEaten <= GameConstants.MAX_VEGETABLE_PORTION
        val fruitOk = pacmanData.fruitEaten >= targetFruit && pacmanData.fruitEaten <= GameConstants.MAX_FRUIT_PORTION

        return riceOk && fishOk && vegetableOk && fruitOk
    }
    // ===========================================
    // UTILITY METHODS FOR DYNAMIC FOOD SYSTEM
    // ===========================================
    fun getCurrentFoodStatus(): Map<String, Any> {
        return mapOf(
            "activeFoodCount" to foodSpawnManager.getActiveFoodCount(),
            "foodsByCategory" to foodSpawnManager.getActiveFoodsByCategory(),
            "currentLevel" to boardController.boardState.value.currentLevel,
            "pacmanFoodStatus" to pacman.pacmanState.value
        )
    }
    fun validateCurrentFoodSpawning(): Boolean {
        val currentLevel = boardController.boardState.value.currentLevel
        return foodSpawnManager.validateSpawnConfiguration(currentLevel)
    }
    // Debug method untuk development
    fun logDynamicFoodSystemStatus() {
        Log.d("PacmanGame", "=== DYNAMIC FOOD SYSTEM STATUS ===")
        Log.d("PacmanGame", "Current Level: ${boardController.boardState.value.currentLevel + 1}")
        Log.d("PacmanGame", "Expected Foods: ${GameConstants.getMaxInstancesForLevel(boardController.boardState.value.currentLevel)}")
        Log.d("PacmanGame", "Active Foods: ${foodSpawnManager.getActiveFoodCount()}")
        val pacmanData = pacman.pacmanState.value
        Log.d("PacmanGame", "Pacman Food Status:")
        Log.d("PacmanGame", "  Total Carbs: ${pacmanData.totalCarbsEaten + pacmanData.riceEaten}")
        Log.d("PacmanGame", "  Total Protein: ${pacmanData.totalProteinEaten + pacmanData.fishEaten}")
        Log.d("PacmanGame", "  Total Vegetables: ${pacmanData.totalVegetablesEaten + pacmanData.vegetableEaten}")
        Log.d("PacmanGame", "  Total Fruits: ${pacmanData.totalFruitsEaten + pacmanData.fruitEaten}")
        foodSpawnManager.logCurrentState()
    }
}