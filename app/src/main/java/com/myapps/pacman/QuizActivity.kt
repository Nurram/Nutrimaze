package com.myapps.pacman

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.myapps.pacman.databinding.ActivityQuizBinding

class QuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuizBinding
    private var currentLevel = 1
    private var currentQuestionIndex = 0
    private var correctAnswers = 0
    private var questionsPerLevel = 3

    companion object {
        const val EXTRA_CURRENT_LEVEL = "current_level"
        const val RESULT_QUIZ_PASSED = "quiz_passed"

        // Bank soal sesuai materi yang diberikan
        private val QUESTIONS = mapOf(
            1 to listOf(
                Question(
                    "Dalam sehari, berapa porsi kita harus mengonsumsi lauk pauk (sumber protein)?",
                    listOf(
                        "3 porsi",
                        "1 porsi",
                        "5 porsi"
                    ),
                    0 // b. Makanan pokok, lauk pauk, sayur, dan buah
                ),
                Question(
                    "Dari kelompok makanan berikut, mana yang termasuk sumber karbohidrat?",
                    listOf(
                        "Roti, telur, singkong",
                        "Nasi, ubi jalar, kentang",
                        "Jagung, daging, tempe"
                    ),
                    1 // b. Nasi, ubi, kentang
                ),
                Question(
                    "Dibawah ini yang termasuk ke dalam junk food adalah...",
                    listOf(
                        "Ayam, burger, kentang",
                        "Donat, ikan, tempe",
                        "Keripik olahan, donat, minuman manis"
                    ),
                    2 // b
                ),
                Question(
                    "Dalam sehari, berapa porsi kita harus mengonsumsi buah-buahan?",
                    listOf(
                        "4 porsi",
                        "3 porsi",
                        "1 porsi"
                    ),
                    0 // b
                ),
            ),
            2 to listOf(
                Question(
                    "Di bawah ini yang termasuk sumber protein hewani adalah…",
                    listOf(
                        "Ikan dan ayam",
                        "Ikan dan tahu",
                        "Ayam dan tempe"
                    ),
                    0 // c.
                ),
                Question(
                    "Dalam sehari, berapa porsi kita harus mengonsumsi sayuran?",
                    listOf(
                        "2 porsi",
                        "3 porsi",
                        "5 porsi"
                    ),
                    1 // a.
                ),
                Question(
                    "Membatasi konsumsi junk food merupakan salah satu upaya untuk mengurangi asupan...",
                    listOf(
                        "Vitamin dan minera",
                        "Karbohidrat dan protein",
                        "Gula, garam, dan lemak"
                    ),
                    2 // b.
                ),
            ),
            3 to listOf(
                Question(
                    "Dalam sehari, berapa porsi kita harus mengonsumsi makanan pokok (sumber karbohidrat)?",
                    listOf(
                        "1 ½  – 3 ½ porsi",
                        "2 ½  – 4 ½  porsi",
                        "4 ½  – 6 ½ porsi"
                    ),
                    2 // b.
                ),
                Question(
                    "Di bawah ini, yang termasuk ke dalam sumber protein nabati adalah...",
                    listOf(
                        "Ikan",
                        "Tempe",
                        "Ayam"
                    ),
                    1 // c.
                ),
                Question(
                    "Tumpeng Gizi Seimbang terdiri dari 4 pilar, yaitu …",
                    listOf(
                        "Pola makan teratur, faktor ekonomi, tingkat pendidikan, dan ketersediaan pangan di lingkungan sekitar",
                        "Konsumsi beranekaragam pangan, kebersihan diri, aktivitas fisik, dan pemantauan berat badan",
                        "Konsumsi beranekaragam pangan, pola tidur teratur, aktivitas fisik, dan pemeriksaan kesehatan rutin setiap bulan"
                    ),
                    1 // a.
                ),
            ),
//            4 to listOf(
//                Question(
//                    "Dalam sehari, sebaiknya maksimal berapa kali mengonsumsi sayur?",
//                    listOf(
//                        "3 kali sehari",
//                        "4 kali sehari",
//                        "5 kali sehari"
//                    ),
//                    1 // b. 4 kali sehari (3-4 porsi)
//                ),
//                Question(
//                    "Dalam sehari, sebaiknya maksimal berapa kali mengonsumsi buah?",
//                    listOf(
//                        "1 kali sehari",
//                        "2 kali sehari",
//                        "3 kali sehari"
//                    ),
//                    2 // c. 3 kali sehari (2-3 porsi)
//                )
//            ),
//            5 to listOf(
//                Question(
//                    "Dibawah ini yang termasuk ke dalam junkfood adalah…",
//                    listOf(
//                        "Telur",
//                        "Burger",
//                        "Tempe"
//                    ),
//                    1 // b. Burger
//                ),
//                Question(
//                    "Dibawah ini yang tidak termasuk ke dalam junkfood adalah …",
//                    listOf(
//                        "Telur",
//                        "Gorengan",
//                        "Burger"
//                    ),
//                    0 // a. Telur
//                )
//            )
        )
    }

    data class Question(
        val question: String,
        val options: List<String>,
        val correctAnswer: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLevel = intent.getIntExtra(EXTRA_CURRENT_LEVEL, 1)

        setupUI()
        showQuestion()
    }

    private fun setupUI() {
        questionsPerLevel = if(currentLevel == 1) {
            4
        } else {
            3
        }
        Log.d("TAG", "Level $currentLevel, Questions per level: $questionsPerLevel")

        binding.levelTitle.text = "Kuis Level $currentLevel"
        binding.questionProgress.text = "Pertanyaan ${currentQuestionIndex + 1} dari $questionsPerLevel"

        binding.option1.setOnClickListener { checkAnswer(0) }
        binding.option2.setOnClickListener { checkAnswer(1) }
        binding.option3.setOnClickListener { checkAnswer(2) }

        // Hide option4 since we only have 3 options now
        binding.option4.visibility = android.view.View.GONE
    }

    private fun showQuestion() {
        val questions = QUESTIONS[currentLevel] ?: return
        if (currentQuestionIndex >= questions.size) return

        val question = questions[currentQuestionIndex]

        binding.questionText.text = question.question
        binding.option1.text = "a. ${question.options[0]}"
        binding.option2.text = "b. ${question.options[1]}"
        binding.option3.text = "c. ${question.options[2]}"

        binding.questionProgress.text = "Pertanyaan ${currentQuestionIndex + 1} dari $questionsPerLevel"

        // Reset button colors and enable all buttons
        resetButtonColors()
        enableAllButtons()
    }

    private fun checkAnswer(selectedAnswer: Int) {
        val questions = QUESTIONS[currentLevel] ?: return
        val question = questions[currentQuestionIndex]
        val isCorrect = selectedAnswer == question.correctAnswer

        if (isCorrect) {
            correctAnswers++
            highlightCorrectAnswer(selectedAnswer)
        } else {
            highlightWrongAnswer(selectedAnswer)
            highlightCorrectAnswer(question.correctAnswer)
        }

        // Disable all buttons
        disableAllButtons()

        // Show next question or finish quiz after delay
        binding.root.postDelayed({
            currentQuestionIndex++

            questionsPerLevel = if(currentLevel == 1) {
                4
            } else {
                3
            }
            Log.d("TAG", "Level $currentLevel, Questions per level: $questionsPerLevel")
            if (currentQuestionIndex < questionsPerLevel) {
                showQuestion()
            } else {
                finishQuiz()
            }
        }, 2000)
    }

    private fun highlightCorrectAnswer(correctIndex: Int) {
        val button = when (correctIndex) {
            0 -> binding.option1
            1 -> binding.option2
            2 -> binding.option3
            else -> return
        }
        button.setBackgroundColor(resources.getColor(R.color.correct_answer, theme))
    }

    private fun highlightWrongAnswer(wrongIndex: Int) {
        val button = when (wrongIndex) {
            0 -> binding.option1
            1 -> binding.option2
            2 -> binding.option3
            else -> return
        }
        button.setBackgroundColor(resources.getColor(R.color.wrong_answer, theme))
    }

    private fun resetButtonColors() {
        val buttons = listOf(binding.option1, binding.option2, binding.option3)
        buttons.forEach { button ->
            button.setBackgroundColor(resources.getColor(R.color.premium_gold, theme))
        }
    }

    private fun disableAllButtons() {
        binding.option1.isEnabled = false
        binding.option2.isEnabled = false
        binding.option3.isEnabled = false
    }

    private fun enableAllButtons() {
        binding.option1.isEnabled = true
        binding.option2.isEnabled = true
        binding.option3.isEnabled = true
    }

    private fun finishQuiz() {
        // Pemain harus menjawab semua soal dengan benar untuk lulus
        val passed = correctAnswers >= questionsPerLevel

        val intent = Intent().apply {
            putExtra(RESULT_QUIZ_PASSED, passed)
        }

        setResult(if (passed) Activity.RESULT_OK else Activity.RESULT_CANCELED, intent)
        finish()
    }
}