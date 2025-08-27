package com.myapps.pacman.states

import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.Position

data class PacmanData(
    val pacmanPosition: Position = Position(-1,-1),
    val pacmanDirection: Direction = Direction.RIGHT,
    val energizerStatus: Boolean = false,
    val speedDelay: Long = 0L,
    val lifeStatement: Boolean = true,

    // Legacy food tracking (backwards compatibility)
    val riceEaten: Int = 0,
    val fishEaten: Int = 0,
    val vegetableEaten: Int = 0,
    val fruitEaten: Int = 0,

    // Health tracking (sebagai float untuk pengurangan 0.5)
    val health: Float = 3.0f,

    // ===========================================
    // DYNAMIC FOOD TRACKING - KARBOHIDRAT
    // ===========================================
    val nasiEaten: Int = 0,
    val ubiEaten: Int = 0,
    val kentangEaten: Int = 0,
    val singkongEaten: Int = 0,
    val jagungEaten: Int = 0,

    // ===========================================
    // DYNAMIC FOOD TRACKING - PROTEIN
    // ===========================================
    val ikanEaten: Int = 0,
    val ayamEaten: Int = 0,
    val tempeEaten: Int = 0,
    val tahuEaten: Int = 0,
    val kacangEaten: Int = 0,

    // ===========================================
    // DYNAMIC FOOD TRACKING - SAYURAN
    // ===========================================
    val bayamEaten: Int = 0,
    val brokoliEaten: Int = 0,
    val wortelEaten: Int = 0,
    val kangkungEaten: Int = 0,
    val sawiEaten: Int = 0,

    // ===========================================
    // DYNAMIC FOOD TRACKING - BUAH
    // ===========================================
    val apelEaten: Int = 0,
    val pisangEaten: Int = 0,
    val jerukEaten: Int = 0,
    val pepayaEaten: Int = 0,
    val manggaEaten: Int = 0
) {
    // ===========================================
    // COMPUTED PROPERTIES FOR CATEGORY TOTALS
    // ===========================================

    val totalCarbsEaten: Int
        get() = nasiEaten + ubiEaten + kentangEaten + singkongEaten + jagungEaten

    val totalProteinEaten: Int
        get() = ikanEaten + ayamEaten + tempeEaten + tahuEaten + kacangEaten

    val totalVegetablesEaten: Int
        get() = bayamEaten + brokoliEaten + wortelEaten + kangkungEaten + sawiEaten

    val totalFruitsEaten: Int
        get() = apelEaten + pisangEaten + jerukEaten + pepayaEaten + manggaEaten

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    fun getFoodCountByChar(char: Char): Int {
        return when (char) {
            // Karbohidrat
            '1' -> nasiEaten      // NASI_CHAR
            '2' -> ubiEaten       // UBI_CHAR
            '3' -> kentangEaten   // KENTANG_CHAR
            '4' -> singkongEaten  // SINGKONG_CHAR
            '5' -> jagungEaten    // JAGUNG_CHAR

            // Protein
            '6' -> ikanEaten      // IKAN_CHAR
            '7' -> ayamEaten      // AYAM_CHAR
            '8' -> tempeEaten     // TEMPE_CHAR
            '9' -> tahuEaten      // TAHU_CHAR
            'A' -> kacangEaten    // KACANG_CHAR

            // Sayuran
            'B' -> bayamEaten     // BAYAM_CHAR
            'C' -> brokoliEaten   // BROKOLI_CHAR
            'D' -> wortelEaten    // WORTEL_CHAR
            'E' -> kangkungEaten  // KANGKUNG_CHAR
            'F' -> sawiEaten      // SAWI_CHAR

            // Buah
            'G' -> apelEaten      // APEL_CHAR
            'H' -> pisangEaten    // PISANG_CHAR
            'J' -> jerukEaten     // JERUK_CHAR
            'K' -> pepayaEaten    // PEPAYA_CHAR
            'L' -> manggaEaten    // MANGGA_CHAR

            // Legacy
            'n' -> riceEaten      // RICE_CHAR
            'i' -> fishEaten      // FISH_CHAR
            'v' -> vegetableEaten // VEGETABLE_CHAR
            'b' -> fruitEaten     // FRUIT_CHAR

            else -> 0
        }
    }

    fun getCategoryTotal(category: String): Int {
        return when (category.lowercase()) {
            "karbohidrat", "carbs", "makanan pokok" -> totalCarbsEaten
            "protein", "lauk pauk" -> totalProteinEaten
            "sayuran", "vegetables" -> totalVegetablesEaten
            "buah", "fruits", "buah-buahan" -> totalFruitsEaten
            else -> 0
        }
    }

    // Check if target achieved for level progression
    fun isLevelTargetAchieved(): Boolean {
        val targetCarbs = 3
        val targetProtein = 2
        val targetVegetables = 3
        val targetFruits = 2

        val carbsOk = (totalCarbsEaten + riceEaten) >= targetCarbs
        val proteinOk = (totalProteinEaten + fishEaten) >= targetProtein
        val vegetablesOk = (totalVegetablesEaten + vegetableEaten) >= targetVegetables
        val fruitsOk = (totalFruitsEaten + fruitEaten) >= targetFruits

        return carbsOk && proteinOk && vegetablesOk && fruitsOk
    }

    // Check if any category exceeded limits
    fun hasExceededLimits(): Boolean {
        val maxCarbs = 4
        val maxProtein = 3
        val maxVegetables = 4
        val maxFruits = 3

        val carbsExceeded = (totalCarbsEaten + riceEaten) > maxCarbs
        val proteinExceeded = (totalProteinEaten + fishEaten) > maxProtein
        val vegetablesExceeded = (totalVegetablesEaten + vegetableEaten) > maxVegetables
        val fruitsExceeded = (totalFruitsEaten + fruitEaten) > maxFruits

        return carbsExceeded || proteinExceeded || vegetablesExceeded || fruitsExceeded
    }

    // Get variety score (number of different foods consumed)
    fun getFoodVarietyScore(): Int {
        var variety = 0

        // Count individual foods consumed (not counting legacy totals)
        if (nasiEaten > 0) variety++
        if (ubiEaten > 0) variety++
        if (kentangEaten > 0) variety++
        if (singkongEaten > 0) variety++
        if (jagungEaten > 0) variety++

        if (ikanEaten > 0) variety++
        if (ayamEaten > 0) variety++
        if (tempeEaten > 0) variety++
        if (tahuEaten > 0) variety++
        if (kacangEaten > 0) variety++

        if (bayamEaten > 0) variety++
        if (brokoliEaten > 0) variety++
        if (wortelEaten > 0) variety++
        if (kangkungEaten > 0) variety++
        if (sawiEaten > 0) variety++

        if (apelEaten > 0) variety++
        if (pisangEaten > 0) variety++
        if (jerukEaten > 0) variety++
        if (pepayaEaten > 0) variety++
        if (manggaEaten > 0) variety++

        return variety
    }
}