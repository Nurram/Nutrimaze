package com.myapps.pacman.game

import android.util.Log
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.utils.Position
import com.myapps.pacman.utils.matrix.Matrix

class FoodSpawnManager {

    // ===========================================
    // POSITION TRACKING BY CATEGORY
    // ===========================================
    private val carbPositions = mutableListOf<Position>()    // n positions
    private val proteinPositions = mutableListOf<Position>() // i positions
    private val vegetablePositions = mutableListOf<Position>() // v positions
    private val fruitPositions = mutableListOf<Position>()   // b positions

    // ===========================================
    // ACTIVE FOOD TRACKING - MULTI-INSTANCE
    // ===========================================
    private val activeFoods = mutableMapOf<Position, Char>()
    private val activeFoodsByType = mutableMapOf<Char, MutableList<Position>>()

    // ===========================================
    // INITIALIZATION
    // ===========================================

    fun initializePositions(templateMap: Matrix<Char>) {
        carbPositions.clear()
        proteinPositions.clear()
        vegetablePositions.clear()
        fruitPositions.clear()
        activeFoods.clear()
        activeFoodsByType.clear()

        // Scan template map untuk posisi spawn berdasarkan karakter legacy
        for (i in 0 until templateMap.getRows()) {
            for (j in 0 until templateMap.getColumns()) {
                val cell = templateMap.getElementByPosition(i, j)
                val position = Position(i, j)

                when (cell) {
                    GameConstants.RICE_CHAR -> carbPositions.add(position)        // 'n'
                    GameConstants.FISH_CHAR -> proteinPositions.add(position)     // 'i'
                    GameConstants.VEGETABLE_CHAR -> vegetablePositions.add(position) // 'v'
                    GameConstants.FRUIT_CHAR -> fruitPositions.add(position)      // 'b'
                }
            }
        }

        Log.d("FoodSpawnManager", "Initialized positions - Carbs: ${carbPositions.size}, Protein: ${proteinPositions.size}, Vegetables: ${vegetablePositions.size}, Fruits: ${fruitPositions.size}")
    }

    // ===========================================
    // MULTI-INSTANCE FOOD SPAWNING FOR LEVELS
    // ===========================================

    fun spawnFoodsForLevel(level: Int, boardController: BoardController) {
        clearAllActiveFoods(boardController)

        val foodsToSpawn = GameConstants.getFoodsForLevel(level)
        Log.d("FoodSpawnManager", "Spawning ${foodsToSpawn.size} foods for level $level: $foodsToSpawn")

        foodsToSpawn.forEach { foodChar ->
            spawnFoodOfType(foodChar, boardController)
        }

        Log.d("FoodSpawnManager", "Level $level spawning complete. Active foods: ${activeFoods.size}")
    }

    private fun spawnFoodOfType(foodChar: Char, boardController: BoardController) {
        val availablePositions = getAvailablePositionsForFood(foodChar)

        if (availablePositions.isNotEmpty()) {
            val spawnPosition = availablePositions.random()

            // Place food on map
            boardController.updateCurrentMap(spawnPosition, foodChar)

            // Track active food
            activeFoods[spawnPosition] = foodChar
            activeFoodsByType.getOrPut(foodChar) { mutableListOf() }.add(spawnPosition)

            Log.d("FoodSpawnManager", "Spawned ${GameConstants.getFoodName(foodChar)} at $spawnPosition")
        } else {
            Log.w("FoodSpawnManager", "No available positions for ${GameConstants.getFoodName(foodChar)}")
        }
    }

    // ===========================================
    // SAME-TYPE RESPAWN SYSTEM
    // ===========================================

    fun respawnSameTypeFood(eatenPosition: Position, eatenFoodChar: Char, boardController: BoardController) {
        // Remove from active tracking
        activeFoods.remove(eatenPosition)
        activeFoodsByType[eatenFoodChar]?.remove(eatenPosition)

        // Find available positions for the same food type
        val availablePositions = getAvailablePositionsForFood(eatenFoodChar)

        if (availablePositions.isNotEmpty()) {
            val respawnPosition = availablePositions.random()

            // Spawn same food type at new position
            boardController.updateCurrentMap(respawnPosition, eatenFoodChar)

            // Update tracking
            activeFoods[respawnPosition] = eatenFoodChar
            activeFoodsByType.getOrPut(eatenFoodChar) { mutableListOf() }.add(respawnPosition)

            Log.d("FoodSpawnManager", "Respawned ${GameConstants.getFoodName(eatenFoodChar)} from $eatenPosition to $respawnPosition")
        } else {
            Log.w("FoodSpawnManager", "No available positions to respawn ${GameConstants.getFoodName(eatenFoodChar)}")
        }
    }

