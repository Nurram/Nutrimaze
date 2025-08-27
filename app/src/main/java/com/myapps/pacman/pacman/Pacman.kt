package com.myapps.pacman.pacman

import android.util.Log
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.game.GameConstants
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.timer.ActorsMovementsTimerController
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Pacman(
    initialPosition: Position,
    initialDirection: Direction = Direction.RIGHT,
    initialEnergizerStatus: Boolean = false,
    private val actorsMovementsTimerController: ActorsMovementsTimerController
) {

    private var direction: Direction = initialDirection
    private var lifeStatement: Boolean = true
    private var energizerStatus: Boolean = initialEnergizerStatus
    private var currentPosition: Position = initialPosition

    private val _pacmanState = MutableStateFlow(
        PacmanData(
            currentPosition,
            direction,
            energizerStatus,
            actorsMovementsTimerController.getPacmanSpeedDelay().toLong(),
            lifeStatement
        )
    )
    val pacmanState: StateFlow<PacmanData> get() = _pacmanState

    suspend fun startMoving(
        movements: MutableList<Direction>,
        currentMap: () -> Matrix<Char>
    ){
        actorsMovementsTimerController.controlTime(ActorsMovementsTimerController.PACMAN_ENTITY_TYPE){
            updatePosition(
                movements,
                currentMap()
            )
        }
    }

    // UPDATED: Method updatePosition dengan logging detail
    private fun updatePosition(
        movements: MutableList<Direction>,
        currentMap: Matrix<Char>
    ) {
        Log.d("Pacman", "updatePosition called with movements: $movements")

        val primaryDirection = movements.getOrNull(0)
        val secondaryDirection = movements.getOrNull(1)

        Log.d("Pacman", "Primary direction: $primaryDirection, Secondary direction: $secondaryDirection")
        Log.d("Pacman", "Current position: $currentPosition")

        if (primaryDirection == null) {
            Log.d("Pacman", "No primary direction, returning")
            return
        }

        val newPosition = getPacmanPossiblePosition(currentPosition, primaryDirection)
        Log.d("Pacman", "New position would be: $newPosition")

        if (checkTransfer(newPosition, primaryDirection, currentMap)) {
            Log.d("Pacman", "Transfer occurred")
            updatePacmanState(currentPosition, direction)
            return
        }

        if (secondaryDirection != null && primaryDirection != secondaryDirection) {
            val newSecondPosition = getPacmanPossiblePosition(currentPosition, secondaryDirection)
            Log.d("Pacman", "Checking secondary direction, new position: $newSecondPosition")
            if (!isWallCollision(newSecondPosition, currentMap)) {
                Log.d("Pacman", "Moving with secondary direction: $secondaryDirection")
                move(secondaryDirection)
                this.direction = secondaryDirection
                updatePacmanState(currentPosition, secondaryDirection)
                movements.removeAt(0)
                return
            } else {
                Log.d("Pacman", "Secondary direction blocked by wall")
            }
        }

        if (!isWallCollision(newPosition, currentMap)) {
            Log.d("Pacman", "Moving with primary direction: $primaryDirection")
            move(primaryDirection)
            this.direction = primaryDirection
            updatePacmanState(currentPosition, primaryDirection)
            return
        } else {
            Log.d("Pacman", "Primary direction blocked by wall")
        }

        Log.d("Pacman", "No movement possible")
    }

    private fun getPacmanPossiblePosition(position: Position, direction: Direction): Position =
        when (direction) {
            Direction.RIGHT -> position.copy(positionY = position.positionY + 1)
            Direction.LEFT -> position.copy(positionY = position.positionY - 1)
            Direction.DOWN -> position.copy(positionX = position.positionX + 1)
            Direction.UP -> position.copy(positionX = position.positionX - 1)
            Direction.NOWHERE -> position
        }

    private fun isWallCollision(position: Position, currentMap: Matrix<Char>): Boolean {
        val elementPosition =
            currentMap.getElementByPosition(position.positionX, position.positionY)
        return elementPosition == BoardController.WALL_CHAR || elementPosition == BoardController.GHOST_DOOR_CHAR
    }

    private fun checkTransfer(
        position: Position,
        direction: Direction,
        currentMap: Matrix<Char>
    ): Boolean = when (direction) {
        Direction.RIGHT -> {
            if (position.positionY >= currentMap.getColumns()) {
                // Transferir al borde izquierdo
                currentPosition = currentPosition.copy(positionY = 0)
                true
            } else false
        }
        Direction.LEFT -> {
            if (position.positionY < 0) {
                // Transferir al borde derecho
                currentPosition = currentPosition.copy(positionY = currentMap.getColumns() - 1)
                true
            } else false
        }
        else -> false
    }

    private fun move(direction: Direction) {
        Log.d("Pacman", "move() called with direction: $direction")
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
        Log.d("Pacman", "New position after move: $currentPosition")
    }

    private fun updatePacmanState(newPosition: Position, newDirection: Direction) {
        Log.d("Pacman", "updatePacmanState called - position: $newPosition, direction: $newDirection")
        if (_pacmanState.value.pacmanPosition != newPosition || _pacmanState.value.pacmanDirection != newDirection) {
            _pacmanState.value = _pacmanState.value.copy(
                pacmanPosition = newPosition,
                pacmanDirection = newDirection
            )
            Log.d("Pacman", "Pacman state updated")
        } else {
            Log.d("Pacman", "Pacman state unchanged")
        }
    }

    fun updateDirection(direction: Direction){
        Log.d("Pacman", "updateDirection called with: $direction")
        this.direction = direction
        _pacmanState.value = _pacmanState.value.copy(
            pacmanDirection = this.direction
        )
    }

    fun updatePosition(position: Position){
        Log.d("Pacman", "updatePosition(Position) called with: $position")
        this.currentPosition = position
        _pacmanState.value = _pacmanState.value.copy(
            pacmanPosition = currentPosition
        )
    }

    fun updateLifeStatement(lifeStatement:Boolean){
        this.lifeStatement = lifeStatement
        _pacmanState.value = _pacmanState.value.copy(
            lifeStatement = this.lifeStatement
        )
    }

    fun updateEnergizerStatus(energizerStatus: Boolean){
        this.energizerStatus = energizerStatus
        _pacmanState.value = _pacmanState.value.copy(
            energizerStatus = this.energizerStatus
        )
    }

    fun updateSpeedDelay(speedDelay: Int){
        actorsMovementsTimerController.setActorSpeedFactor(ActorsMovementsTimerController.PACMAN_ENTITY_TYPE,speedDelay)
        _pacmanState.value = _pacmanState.value.copy(
            speedDelay = actorsMovementsTimerController.getPacmanSpeedDelay().toLong()
        )
    }

    // Di dalam class Pacman
    fun updateRiceEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(riceEaten = count)
    }

    fun updateFishEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(fishEaten = count)
    }

    fun updateVegetableEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(vegetableEaten = count)
    }

    fun updateFruitEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(fruitEaten = count)
    }

    fun updateHealth(health: Float) {
        _pacmanState.value = _pacmanState.value.copy(health = health)
    }

    // Tambahkan method-method ini ke dalam class Pacman.kt yang sudah ada

    // ===========================================
    // DYNAMIC FOOD UPDATE METHODS - KARBOHIDRAT
    // ===========================================
    fun updateNasiEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(nasiEaten = count)
    }

    fun updateUbiEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(ubiEaten = count)
    }

    fun updateKentangEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(kentangEaten = count)
    }

    fun updateSingkongEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(singkongEaten = count)
    }

    fun updateJagungEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(jagungEaten = count)
    }

    // ===========================================
    // DYNAMIC FOOD UPDATE METHODS - PROTEIN
    // ===========================================
    fun updateIkanEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(ikanEaten = count)
    }

    fun updateAyamEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(ayamEaten = count)
    }

    fun updateTempeEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(tempeEaten = count)
    }

    fun updateTahuEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(tahuEaten = count)
    }

    fun updateKacangEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(kacangEaten = count)
    }

    // ===========================================
    // DYNAMIC FOOD UPDATE METHODS - SAYURAN
    // ===========================================
    fun updateBayamEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(bayamEaten = count)
    }

    fun updateBrokoliEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(brokoliEaten = count)
    }

    fun updateWortelEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(wortelEaten = count)
    }

    fun updateKangkungEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(kangkungEaten = count)
    }

    fun updateSawiEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(sawiEaten = count)
    }

    // ===========================================
    // DYNAMIC FOOD UPDATE METHODS - BUAH
    // ===========================================
    fun updateApelEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(apelEaten = count)
    }

    fun updatePisangEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(pisangEaten = count)
    }

    fun updateJerukEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(jerukEaten = count)
    }

    fun updatePepayaEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(pepayaEaten = count)
    }

    fun updateManggaEaten(count: Int) {
        _pacmanState.value = _pacmanState.value.copy(manggaEaten = count)
    }

    // ===========================================
    // RESET ALL FOOD COUNTERS METHOD
    // ===========================================

    fun resetAllFoodCounters() {
        _pacmanState.value = _pacmanState.value.copy(
            // Legacy (backwards compatibility)
            riceEaten = 0,
            fishEaten = 0,
            vegetableEaten = 0,
            fruitEaten = 0,

            // Karbohidrat
            nasiEaten = 0,
            ubiEaten = 0,
            kentangEaten = 0,
            singkongEaten = 0,
            jagungEaten = 0,

            // Protein
            ikanEaten = 0,
            ayamEaten = 0,
            tempeEaten = 0,
            tahuEaten = 0,
            kacangEaten = 0,

            // Sayuran
            bayamEaten = 0,
            brokoliEaten = 0,
            wortelEaten = 0,
            kangkungEaten = 0,
            sawiEaten = 0,

            // Buah
            apelEaten = 0,
            pisangEaten = 0,
            jerukEaten = 0,
            pepayaEaten = 0,
            manggaEaten = 0
        )
        Log.d("Pacman", "All food counters reset")
    }

    // ===========================================
    // GET FOOD COUNT BY CHARACTER METHOD
    // ===========================================

    fun getFoodCountByChar(char: Char): Int {
        return _pacmanState.value.getFoodCountByChar(char)
    }

    // ===========================================
    // CATEGORY-BASED FOOD COUNTER METHODS
    // ===========================================

    fun getTotalCarbsConsumed(): Int {
        return _pacmanState.value.totalCarbsEaten + _pacmanState.value.riceEaten
    }

    fun getTotalProteinConsumed(): Int {
        return _pacmanState.value.totalProteinEaten + _pacmanState.value.fishEaten
    }

    fun getTotalVegetablesConsumed(): Int {
        return _pacmanState.value.totalVegetablesEaten + _pacmanState.value.vegetableEaten
    }

    fun getTotalFruitsConsumed(): Int {
        return _pacmanState.value.totalFruitsEaten + _pacmanState.value.fruitEaten
    }

    // ===========================================
    // TARGET AND LIMIT CHECKING METHODS
    // ===========================================

    fun isTargetAchieved(): Boolean {
        return _pacmanState.value.isLevelTargetAchieved()
    }

    fun hasExceededAnyLimit(): Boolean {
        return _pacmanState.value.hasExceededLimits()
    }

    fun getFoodVarietyScore(): Int {
        return _pacmanState.value.getFoodVarietyScore()
    }

    // ===========================================
    // LOGGING AND DEBUG METHODS
    // ===========================================

    fun logCurrentFoodStatus() {
        val state = _pacmanState.value
        Log.d("Pacman", "=== CURRENT FOOD STATUS ===")
        Log.d("Pacman", "Health: ${state.health}")

        Log.d("Pacman", "Legacy Totals:")
        Log.d("Pacman", "  Rice: ${state.riceEaten}")
        Log.d("Pacman", "  Fish: ${state.fishEaten}")
        Log.d("Pacman", "  Vegetable: ${state.vegetableEaten}")
        Log.d("Pacman", "  Fruit: ${state.fruitEaten}")

        Log.d("Pacman", "Dynamic Totals:")
        Log.d("Pacman", "  Total Carbs: ${state.totalCarbsEaten}")
        Log.d("Pacman", "  Total Protein: ${state.totalProteinEaten}")
        Log.d("Pacman", "  Total Vegetables: ${state.totalVegetablesEaten}")
        Log.d("Pacman", "  Total Fruits: ${state.totalFruitsEaten}")

        Log.d("Pacman", "Individual Foods:")
        Log.d("Pacman", "  Nasi: ${state.nasiEaten}, Ubi: ${state.ubiEaten}, Kentang: ${state.kentangEaten}")
        Log.d("Pacman", "  Ikan: ${state.ikanEaten}, Ayam: ${state.ayamEaten}, Tempe: ${state.tempeEaten}")
        Log.d("Pacman", "  Bayam: ${state.bayamEaten}, Brokoli: ${state.brokoliEaten}, Wortel: ${state.wortelEaten}")
        Log.d("Pacman", "  Apel: ${state.apelEaten}, Pisang: ${state.pisangEaten}, Jeruk: ${state.jerukEaten}")
        Log.d("Pacman", "  Kangkung: ${state.kangkungEaten}, Sawi: ${state.sawiEaten}")
        Log.d("Pacman", "  Singkong: ${state.singkongEaten}, Tahu: ${state.tahuEaten}, Kacang: ${state.kacangEaten}")
        Log.d("Pacman", "  Pepaya: ${state.pepayaEaten}, Mangga: ${state.manggaEaten}, Jagung: ${state.jagungEaten}")


        Log.d("Pacman", "Food Variety Score: ${state.getFoodVarietyScore()}")
        Log.d("Pacman", "Target Achieved: ${state.isLevelTargetAchieved()}")
        Log.d("Pacman", "Limits Exceeded: ${state.hasExceededLimits()}")
    }

    // ===========================================
    // SAFE UPDATE METHODS WITH VALIDATION
    // ===========================================

    fun updateFoodCountSafely(foodChar: Char, newCount: Int) {
        if (newCount < 0) {
            Log.w("Pacman", "Attempted to set negative food count for ${GameConstants.getFoodName(foodChar)}")
            return
        }

        when (foodChar) {
            GameConstants.NASI_CHAR -> updateNasiEaten(newCount)
            GameConstants.UBI_CHAR -> updateUbiEaten(newCount)
            GameConstants.KENTANG_CHAR -> updateKentangEaten(newCount)
            GameConstants.SINGKONG_CHAR -> updateSingkongEaten(newCount)
            GameConstants.JAGUNG_CHAR -> updateJagungEaten(newCount)

            GameConstants.IKAN_CHAR -> updateIkanEaten(newCount)
            GameConstants.AYAM_CHAR -> updateAyamEaten(newCount)
            GameConstants.TEMPE_CHAR -> updateTempeEaten(newCount)
            GameConstants.TAHU_CHAR -> updateTahuEaten(newCount)
            GameConstants.KACANG_CHAR -> updateKacangEaten(newCount)

            GameConstants.BAYAM_CHAR -> updateBayamEaten(newCount)
            GameConstants.BROKOLI_CHAR -> updateBrokoliEaten(newCount)
            GameConstants.WORTEL_CHAR -> updateWortelEaten(newCount)
            GameConstants.KANGKUNG_CHAR -> updateKangkungEaten(newCount)
            GameConstants.SAWI_CHAR -> updateSawiEaten(newCount)

            GameConstants.APEL_CHAR -> updateApelEaten(newCount)
            GameConstants.PISANG_CHAR -> updatePisangEaten(newCount)
            GameConstants.JERUK_CHAR -> updateJerukEaten(newCount)
            GameConstants.PEPAYA_CHAR -> updatePepayaEaten(newCount)
            GameConstants.MANGGA_CHAR -> updateManggaEaten(newCount)

            // Legacy foods
            GameConstants.RICE_CHAR -> updateRiceEaten(newCount)
            GameConstants.FISH_CHAR -> updateFishEaten(newCount)
            GameConstants.VEGETABLE_CHAR -> updateVegetableEaten(newCount)
            GameConstants.FRUIT_CHAR -> updateFruitEaten(newCount)

            else -> Log.w("Pacman", "Unknown food character: $foodChar")
        }
    }
}