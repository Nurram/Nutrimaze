package com.myapps.pacman.game

object GameConstants {
    const val SCATTER_TIME = 7
    const val CHASE_TIME = 20
    const val ENERGIZER_TIME = 6
    const val BELL_TIME = 10
    const val PELLET_POINTS = 10
    const val ENERGIZER_POINTS = 50
    const val SIREN_DELAY = 330L
    const val BELL_POINTS = 200
    const val BELL_REDUCTION_TIME = 10
    const val PACMAN_LIVES = 3

    // Tambahkan konstanta untuk batasan makanan
    const val MAX_RICE_PORTION = 5
    const val MAX_FISH_PORTION = 3
    const val MAX_VEGETABLE_PORTION = 3
    const val MAX_FRUIT_PORTION = 4

    // Konstanta untuk karakter di map (legacy - backwards compatibility)
    const val RICE_CHAR = 'n'
    const val FISH_CHAR = 'i'
    const val VEGETABLE_CHAR = 'v'
    const val FRUIT_CHAR = 'b'

    // ===========================================
    // DYNAMIC FOOD CHARACTER CONSTANTS - KARBOHIDRAT
    // ===========================================
    const val NASI_CHAR = '1'      // Menggunakan angka untuk menghindari konflik
    const val UBI_CHAR = '2'
    const val KENTANG_CHAR = '3'
    const val SINGKONG_CHAR = '4'
    const val JAGUNG_CHAR = '5'

    // ===========================================
    // DYNAMIC FOOD CHARACTER CONSTANTS - PROTEIN
    // ===========================================
    const val IKAN_CHAR = '6'
    const val AYAM_CHAR = '7'
    const val TEMPE_CHAR = '8'
    const val TAHU_CHAR = '9'
    const val KACANG_CHAR = 'A'

    // ===========================================
    // DYNAMIC FOOD CHARACTER CONSTANTS - SAYURAN
    // ===========================================
    const val BAYAM_CHAR = 'B'
    const val BROKOLI_CHAR = 'C'
    const val WORTEL_CHAR = 'D'
    const val KANGKUNG_CHAR = 'E'
    const val SAWI_CHAR = 'F'

    // ===========================================
    // DYNAMIC FOOD CHARACTER CONSTANTS - BUAH
    // ===========================================
    const val APEL_CHAR = 'G'
    const val PISANG_CHAR = 'H'
    const val JERUK_CHAR = 'J'
    const val PEPAYA_CHAR = 'K'
    const val MANGGA_CHAR = 'L'

    // ===========================================
    // FOOD CATEGORY UTILITY FUNCTIONS
    // ===========================================

