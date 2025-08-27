package com.myapps.pacman
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.myapps.pacman.databinding.ActivityScoreBinding
import com.myapps.pacman.game.GameConstants
class ScoreActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScoreBinding
    companion object {
        const val EXTRA_FINAL_SCORE = "final_score"
        const val EXTRA_LEVEL_REACHED = "level_reached"
        const val EXTRA_GAME_WON = "game_won"
        // Legacy compatibility
        const val EXTRA_RICE_EATEN = "rice_eaten"
        const val EXTRA_FISH_EATEN = "fish_eaten"
        const val EXTRA_VEGETABLE_EATEN = "vegetable_eaten"
        const val EXTRA_FRUIT_EATEN = "fruit_eaten"
        // Dynamic food totals
        const val EXTRA_CARBS_TOTAL = "DYNAMIC_CARBS_TOTAL"
        const val EXTRA_PROTEIN_TOTAL = "DYNAMIC_PROTEIN_TOTAL"
        const val EXTRA_VEGETABLES_TOTAL = "DYNAMIC_VEGETABLES_TOTAL"
        const val EXTRA_FRUITS_TOTAL = "DYNAMIC_FRUITS_TOTAL"
        // Individual dynamic foods
        const val EXTRA_NASI_EATEN = "NASI_EATEN"
        const val EXTRA_UBI_EATEN = "UBI_EATEN"
        const val EXTRA_KENTANG_EATEN = "KENTANG_EATEN"
        const val EXTRA_SINGKONG_EATEN = "SINGKONG_EATEN"
        const val EXTRA_JAGUNG_EATEN = "JAGUNG_EATEN"
        const val EXTRA_IKAN_EATEN = "IKAN_EATEN"
        const val EXTRA_AYAM_EATEN = "AYAM_EATEN"
        const val EXTRA_TEMPE_EATEN = "TEMPE_EATEN"
        const val EXTRA_TAHU_EATEN = "TAHU_EATEN"
        const val EXTRA_KACANG_EATEN = "KACANG_EATEN"
        const val EXTRA_BAYAM_EATEN = "BAYAM_EATEN"
        const val EXTRA_BROKOLI_EATEN = "BROKOLI_EATEN"
        const val EXTRA_WORTEL_EATEN = "WORTEL_EATEN"
        const val EXTRA_KANGKUNG_EATEN = "KANGKUNG_EATEN"
        const val EXTRA_SAWI_EATEN = "SAWI_EATEN"
        const val EXTRA_APEL_EATEN = "APEL_EATEN"
        const val EXTRA_PISANG_EATEN = "PISANG_EATEN"
        const val EXTRA_JERUK_EATEN = "JERUK_EATEN"
        const val EXTRA_PEPAYA_EATEN = "PEPAYA_EATEN"
        const val EXTRA_MANGGA_EATEN = "MANGGA_EATEN"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupDynamicScoreDisplay()
        setupButtons()
    }
    private fun setupDynamicScoreDisplay() {
        val finalScore = intent.getIntExtra(EXTRA_FINAL_SCORE, 0)
        val levelReached = intent.getIntExtra(EXTRA_LEVEL_REACHED, 1)
        val gameWon = intent.getBooleanExtra(EXTRA_GAME_WON, false)
        // Get dynamic food data
        val dynamicData = getDynamicFoodData()
        // Set title berdasarkan hasil game
        binding.gameResultTitle.text = if (gameWon) {
            getString(R.string.selamat_menang)
        } else {
            getString(R.string.game_selesai)
        }
        // Set final score
        binding.finalScore.text = getString(R.string.skor_akhir, finalScore)
        // Set level reached
        binding.levelReached.text = getString(R.string.level_tercapai, levelReached)
        // Set dynamic food consumption stats
        setupDynamicFoodStats(dynamicData)
        // Set rating berdasarkan skor
        val rating = calculateDynamicRating(finalScore, levelReached, gameWon, dynamicData)
        binding.gameRating.text = rating
        // Set pesan evaluasi dinamis
        val evaluation = getDynamicEvaluationMessage(dynamicData, gameWon)
        binding.evaluationMessage.text = evaluation
    }
    private fun getDynamicFoodData(): DynamicFoodData {
        return DynamicFoodData(
            // Totals
            carbsTotal = intent.getIntExtra(EXTRA_CARBS_TOTAL, 0),
            proteinTotal = intent.getIntExtra(EXTRA_PROTEIN_TOTAL, 0),
            vegetablesTotal = intent.getIntExtra(EXTRA_VEGETABLES_TOTAL, 0),
            fruitsTotal = intent.getIntExtra(EXTRA_FRUITS_TOTAL, 0),
            // Legacy compatibility
            riceEaten = intent.getIntExtra(EXTRA_RICE_EATEN, 0),
            fishEaten = intent.getIntExtra(EXTRA_FISH_EATEN, 0),
            vegetableEaten = intent.getIntExtra(EXTRA_VEGETABLE_EATEN, 0),
            fruitEaten = intent.getIntExtra(EXTRA_FRUIT_EATEN, 0),
            // Karbohidrat
            nasiEaten = intent.getIntExtra(EXTRA_NASI_EATEN, 0),
            ubiEaten = intent.getIntExtra(EXTRA_UBI_EATEN, 0),
            kentangEaten = intent.getIntExtra(EXTRA_KENTANG_EATEN, 0),
            singkongEaten = intent.getIntExtra(EXTRA_SINGKONG_EATEN, 0),
            jagungEaten = intent.getIntExtra(EXTRA_JAGUNG_EATEN, 0),
            // Protein
            ikanEaten = intent.getIntExtra(EXTRA_IKAN_EATEN, 0),
            ayamEaten = intent.getIntExtra(EXTRA_AYAM_EATEN, 0),
            tempeEaten = intent.getIntExtra(EXTRA_TEMPE_EATEN, 0),
            tahuEaten = intent.getIntExtra(EXTRA_TAHU_EATEN, 0),
            kacangEaten = intent.getIntExtra(EXTRA_KACANG_EATEN, 0),
            // Sayuran
            bayamEaten = intent.getIntExtra(EXTRA_BAYAM_EATEN, 0),
            brokoliEaten = intent.getIntExtra(EXTRA_BROKOLI_EATEN, 0),
            wortelEaten = intent.getIntExtra(EXTRA_WORTEL_EATEN, 0),
            kangkungEaten = intent.getIntExtra(EXTRA_KANGKUNG_EATEN, 0),
            sawiEaten = intent.getIntExtra(EXTRA_SAWI_EATEN, 0),
            // Buah
            apelEaten = intent.getIntExtra(EXTRA_APEL_EATEN, 0),
            pisangEaten = intent.getIntExtra(EXTRA_PISANG_EATEN, 0),
            jerukEaten = intent.getIntExtra(EXTRA_JERUK_EATEN, 0),
            pepayaEaten = intent.getIntExtra(EXTRA_PEPAYA_EATEN, 0),
            manggaEaten = intent.getIntExtra(EXTRA_MANGGA_EATEN, 0)
        )
    }
    private fun setupDynamicFoodStats(data: DynamicFoodData) {
        val carbsToShow = data.carbsTotal
        val proteinToShow = data.proteinTotal
        val vegetablesToShow = data.vegetablesTotal
        val fruitsToShow = data.fruitsTotal

        binding.riceConsumed.text = getString(R.string.makanan_pokok_dimakan, carbsToShow, GameConstants.MAX_RICE_PORTION)
        binding.fishConsumed.text = getString(R.string.lauk_pauk_dimakan, proteinToShow, GameConstants.MAX_FISH_PORTION)
        binding.vegetableConsumed.text = getString(R.string.sayur_dimakan, vegetablesToShow, GameConstants.MAX_VEGETABLE_PORTION)
        binding.fruitConsumed.text = getString(R.string.buah_dimakan, fruitsToShow, GameConstants.MAX_FRUIT_PORTION)

        if (data.carbsTotal > 0) {
            addDetailedFoodBreakdown(data)
        }
    }
    private fun addDetailedFoodBreakdown(data: DynamicFoodData) {
        val breakdown = StringBuilder()
        // Karbohidrat breakdown
        if (data.carbsTotal > 0) {
            breakdown.append("📋 DETAIL KONSUMSI:\n\n")
            breakdown.append("🍚 MAKANAN POKOK:\n")
            if (data.nasiEaten > 0) breakdown.append("  • Nasi: ${data.nasiEaten}\n")
            if (data.ubiEaten > 0) breakdown.append("  • Ubi: ${data.ubiEaten}\n")
            if (data.kentangEaten > 0) breakdown.append("  • Kentang: ${data.kentangEaten}\n")
            if (data.singkongEaten > 0) breakdown.append("  • Singkong: ${data.singkongEaten}\n")
            if (data.jagungEaten > 0) breakdown.append("  • Jagung: ${data.jagungEaten}\n")
            breakdown.append("  Total: ${data.carbsTotal}/${GameConstants.MAX_RICE_PORTION}\n\n")
        }
        // Protein breakdown
        if (data.proteinTotal > 0) {
            breakdown.append("🥩 LAUK PAUK:\n")
            if (data.ikanEaten > 0) breakdown.append("  • Ikan: ${data.ikanEaten}\n")
            if (data.ayamEaten > 0) breakdown.append("  • Ayam: ${data.ayamEaten}\n")
            if (data.tempeEaten > 0) breakdown.append("  • Tempe: ${data.tempeEaten}\n")
            if (data.tahuEaten > 0) breakdown.append("  • Tahu: ${data.tahuEaten}\n")
            if (data.kacangEaten > 0) breakdown.append("  • Kacang: ${data.kacangEaten}\n")
            breakdown.append("  Total: ${data.proteinTotal}/${GameConstants.MAX_FISH_PORTION}\n\n")
        }
        // Sayuran breakdown
        if (data.vegetablesTotal > 0) {
            breakdown.append("🥬 SAYURAN:\n")
            if (data.bayamEaten > 0) breakdown.append("  • Bayam: ${data.bayamEaten}\n")
            if (data.brokoliEaten > 0) breakdown.append("  • Brokoli: ${data.brokoliEaten}\n")
            if (data.wortelEaten > 0) breakdown.append("  • Wortel: ${data.wortelEaten}\n")
            if (data.kangkungEaten > 0) breakdown.append("  • Kangkung: ${data.kangkungEaten}\n")
            if (data.sawiEaten > 0) breakdown.append("  • Sawi: ${data.sawiEaten}\n")
            breakdown.append("  Total: ${data.vegetablesTotal}/${GameConstants.MAX_VEGETABLE_PORTION}\n\n")
        }
        // Buah breakdown
        if (data.fruitsTotal > 0) {
            breakdown.append("🍎 BUAH-BUAHAN:\n")
            if (data.apelEaten > 0) breakdown.append("  • Apel: ${data.apelEaten}\n")
            if (data.pisangEaten > 0) breakdown.append("  • Pisang: ${data.pisangEaten}\n")
            if (data.jerukEaten > 0) breakdown.append("  • Jeruk: ${data.jerukEaten}\n")
            if (data.pepayaEaten > 0) breakdown.append ("  • Pepaya: ${data.pepayaEaten}\n")
            if (data.manggaEaten > 0) breakdown.append("  • Mangga: ${data.manggaEaten}\n")
            breakdown.append("  Total: ${data.fruitsTotal}/${GameConstants.MAX_FRUIT_PORTION}\n")
        }
        // Add breakdown to evaluation message
        val currentEval = binding.evaluationMessage.text.toString()
        binding.evaluationMessage.text = "$currentEval\n\n$breakdown"
    }
    private fun calculateDynamicRating(score: Int, level: Int, won: Boolean, data: DynamicFoodData): String {
        val hasExcess = hasExcessConsumption(data)
        val hasGoodBalance = hasGoodFoodBalance(data)

        return when {
            won && score >= 2000 && !hasExcess && hasGoodBalance -> "⭐⭐⭐ Sempurna! Gizi Seimbang!"
            won && score >= 1500 && !hasExcess -> "⭐⭐ Bagus! Konsumsi Terkontrol!"
            won && score >= 1000 -> "⭐ Cukup Baik!"
            level >= 4 && !hasExcess -> "👍 Hampir Berhasil! Pola Makan Baik!"
            level >= 3 -> "💪 Terus Berlatih!"
            else -> "🎯 Ayo Coba Lagi!"
        }
    }
    private fun getDynamicEvaluationMessage(data: DynamicFoodData, won: Boolean): String {
        val excessFoods = getExcessFoods(data)
        val foodVariety = getFoodVarietyScore(data)
        val targetMet = data.carbsTotal >= 3 && data.proteinTotal >= 2 &&
                data.vegetablesTotal >= 3 && data.fruitsTotal >= 2

        return when {
            won && excessFoods.isEmpty() && foodVariety >= 10 && targetMet ->
                "🏆 LUAR BIASA! Kamu berhasil menerapkan prinsip Isi Piringku dengan sempurna! Variasi makanan sangat baik dan porsi seimbang!"

            won && excessFoods.isEmpty() && targetMet ->
                "HEBAT\uD83D\uDC4F Kamu menang dengan pola makan seimbang sesuai dengan Isi Piringku! Porsi semua kategori makanan sudah tepat : Makanan pokok (4), Lauk pauk (4), Sayuran (4) dan Buah-buahan (3)"

            won && excessFoods.isNotEmpty() ->
                "🎊 Selamat menang! Tapi perhatikan konsumsi berlebihan: ${excessFoods.joinToString(", ")}. Ingat batasan Isi Piringku ya!"

            excessFoods.isNotEmpty() ->
                "\uD83D\uDD04COBA LAGI!  Hindari konsumsi makanan berlebih dan ikuti panduan Isi Piringku untuk hidup sehat: Makanan pokok (4), Lauk pauk (4), Sayuran (4), dan Buah-buahan (3)"

            !targetMet ->
                "🎯 Coba lagi! Konsumsi makanan sesuai target: Karbohidrat (3), Protein (2), Sayuran (3), Buah (2)"

            foodVariety < 4 ->
                "🌈 Coba lagi! Variasikan makanan dari semua kategori. Isi Piringku menganjurkan keanekaragaman nutrisi!"

            else ->
                "💪 Coba lagi! Hindari junk food dan konsumsi makanan sehat sesuai panduan Isi Piringku!"
        }
    }
    private fun hasExcessConsumption(data: DynamicFoodData): Boolean {
        return data.carbsTotal > GameConstants.MAX_RICE_PORTION ||
                data.proteinTotal > GameConstants.MAX_FISH_PORTION ||
                data.vegetablesTotal > GameConstants.MAX_VEGETABLE_PORTION ||
                data.fruitsTotal > GameConstants.MAX_FRUIT_PORTION
    }
    private fun getExcessFoods(data: DynamicFoodData): List<String> {
        val excess = mutableListOf<String>()
        if (data.carbsTotal > GameConstants.MAX_RICE_PORTION) excess.add("Makanan Pokok")
        if (data.proteinTotal > GameConstants.MAX_FISH_PORTION) excess.add("Lauk Pauk")
        if (data.vegetablesTotal > GameConstants.MAX_VEGETABLE_PORTION) excess.add("Sayuran")
        if (data.fruitsTotal > GameConstants.MAX_FRUIT_PORTION) excess.add("Buah-buahan")
        return excess
    }
    private fun hasGoodFoodBalance(data: DynamicFoodData): Boolean {
        return data.carbsTotal >= 3 && data.proteinTotal >= 2 &&
                data.vegetablesTotal >= 3 && data.fruitsTotal >= 2
    }
    private fun getFoodVarietyScore(data: DynamicFoodData): Int {
        var variety = 0
        // Count different types of foods consumed
        if (data.nasiEaten > 0) variety++
        if (data.ubiEaten > 0) variety++
        if (data.kentangEaten > 0) variety++
        if (data.singkongEaten > 0) variety++
        if (data.jagungEaten > 0) variety++
        if (data.ikanEaten > 0) variety++
        if (data.ayamEaten > 0) variety++
        if (data.tempeEaten > 0) variety++
        if (data.tahuEaten > 0) variety++
        if (data.kacangEaten > 0) variety++
        if (data.bayamEaten > 0) variety++
        if (data.brokoliEaten > 0) variety++
        if (data.wortelEaten > 0) variety++
        if (data.kangkungEaten > 0) variety++
        if (data.sawiEaten > 0) variety++
        if (data.apelEaten > 0) variety++
        if (data.pisangEaten > 0) variety++
        if (data.jerukEaten > 0) variety++
        if (data.pepayaEaten > 0) variety++
        if (data.manggaEaten > 0) variety++
        return variety
    }
    private fun setupButtons() {
        binding.playAgainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        binding.backToMenuButton.setOnClickListener {
            val intent = Intent(this, IsiPiringkuActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
    // Data class untuk dynamic food data
    data class DynamicFoodData(
        // Totals
        val carbsTotal: Int,
        val proteinTotal: Int,
        val vegetablesTotal: Int,
        val fruitsTotal: Int,
        // Legacy
        val riceEaten: Int,
        val fishEaten: Int,
        val vegetableEaten: Int,
        val fruitEaten: Int,
        // Individual foods
        val nasiEaten: Int,
        val ubiEaten: Int,
        val kentangEaten: Int,
        val singkongEaten: Int,
        val jagungEaten: Int,
        val ikanEaten: Int,
        val ayamEaten: Int,
        val tempeEaten: Int,
        val tahuEaten: Int,
        val kacangEaten: Int,
        val bayamEaten: Int,
        val brokoliEaten: Int,
        val wortelEaten: Int,
        val kangkungEaten: Int,
        val sawiEaten: Int,
        val apelEaten: Int,
        val pisangEaten: Int,
        val jerukEaten: Int,
        val pepayaEaten: Int,
        val manggaEaten: Int
    )
}
