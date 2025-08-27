package com.myapps.pacman.utils

enum class TypeOfCollision {
    // Basic collision types
    PELLET,
    ENERGIZER,
    BELL,
    WALL,
    NONE,

    // Legacy food types (backwards compatibility)
    RICE,
    FISH,
    VEGETABLE,
    FRUIT,

    // ===========================================
    // DYNAMIC FOOD COLLISION TYPES - KARBOHIDRAT
    // ===========================================
    NASI,
    UBI,
    KENTANG,
    SINGKONG,
    JAGUNG,

    // ===========================================
    // DYNAMIC FOOD COLLISION TYPES - PROTEIN
    // ===========================================
    IKAN,
    AYAM,
    TEMPE,
    TAHU,
    KACANG,

    // ===========================================
    // DYNAMIC FOOD COLLISION TYPES - SAYURAN
    // ===========================================
    BAYAM,
    BROKOLI,
    WORTEL,
    KANGKUNG,
    SAWI,

    // ===========================================
    // DYNAMIC FOOD COLLISION TYPES - BUAH
    // ===========================================
    APEL,
    PISANG,
    JERUK,
    PEPAYA,
    MANGGA;

    // ===========================================
    // UTILITY METHODS
    // ===========================================

    fun isFood(): Boolean {
        return this in listOf(
            RICE, FISH, VEGETABLE, FRUIT,
            NASI, UBI, KENTANG, SINGKONG, JAGUNG,
            IKAN, AYAM, TEMPE, TAHU, KACANG,
            BAYAM, BROKOLI, WORTEL, KANGKUNG, SAWI,
            APEL, PISANG, JERUK, PEPAYA, MANGGA
        )
    }

    fun isCarbFood(): Boolean {
        return this in listOf(RICE, NASI, UBI, KENTANG, SINGKONG, JAGUNG)
    }

    fun isProteinFood(): Boolean {
        return this in listOf(FISH, IKAN, AYAM, TEMPE, TAHU, KACANG)
    }

    fun isVegetableFood(): Boolean {
        return this in listOf(VEGETABLE, BAYAM, BROKOLI, WORTEL, KANGKUNG, SAWI)
    }

    fun isFruitFood(): Boolean {
        return this in listOf(FRUIT, APEL, PISANG, JERUK, PEPAYA, MANGGA)
    }

    fun isLegacyFood(): Boolean {
        return this in listOf(RICE, FISH, VEGETABLE, FRUIT)
    }

    fun isDynamicFood(): Boolean {
        return isFood() && !isLegacyFood()
    }

    fun getFoodCategory(): String {
        return when {
            isCarbFood() -> "Karbohidrat"
            isProteinFood() -> "Protein"
            isVegetableFood() -> "Sayuran"
            isFruitFood() -> "Buah"
            else -> "Unknown"
        }
    }

    fun getFoodName(): String {
        return when (this) {
            // Legacy foods
            RICE -> "Makanan Pokok"
            FISH -> "Lauk Pauk"
            VEGETABLE -> "Sayuran"
            FRUIT -> "Buah-buahan"

            // Karbohidrat
            NASI -> "Nasi"
            UBI -> "Ubi"
            KENTANG -> "Kentang"
            SINGKONG -> "Singkong"
            JAGUNG -> "Jagung"

            // Protein
            IKAN -> "Ikan"
            AYAM -> "Ayam"
            TEMPE -> "Tempe"
            TAHU -> "Tahu"
            KACANG -> "Kacang"

            // Sayuran
            BAYAM -> "Bayam"
            BROKOLI -> "Brokoli"
            WORTEL -> "Wortel"
            KANGKUNG -> "Kangkung"
            SAWI -> "Sawi"

            // Buah
            APEL -> "Apel"
            PISANG -> "Pisang"
            JERUK -> "Jeruk"
            PEPAYA -> "Pepaya"
            MANGGA -> "Mangga"

            else -> "Unknown"
        }
    }

    fun getPoints(): Int {
        return when {
            isFood() -> 10 // Standard food points
            this == PELLET -> 10
            this == ENERGIZER -> 50
            this == BELL -> 200
            else -> 0
        }
    }

    companion object {
        fun fromChar(char: Char): TypeOfCollision {
            return when (char) {
                '.' -> PELLET
                'o' -> ENERGIZER
                'c' -> BELL
                '|' -> WALL

                // Legacy foods
                'n' -> RICE
                'i' -> FISH
                'v' -> VEGETABLE
                'b' -> FRUIT

                // Dynamic foods - using GameConstants mapping
                '1' -> NASI
                '2' -> UBI
                '3' -> KENTANG
                '4' -> SINGKONG
                '5' -> JAGUNG

                '6' -> IKAN
                '7' -> AYAM
                '8' -> TEMPE
                '9' -> TAHU
                'A' -> KACANG

                'B' -> BAYAM
                'C' -> BROKOLI
                'D' -> WORTEL
                'E' -> KANGKUNG
                'F' -> SAWI

                'G' -> APEL
                'H' -> PISANG
                'J' -> JERUK
                'K' -> PEPAYA
                'L' -> MANGGA

                else -> NONE
            }
        }

        fun getAllFoodTypes(): List<TypeOfCollision> {
            return values().filter { it.isFood() }
        }

        fun getFoodTypesByCategory(category: String): List<TypeOfCollision> {
            return when (category.lowercase()) {
                "karbohidrat", "carbs" -> values().filter { it.isCarbFood() }
                "protein" -> values().filter { it.isProteinFood() }
                "sayuran", "vegetables" -> values().filter { it.isVegetableFood() }
                "buah", "fruits" -> values().filter { it.isFruitFood() }
                else -> emptyList()
            }
        }
    }
}