    fun isCarbFood(char: Char): Boolean {
        return char in listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR, JAGUNG_CHAR, RICE_CHAR)
    }

    fun isProteinFood(char: Char): Boolean {
        return char in listOf(IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR, KACANG_CHAR, FISH_CHAR)
    }

    fun isVegetableFood(char: Char): Boolean {
        return char in listOf(BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR, SAWI_CHAR, VEGETABLE_CHAR)
    }

    fun isFruitFood(char: Char): Boolean {
        return char in listOf(APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR, MANGGA_CHAR, FRUIT_CHAR)
    }

    fun getFoodName(char: Char): String {
        return when (char) {
            // Karbohidrat
            NASI_CHAR -> "Nasi"
            UBI_CHAR -> "Ubi"
            KENTANG_CHAR -> "Kentang"
            SINGKONG_CHAR -> "Singkong"
            JAGUNG_CHAR -> "Jagung"

            // Protein
            IKAN_CHAR -> "Ikan"
            AYAM_CHAR -> "Ayam"
            TEMPE_CHAR -> "Tempe"
            TAHU_CHAR -> "Tahu"
            KACANG_CHAR -> "Kacang"

            // Sayuran
            BAYAM_CHAR -> "Bayam"
            BROKOLI_CHAR -> "Brokoli"
            WORTEL_CHAR -> "Wortel"
            KANGKUNG_CHAR -> "Kangkung"
            SAWI_CHAR -> "Sawi"

            // Buah
            APEL_CHAR -> "Apel"
            PISANG_CHAR -> "Pisang"
            JERUK_CHAR -> "Jeruk"
            PEPAYA_CHAR -> "Pepaya"
            MANGGA_CHAR -> "Mangga"

            // Legacy
            RICE_CHAR -> "Makanan Pokok"
            FISH_CHAR -> "Lauk Pauk"
            VEGETABLE_CHAR -> "Sayuran"
            FRUIT_CHAR -> "Buah-buahan"

            else -> "Unknown Food"
        }
    }

    // ===========================================
    // LEVEL-BASED FOOD CONFIGURATION
    // ===========================================

    fun getFoodsForLevel(level: Int): List<Char> {
        return when (level) {
            0 -> listOf(NASI_CHAR, IKAN_CHAR, BAYAM_CHAR, APEL_CHAR) // Level 1: 4 foods
            1 -> listOf(NASI_CHAR, UBI_CHAR, IKAN_CHAR, AYAM_CHAR, BAYAM_CHAR, BROKOLI_CHAR, APEL_CHAR, PISANG_CHAR) // Level 2: 8 foods
            2 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, APEL_CHAR, PISANG_CHAR, JERUK_CHAR) // Level 3: 12 foods
            3 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR, IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR, BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR, APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR) // Level 4: 16 foods
            4 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR, JAGUNG_CHAR, IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR, KACANG_CHAR, BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR, SAWI_CHAR, APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR, MANGGA_CHAR) // Level 5: 20 foods
            else -> getFoodsForLevel(4) // Default to max level
        }
    }

    fun getMaxInstancesForLevel(level: Int): Int {
        return when (level) {
            0 -> 4   // Level 1
            1 -> 8   // Level 2
            2 -> 12  // Level 3
            3 -> 16  // Level 4
            4 -> 20  // Level 5
            else -> 20
        }
    }

    fun getCarbFoodsForLevel(level: Int): List<Char> {
        return when (level) {
            0 -> listOf(NASI_CHAR)
            1 -> listOf(NASI_CHAR, UBI_CHAR)
            2 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR)
            3 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR)
            4 -> listOf(NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR, JAGUNG_CHAR)
            else -> getCarbFoodsForLevel(4)
        }
    }

    fun getProteinFoodsForLevel(level: Int): List<Char> {
        return when (level) {
            0 -> listOf(IKAN_CHAR)
            1 -> listOf(IKAN_CHAR, AYAM_CHAR)
            2 -> listOf(IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR)
            3 -> listOf(IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR)
            4 -> listOf(IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR, KACANG_CHAR)
            else -> getProteinFoodsForLevel(4)
        }
    }

    fun getVegetableFoodsForLevel(level: Int): List<Char> {
        return when (level) {
            0 -> listOf(BAYAM_CHAR)
            1 -> listOf(BAYAM_CHAR, BROKOLI_CHAR)
            2 -> listOf(BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR)
            3 -> listOf(BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR)
            4 -> listOf(BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR, SAWI_CHAR)
            else -> getVegetableFoodsForLevel(4)
        }
    }

    fun getFruitFoodsForLevel(level: Int): List<Char> {
        return when (level) {
            0 -> listOf(APEL_CHAR)
            1 -> listOf(APEL_CHAR, PISANG_CHAR)
            2 -> listOf(APEL_CHAR, PISANG_CHAR, JERUK_CHAR)
            3 -> listOf(APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR)
            4 -> listOf(APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR, MANGGA_CHAR)
            else -> getFruitFoodsForLevel(4)
        }
    }

    // ===========================================
    // FOOD CATEGORY MAPPING
    // ===========================================

    fun getFoodCategory(char: Char): String {
        return when {
            isCarbFood(char) -> "Karbohidrat"
            isProteinFood(char) -> "Protein"
            isVegetableFood(char) -> "Sayuran"
            isFruitFood(char) -> "Buah"
            else -> "Unknown"
        }
    }

    fun isDynamicFood(char: Char): Boolean {
        return char in listOf(
            NASI_CHAR, UBI_CHAR, KENTANG_CHAR, SINGKONG_CHAR, JAGUNG_CHAR,
            IKAN_CHAR, AYAM_CHAR, TEMPE_CHAR, TAHU_CHAR, KACANG_CHAR,
            BAYAM_CHAR, BROKOLI_CHAR, WORTEL_CHAR, KANGKUNG_CHAR, SAWI_CHAR,
            APEL_CHAR, PISANG_CHAR, JERUK_CHAR, PEPAYA_CHAR, MANGGA_CHAR
        )
    }

    fun isLegacyFood(char: Char): Boolean {
        return char in listOf(RICE_CHAR, FISH_CHAR, VEGETABLE_CHAR, FRUIT_CHAR)
    }

    // ===========================================
    // LEGACY CHARACTER MAPPING (untuk backwards compatibility)
    // ===========================================

    fun mapLegacyToSpecific(legacyChar: Char, level: Int): List<Char> {
        return when (legacyChar) {
            RICE_CHAR -> getCarbFoodsForLevel(level)
            FISH_CHAR -> getProteinFoodsForLevel(level)
            VEGETABLE_CHAR -> getVegetableFoodsForLevel(level)
            FRUIT_CHAR -> getFruitFoodsForLevel(level)
            else -> emptyList()
        }
    }
}