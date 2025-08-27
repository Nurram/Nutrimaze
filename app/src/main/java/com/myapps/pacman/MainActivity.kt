package com.myapps.pacman
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.myapps.pacman.databinding.ActivityMainBinding
import com.myapps.pacman.game.GameConstants
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.ui.PacmanSurfaceView
import com.myapps.pacman.utils.Direction
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.myapps.pacman.ScoreActivity // Tambahkan baris ini

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: PacmanGameViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
    private var tutorialShown = false
    // ActivityResultLauncher untuk Quiz
    private val quizLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // Quiz berhasil, lanjut ke level berikutnya
            viewModel.onEvents(PacmanEvents.ContinueToNextLevel)
        } else {
            // Quiz gagal, kembali ke level sebelumnya
            viewModel.onEvents(PacmanEvents.FailedQuiz)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Setup immersive mode
        setupImmersiveMode()
        configureStartResetButton()
        configurePauseResumeButton()
        configureSoundButton()
        // Setup gesture controls - hapus tombol arah
        setupGestureControls()
        // Show tutorial on first run
        if (!tutorialShown) {
            showTutorial()
        }
        observeGameStates()
        animateGameControls()
        initializeFoodProgress()
    }
    private fun setupImmersiveMode() {
        // Enable edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Get the window insets controller
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        // Keep screen on during gameplay
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        // Set status bar and navigation bar colors to transparent
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        Log.d("MainActivity", "Immersive mode setup completed")
    }
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Re-hide system bars when window regains focus
            hideSystemBars()
        }
    }
    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
    override fun onResume() {
        super.onResume()
        // Hide system bars again when activity resumes
        hideSystemBars()
    }
    private fun initializeFoodProgress() {
        // Initialize progress bars
        updateFoodProgress(0, 0, 0, 0)
        // Target info sudah dihapus dari layout
        // updateTargetInfo() // DIHAPUS
    }
    private fun updateFoodProgress(rice: Int, fish: Int, vegetable: Int, fruit: Int) {
        // Update Rice
        binding.riceProgressBar.progress = rice
        binding.riceProgressText.text = "$rice/${GameConstants.MAX_RICE_PORTION}"
        updateProgressTextColor(binding.riceProgressText, rice, 3, GameConstants.MAX_RICE_PORTION)
        // Update Fish
        binding.fishProgressBar.progress = fish
        binding.fishProgressText.text = "$fish/${GameConstants.MAX_FISH_PORTION}"
        updateProgressTextColor(binding.fishProgressText, fish, 2, GameConstants.MAX_FISH_PORTION)
        // Update Vegetable
        binding.vegetableProgressBar.progress = vegetable
        binding.vegetableProgressText.text = "$vegetable/${GameConstants.MAX_VEGETABLE_PORTION}"
        updateProgressTextColor(binding.vegetableProgressText, vegetable, 3, GameConstants.MAX_VEGETABLE_PORTION)
        // Update Fruit
        binding.fruitProgressBar.progress = fruit
        binding.fruitProgressText.text = "$fruit/${GameConstants.MAX_FRUIT_PORTION}"
        updateProgressTextColor(binding.fruitProgressText, fruit, 2, GameConstants.MAX_FRUIT_PORTION)
        // Target info sudah dihapus dari layout
        // updateTargetInfo(rice, fish, vegetable, fruit) // DIHAPUS
    }
    private fun updateProgressTextColor(textView: TextView, current: Int, target: Int, max: Int) {
        val color = when {
            current > max -> ContextCompat.getColor(this, R.color.wrong_answer) // Red for excess
            current >= target -> ContextCompat.getColor(this, R.color.correct_answer) // Green for target met
            else -> ContextCompat.getColor(this, R.color.white) // White for not enough
        }
        textView.setTextColor(color)
    }
    // Method updateTargetInfo() DIHAPUS karena targetInfoText sudah tidak ada di layout
    /*
    private fun updateTargetInfo(rice: Int = 0, fish: Int = 0, vegetable: Int = 0, fruit: Int = 0) {
        val riceStatus = getStatusIcon(rice, 3, GameConstants.MAX_RICE_PORTION)
        val fishStatus = getStatusIcon(fish, 2, GameConstants.MAX_FISH_PORTION)
        val vegStatus = getStatusIcon(vegetable, 3, GameConstants.MAX_VEGETABLE_PORTION)
        val fruitStatus = getStatusIcon(fruit, 2, GameConstants.MAX_FRUIT_PORTION)
        val statusText = "Status: Nasi $riceStatus, Ikan $fishStatus, Sayur $vegStatus, Buah $fruitStatus"
        binding.targetInfoText.text = if (isTargetAchieved(rice, fish, vegetable, fruit)) {
            "🎉 TARGET TERCAPAI! Selesaikan level untuk lanjut!"
        } else {
            statusText
        }
        // Update background color based on overall status
        val bgColor = if (isTargetAchieved(rice, fish, vegetable, fruit)) {
            ContextCompat.getColor(this, R.color.correct_answer)
        } else {
            ContextCompat.getColor(this, R.color.premium_background)
        }
        binding.targetInfoText.setBackgroundColor(bgColor)
    }
    */
    // Method getStatusIcon() DIHAPUS karena tidak digunakan lagi
    /*
    private fun getStatusIcon(current: Int, target: Int, max: Int): String {
        return when {
            current > max -> "⚠️" // Warning for excess
            current >= target -> "✅" // Check for target met
            else -> "❌" // X for not enough
        }
    }
    */
    private fun isTargetAchieved(rice: Int, fish: Int, vegetable: Int, fruit: Int): Boolean {
        return rice >= 3 && rice <= GameConstants.MAX_RICE_PORTION &&          // 3 dari maks 4
                fish >= 2 && fish <= GameConstants.MAX_FISH_PORTION &&         // 2 dari maks 3
                vegetable >= 3 && vegetable <= GameConstants.MAX_VEGETABLE_PORTION &&  // 3 dari maks 4
                fruit >= 2 && fruit <= GameConstants.MAX_FRUIT_PORTION         // 2 dari maks 3
    }
    private fun setupGestureControls() {
        // Set up gesture listener untuk surface view dengan logging
        binding.pacmanView.setDirectionChangeListener(object : PacmanSurfaceView.DirectionChangeListener {
            override fun onDirectionChange(direction: Direction) {
                Log.d("MainActivity", "Direction changed to: $direction")
                when (direction) {
                    Direction.UP -> {
                        Log.d("MainActivity", "Sending UP direction to ViewModel")
                        viewModel.onEvents(PacmanEvents.UpDirection)
                    }
                    Direction.DOWN -> {
                        Log.d("MainActivity", "Sending DOWN direction to ViewModel")
                        viewModel.onEvents(PacmanEvents.DownDirection)
                    }
                    Direction.LEFT -> {
                        Log.d("MainActivity", "Sending LEFT direction to ViewModel")
                        viewModel.onEvents(PacmanEvents.LeftDirection)
                    }
                    Direction.RIGHT -> {
                        Log.d("MainActivity", "Sending RIGHT direction to ViewModel")
                        viewModel.onEvents(PacmanEvents.RightDirection)
                    }
                    Direction.NOWHERE -> {
                        Log.d("MainActivity", "NOWHERE direction received")
                    }
                }
            }
        })
    }
    private fun observeGameStates() {
        lifecycleScope.launch {
            viewModel.gameIsStarted.collect {
                if (it) {
                    binding.pacmanView.setActiveGameView(true)
                    binding.pacmanView.setGameBoardData(viewModel.mapBoardData)
                    binding.pacmanView.setPacmanData(viewModel.pacmanData)
                    binding.pacmanView.setBlinkyData(viewModel.blinkyData)
                    binding.pacmanView.setInkyData(viewModel.inkyData)
                    binding.pacmanView.setPinkyData(viewModel.pinkyData)
                    binding.pacmanView.setClydeData(viewModel.clydeData)
                    // PENTING: Setup level completion callback setelah game dimulai
                    viewModel.setupLevelCompletionCallback()
                    binding.startResetButton.text = getString(R.string.reset)
                } else {
                    binding.pacmanView.setActiveGameView(false)
                    binding.pacmanView.stopAllCurrentJobs()
                    binding.startResetButton.text = getString(R.string.start)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.gameIsMute.collect {
                if (it) {
                    binding.pacmanView.changeSoundGameStatus(true)
                    binding.soundButton.setIconResource(R.drawable.ic_volume_off)
                } else {
                    binding.pacmanView.changeSoundGameStatus(false)
                    binding.soundButton.setIconResource(R.drawable.ic_volume_up)
                }
            }
        }
        lifecycleScope.launch {
            viewModel.gameIsPaused.collect {
                if (it) {
                    binding.pacmanView.changePauseGameStatus(true)
                    binding.pacmanView.pauseGameDraw()
                    binding.pauseResetButton.text = getString(R.string.resume)
                } else {
                    binding.pacmanView.changePauseGameStatus(false)
                    binding.pacmanView.resumeGameDraw()
                    binding.pauseResetButton.text = getString(R.string.pause)
                }
            }
        }
        // Observer untuk quiz requirement
        lifecycleScope.launch {
            viewModel.showQuiz.collect { currentLevel ->
                if (currentLevel > 0) {
                    Log.d("MainActivity", "Starting quiz for level: $currentLevel")
                    val intent = Intent(this@MainActivity, QuizActivity::class.java)
                    intent.putExtra(QuizActivity.EXTRA_CURRENT_LEVEL, currentLevel)
                    quizLauncher.launch(intent)
                }
            }
        }
        // Observer untuk game over
        lifecycleScope.launch {
            viewModel.mapBoardData.collect { boardData ->
                if (boardData.gameStatus == GameStatus.WON || boardData.gameStatus == GameStatus.LOSE) {
                    showScoreActivity(boardData.gameStatus == GameStatus.WON)
                }
            }
        }
        // Observer untuk update progress makanan
        lifecycleScope.launch {
            viewModel.pacmanData.collect { pacmanData ->
                updateFoodProgress(
                    pacmanData.totalCarbsEaten,
                    pacmanData.totalProteinEaten,
                    pacmanData.totalVegetablesEaten,
                    pacmanData.totalFruitsEaten
                )
            }
        }
    }
    private fun showScoreActivity(gameWon: Boolean) {
        val pacmanData = viewModel.pacmanData.value
        val intent = Intent(this, ScoreActivity::class.java).apply {
            putExtra(ScoreActivity.EXTRA_FINAL_SCORE, viewModel.mapBoardData.value.score)
            putExtra(ScoreActivity.EXTRA_LEVEL_REACHED, viewModel.mapBoardData.value.currentLevel + 1)
            putExtra(ScoreActivity.EXTRA_GAME_WON, gameWon)
            putExtra(ScoreActivity.EXTRA_RICE_EATEN, pacmanData.totalCarbsEaten)
            putExtra(ScoreActivity.EXTRA_FISH_EATEN, pacmanData.totalProteinEaten)
            putExtra(ScoreActivity.EXTRA_VEGETABLE_EATEN, pacmanData.totalVegetablesEaten)
            putExtra(ScoreActivity.EXTRA_FRUIT_EATEN, pacmanData.totalFruitsEaten)

            putExtra(ScoreActivity.EXTRA_CARBS_TOTAL, pacmanData.totalCarbsEaten)
            putExtra(ScoreActivity.EXTRA_PROTEIN_TOTAL, pacmanData.totalProteinEaten)
            putExtra(ScoreActivity.EXTRA_VEGETABLES_TOTAL, pacmanData.totalVegetablesEaten)
            putExtra(ScoreActivity.EXTRA_FRUITS_TOTAL, pacmanData.totalFruitsEaten)

            putExtra(ScoreActivity.EXTRA_NASI_EATEN, pacmanData.nasiEaten)
            putExtra(ScoreActivity.EXTRA_UBI_EATEN, pacmanData.ubiEaten)
            putExtra(ScoreActivity.EXTRA_KENTANG_EATEN, pacmanData.kentangEaten)
            putExtra(ScoreActivity.EXTRA_SINGKONG_EATEN, pacmanData.singkongEaten)
            putExtra(ScoreActivity.EXTRA_JAGUNG_EATEN, pacmanData.jagungEaten)

            putExtra(ScoreActivity.EXTRA_IKAN_EATEN, pacmanData.ikanEaten)
            putExtra(ScoreActivity.EXTRA_AYAM_EATEN, pacmanData.ayamEaten)
            putExtra(ScoreActivity.EXTRA_TEMPE_EATEN, pacmanData.tempeEaten)
            putExtra(ScoreActivity.EXTRA_TAHU_EATEN, pacmanData.tahuEaten)
            putExtra(ScoreActivity.EXTRA_KACANG_EATEN, pacmanData.kacangEaten)

            putExtra(ScoreActivity.EXTRA_BAYAM_EATEN, pacmanData.bayamEaten)
            putExtra(ScoreActivity.EXTRA_BROKOLI_EATEN, pacmanData.brokoliEaten)
            putExtra(ScoreActivity.EXTRA_WORTEL_EATEN, pacmanData.wortelEaten)
            putExtra(ScoreActivity.EXTRA_KANGKUNG_EATEN, pacmanData.kangkungEaten)
            putExtra(ScoreActivity.EXTRA_SAWI_EATEN, pacmanData.sawiEaten)

            putExtra(ScoreActivity.EXTRA_APEL_EATEN, pacmanData.apelEaten)
            putExtra(ScoreActivity.EXTRA_PISANG_EATEN, pacmanData.pisangEaten)
            putExtra(ScoreActivity.EXTRA_JERUK_EATEN, pacmanData.jerukEaten)
            putExtra(ScoreActivity.EXTRA_PEPAYA_EATEN, pacmanData.pepayaEaten)
            putExtra(ScoreActivity.EXTRA_MANGGA_EATEN, pacmanData.manggaEaten)
        }
        startActivity(intent)
        finish()
    }
    private fun showTutorial() {
        binding.tutorialOverlay.visibility = View.VISIBLE
        binding.tutorialOverlay.setOnClickListener {
            binding.tutorialOverlay.visibility = View.GONE
            tutorialShown = true
        }
    }
    private fun animateGameControls() {
        binding.gameControls.alpha = 0f
        binding.gameControls.translationY = 100f
        binding.foodProgressCard.alpha = 0f
        binding.foodProgressCard.translationY = 50f
        binding.gameControls.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(500)
            .setInterpolator(OvershootInterpolator())
            .start()
        binding.foodProgressCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator())
            .setStartDelay(200)
            .start()
    }
    private fun configureStartResetButton() {
        binding.startResetButton.setOnClickListener {
            binding.startResetButton.isEnabled = false
            provideHapticFeedback()
            if (viewModel.gameIsStarted.value) {
                viewModel.onEvents(PacmanEvents.Stop)
            } else {
                viewModel.onEvents(PacmanEvents.Start)
            }
            binding.startResetButton.postDelayed({
                binding.startResetButton.isEnabled = true
            }, 1000)
        }
    }
    private fun configurePauseResumeButton() {
        binding.pauseResetButton.setOnClickListener {
            binding.pauseResetButton.isEnabled = false
            provideHapticFeedback()
            if (viewModel.gameIsPaused.value) {
                viewModel.onEvents(PacmanEvents.Resume)
            } else {
                viewModel.onEvents(PacmanEvents.Pause)
            }
            binding.pauseResetButton.postDelayed({
                binding.pauseResetButton.isEnabled = true
            }, 500)
        }
    }
    private fun configureSoundButton() {
        binding.soundButton.setOnClickListener {
            binding.soundButton.isEnabled = false
            provideHapticFeedback()
            if (viewModel.gameIsMute.value) {
                viewModel.onEvents(PacmanEvents.RecoverSounds)
            } else {
                viewModel.onEvents(PacmanEvents.MuteSounds)
            }
            binding.soundButton.postDelayed({
                binding.soundButton.isEnabled = true
            }, 500)
        }
    }
    private fun provideHapticFeedback(feedbackType: Int = HapticFeedbackConstants.VIRTUAL_KEY) {
        binding.pacmanView.performHapticFeedback(
            feedbackType
            // HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING // Dihapus karena deprecated
        )
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.onEvents(PacmanEvents.Stop)
        binding.pacmanView.stopAllCurrentJobs()
        binding.pacmanView.stopDrawJob()
    }
}