    // ===========================================
    // POSITION MANAGEMENT
    // ===========================================

    private fun getAvailablePositionsForFood(foodChar: Char): List<Position> {
        val categoryPositions = when {
            GameConstants.isCarbFood(foodChar) -> carbPositions
            GameConstants.isProteinFood(foodChar) -> proteinPositions
            GameConstants.isVegetableFood(foodChar) -> vegetablePositions
            GameConstants.isFruitFood(foodChar) -> fruitPositions
            else -> emptyList()
        }

        // Return positions that are not currently occupied
        return categoryPositions.filter { position ->
            !activeFoods.containsKey(position)
        }
    }

    private fun clearAllActiveFoods(boardController: BoardController) {
        // Clear from map
        activeFoods.keys.forEach { position ->
            boardController.updateCurrentMap(position, BoardController.EMPTY_SPACE)
        }

        // Clear tracking
        activeFoods.clear()
        activeFoodsByType.clear()

        Log.d("FoodSpawnManager", "Cleared all active foods")
    }

    // ===========================================
    // INFORMATION METHODS
    // ===========================================

    fun getActiveFoodCount(): Int = activeFoods.size

    fun getActiveFoodsByCategory(): Map<String, Int> {
        val counts = mutableMapOf(
            "Karbohidrat" to 0,
            "Protein" to 0,
            "Sayuran" to 0,
            "Buah" to 0
        )

        activeFoods.values.forEach { foodChar ->
            val category = GameConstants.getFoodCategory(foodChar)
            counts[category] = counts[category]!! + 1
        }

        return counts
    }

    fun getActiveFoodsOfType(foodChar: Char): List<Position> {
        return activeFoodsByType[foodChar] ?: emptyList()
    }

    fun isPositionOccupied(position: Position): Boolean {
        return activeFoods.containsKey(position)
    }

    fun getFoodAtPosition(position: Position): Char? {
        return activeFoods[position]
    }

    // ===========================================
    // DEBUG AND LOGGING
    // ===========================================

    fun logCurrentState() {
        Log.d("FoodSpawnManager", "=== CURRENT FOOD STATE ===")
        Log.d("FoodSpawnManager", "Total active foods: ${activeFoods.size}")

        val categoryBreakdown = getActiveFoodsByCategory()
        categoryBreakdown.forEach { (category, count) ->
            Log.d("FoodSpawnManager", "$category: $count foods")
        }

        activeFoodsByType.forEach { (foodChar, positions) ->
            Log.d("FoodSpawnManager", "${GameConstants.getFoodName(foodChar)}: ${positions.size} instances at $positions")
        }

        Log.d("FoodSpawnManager", "Available spawn positions - Carbs: ${carbPositions.size - getActiveFoodsOfCategory("Karbohidrat").size}, Protein: ${proteinPositions.size - getActiveFoodsOfCategory("Protein").size}, Vegetables: ${vegetablePositions.size - getActiveFoodsOfCategory("Sayuran").size}, Fruits: ${fruitPositions.size - getActiveFoodsOfCategory("Buah").size}")
    }

    private fun getActiveFoodsOfCategory(category: String): List<Position> {
        return activeFoods.entries.filter { (_, foodChar) ->
            GameConstants.getFoodCategory(foodChar) == category
        }.map { it.key }
    }

    // ===========================================
    // VALIDATION METHODS
    // ===========================================

    fun validateSpawnConfiguration(level: Int): Boolean {
        val requiredFoods = GameConstants.getFoodsForLevel(level)
        val availableSpawnSlots = carbPositions.size + proteinPositions.size +
                vegetablePositions.size + fruitPositions.size

        val isValid = requiredFoods.size <= availableSpawnSlots

        if (!isValid) {
            Log.e("FoodSpawnManager", "Invalid spawn configuration for level $level: Need ${requiredFoods.size} slots but only have $availableSpawnSlots")
        }

        return isValid
    }

    companion object {
        const val TAG = "FoodSpawnManager"
    }
}