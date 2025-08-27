package com.myapps.pacman.ui

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.GestureDetector.SimpleOnGestureListener
import com.myapps.pacman.R
import com.myapps.pacman.board.BoardController
import com.myapps.pacman.game.GameConstants
import com.myapps.pacman.states.BoardData
import com.myapps.pacman.states.GameStatus
import com.myapps.pacman.states.GhostData
import com.myapps.pacman.states.GhostsIdentifiers
import com.myapps.pacman.states.PacmanData
import com.myapps.pacman.utils.Direction
import com.myapps.pacman.utils.convertPositionToPair
import com.myapps.pacman.utils.matrix.Matrix
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.currentCoroutineContext
import kotlin.math.abs

class PacmanSurfaceView(
    context: Context, attrs: AttributeSet? = null
) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    // Interface untuk callback perubahan arah
    interface DirectionChangeListener {
        fun onDirectionChange(direction: Direction)
    }

    // Variabel untuk menyimpan listener
    private var directionChangeListener: DirectionChangeListener? = null

    // Metode untuk mengatur listener
    fun setDirectionChangeListener(listener: DirectionChangeListener) {
        this.directionChangeListener = listener
        Log.d("PacmanSurfaceView", "Direction change listener set")
    }

    // Detector gestur untuk menangani swipe
    private lateinit var gestureDetector: GestureDetector

    // Threshold untuk swipe - dibuat lebih sensitif
    private val SWIPE_THRESHOLD = 30  // Dikurangi untuk lebih sensitif
    private val SWIPE_VELOCITY_THRESHOLD = 30  // Dikurangi untuk lebih sensitif

    // Implementasi gesture listener yang diperbaiki dengan logging detail
    private val gestureListener = object : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            Log.d("PacmanSurfaceView", "onDown called at (${e.x}, ${e.y})")
            return true  // PENTING: Return true untuk memproses gesture selanjutnya
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            Log.d("PacmanSurfaceView", "onScroll called - distanceX: $distanceX, distanceY: $distanceY")
            return false  // Biarkan onFling yang menangani
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.d("PacmanSurfaceView", "onFling called")
            if (e1 == null || e2 == null) {
                Log.d("PacmanSurfaceView", "onFling - null motion events")
                return false
            }

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            Log.d("PacmanSurfaceView", "onFling - diffX: $diffX, diffY: $diffY")
            Log.d("PacmanSurfaceView", "onFling - velocityX: $velocityX, velocityY: $velocityY")

            // Cek apakah game sedang berjalan dan tidak di-pause
            if (!isPlaying || isGamePaused) {
                Log.d("PacmanSurfaceView", "Game not active, ignoring gesture")
                return false
            }

            // Tentukan apakah swipe horizontal atau vertikal
            if (abs(diffX) > abs(diffY)) {
                // Swipe horizontal
                Log.d("PacmanSurfaceView", "Horizontal swipe detected")
                if (abs(diffX) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        Log.d("PacmanSurfaceView", "RIGHT swipe detected")
                        notifyDirectionChange(Direction.RIGHT)
                    } else {
                        Log.d("PacmanSurfaceView", "LEFT swipe detected")
                        notifyDirectionChange(Direction.LEFT)
                    }
                    return true
                } else {
                    Log.d("PacmanSurfaceView", "Horizontal swipe too weak - diffX: ${abs(diffX)}, velocityX: ${abs(velocityX)}")
                    Log.d("PacmanSurfaceView", "Required - threshold: $SWIPE_THRESHOLD, velocity threshold: $SWIPE_VELOCITY_THRESHOLD")
                }
            } else {
                // Swipe vertikal
                Log.d("PacmanSurfaceView", "Vertical swipe detected")
                if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        Log.d("PacmanSurfaceView", "DOWN swipe detected")
                        notifyDirectionChange(Direction.DOWN)
                    } else {
                        Log.d("PacmanSurfaceView", "UP swipe detected")
                        notifyDirectionChange(Direction.UP)
                    }
                    return true
                } else {
                    Log.d("PacmanSurfaceView", "Vertical swipe too weak - diffY: ${abs(diffY)}, velocityY: ${abs(velocityY)}")
                    Log.d("PacmanSurfaceView", "Required - threshold: $SWIPE_THRESHOLD, velocity threshold: $SWIPE_VELOCITY_THRESHOLD")
                }
            }
            return false
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            Log.d("PacmanSurfaceView", "onSingleTapUp called at (${e.x}, ${e.y})")
            return super.onSingleTapUp(e)
        }
    }

    private var drawingJob: Job? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main.immediate)
    private var isPlaying = false

    // ===========================================
    // BITMAP RESOURCES - JUNK FOOD
    // ===========================================
    //HERE No 3
    private lateinit var burgerBitmap: Bitmap
    private lateinit var sodaBitmap: Bitmap
    private lateinit var donatBitmap: Bitmap
    private lateinit var chipsBitmap: Bitmap
    private lateinit var frightenedBitmap: Bitmap

    // ===========================================
    // BITMAP RESOURCES - LEGACY HEALTHY FOOD
    // ===========================================
    private lateinit var riceBitmap: Bitmap
    private lateinit var fishBitmap: Bitmap
    private lateinit var vegetableBitmap: Bitmap
    private lateinit var fruitBitmap: Bitmap

    // ===========================================
    // DYNAMIC FOOD BITMAP RESOURCES - KARBOHIDRAT
    // ===========================================
    private lateinit var nasiBitmap: Bitmap
    private lateinit var ubiBitmap: Bitmap
    private lateinit var kentangBitmap: Bitmap
    private lateinit var singkongBitmap: Bitmap
    private lateinit var jagungBitmap: Bitmap

    // ===========================================
    // DYNAMIC FOOD BITMAP RESOURCES - PROTEIN
    // ===========================================
    private lateinit var ikanBitmap: Bitmap
    private lateinit var ayamBitmap: Bitmap
    private lateinit var tempeBitmap: Bitmap
    private lateinit var tahuBitmap: Bitmap
    private lateinit var kacangBitmap: Bitmap

    // ===========================================
    // DYNAMIC FOOD BITMAP RESOURCES - SAYURAN
    // ===========================================
    private lateinit var bayamBitmap: Bitmap
    private lateinit var brokoliBitmap: Bitmap
    private lateinit var wortelBitmap: Bitmap
    private lateinit var kangkungBitmap: Bitmap
    private lateinit var sawiBitmap: Bitmap

    // ===========================================
    // DYNAMIC FOOD BITMAP RESOURCES - BUAH
    // ===========================================
    private lateinit var apelBitmap: Bitmap
    private lateinit var pisangBitmap: Bitmap
    private lateinit var jerukBitmap: Bitmap
    private lateinit var pepayaBitmap: Bitmap
    private lateinit var manggaBitmap: Bitmap

    //game view elements painters
    private val pacmanPaint = createPaint(color = Color.YELLOW)
    private val scorerPaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 26f, textAlign = Paint.Align.LEFT)
    private val blinkyPaint = createPaint(color = Color.RED)
    private val pinkyPaint = createPaint(color = resources.getColor(R.color.pink, resources.newTheme()))
    private val clydePaint = createPaint(color = resources.getColor(R.color.orange, resources.newTheme()))
    private val inkyPaint = createPaint(color = resources.getColor(R.color.lightBlue, resources.newTheme()))
    private val eyeWhitePaint = createPaint(color = Color.WHITE)
    private val eyeBluePaint = createPaint(color = Color.BLUE)
    private val emptySpace = createPaint(color = Color.BLACK)
    private val frightenedGhostPaint = createPaint(color = resources.getColor(R.color.darkBlue, resources.newTheme()))

    // Ukuran wall diperkecil, empty space diperbesar
    private var wallPaint = createPaint(color = Color.BLUE, strokeWidth = 2f)
    private var food = createPaint(color = Color.WHITE, strokeWidth = 4f)
    private var energizer = createPaint(color = Color.WHITE, strokeWidth = 25f)
    private var doorPaint = createPaint(color = Color.WHITE)
    private val bellPaint = createPaint(color = Color.YELLOW, isAntiAlias = true)
    private val soundPaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 26f, textAlign = Paint.Align.LEFT)
    private val pauseScreenPaint = createPaint(color = resources.getColor(R.color.transparentBlack, resources.newTheme()))
    private val pauseMessagePaint = createPaint(color = Color.WHITE, strokeWidth = 20f, textSize = 30f)
    private val endGameScreenPaint = createPaint(color = resources.getColor(R.color.littleTransparentBlack, resources.newTheme()))
    private val endGameMessageScreenPaint = createPaint(color = Color.YELLOW, strokeWidth = 25f, textSize = 40f)
    private val tittleMainScreen = createPaint(Color.YELLOW, textSize = 100f, textAlign = Paint.Align.CENTER, isAntiAlias = true)
    private val textMainScreenPaint = createPaint(Color.WHITE, textSize = 50f, textAlign = Paint.Align.CENTER, isAntiAlias = true)
    private val healthInfoPaint = createPaint(color = Color.GREEN, strokeWidth = 20f, textSize = 26f, textAlign = Paint.Align.LEFT)
    private val warningPaint = createPaint(color = Color.RED, strokeWidth = 20f, textSize = 26f, textAlign = Paint.Align.LEFT)

    //game variables
    private var mapMatrix = Matrix<Char>(0, 0)
    private var scorer = 0
    private var pacmanLives = 0
    private var pacmanHealth = 3.0f
    private var currentLevel = 0

    // Food tracking variables
    private var riceEaten = 0
    private var fishEaten = 0
    private var vegetableEaten = 0
    private var fruitEaten = 0

    //pacman variables
    private var pacmanPosition = Pair(-1f, -1f)
    private var pacmanEnergizerState = false
    private var pacmanDirection = Direction.RIGHT
    private var mouthOpen = false

    //blinky variables
    private var blinkyPosition = Pair(-1f, -1f)
    private var blinkyDirection = Direction.NOWHERE
    private var blinkyIsAlive = true

    //pinky variables
    private var pinkyPosition = Pair(-1f, -1f)
    private var pinkyDirection = Direction.RIGHT
    private var pinkyIsAlive = true

    //inky variables
    private var inkyPosition = Pair(-1f, -1f)
    private var inkyDirection = Direction.RIGHT
    private var inkyIsAlive = true

    // clyde variables
    private var clydePosition = Pair(-1f, -1f)
    private var clydeDirection = Direction.RIGHT
    private var clydeIsAlive = true

    private var isGamePaused = false
    private var gameState = GameStatus.ONGOING
    private var soundIsMuted = false

    private var handler: Handler = Handler(Looper.getMainLooper())
    private var collectMapDataJob: Job? = null
    private var collectPacmanDataJob: Job? = null
    private var collectPinkyDataJob: Job? = null
    private var collectBlinkyDataJob: Job? = null
    private var collectInkyDataJob: Job? = null
    private var collectClydeDataJob: Job? = null

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if(!isGamePaused){
                mouthOpen = !mouthOpen
            }
            invalidate()
            handler.postDelayed(this, 200)
        }
    }

    init {
        Log.d("PacmanSurfaceView", "Initializing PacmanSurfaceView")

        handler.post(runnable)
        holder.addCallback(this)

        // Inisialisasi gesture detector dengan listener yang benar
        gestureDetector = GestureDetector(context, gestureListener)
        Log.d("PacmanSurfaceView", "GestureDetector initialized")

        // Set view properties untuk memastikan menerima touch events
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true

        Log.d("PacmanSurfaceView", "View focus and click properties set")

        // Load all food bitmaps (dynamic system)
        loadAllFoodBitmaps()
    }

    // ===========================================
    // MULTI-INSTANCE DYNAMIC FOOD BITMAP LOADING
    // ===========================================
    private fun loadAllFoodBitmaps() {
        try {
            // ===========================================
            // LOAD JUNK FOOD BITMAPS (EXISTING)
            // ===========================================
            burgerBitmap = BitmapFactory.decodeResource(resources, R.drawable.burger)
            sodaBitmap = BitmapFactory.decodeResource(resources, R.drawable.soda)
            donatBitmap = BitmapFactory.decodeResource(resources, R.drawable.donat)
            chipsBitmap = BitmapFactory.decodeResource(resources, R.drawable.chips)

            // ===========================================
            // LOAD LEGACY HEALTHY FOOD BITMAPS (EXISTING)
            // ===========================================
            riceBitmap = BitmapFactory.decodeResource(resources, R.drawable.nasi)
            fishBitmap = BitmapFactory.decodeResource(resources, R.drawable.ikan)
            vegetableBitmap = BitmapFactory.decodeResource(resources, R.drawable.bayam) // Default to bayam
            fruitBitmap = BitmapFactory.decodeResource(resources, R.drawable.apel) // Default to apel

            // ===========================================
            // DYNAMIC FOOD BITMAPS - KARBOHIDRAT
            // ===========================================
            nasiBitmap = BitmapFactory.decodeResource(resources, R.drawable.nasi)
            ubiBitmap = BitmapFactory.decodeResource(resources, R.drawable.ubi)
            kentangBitmap = BitmapFactory.decodeResource(resources, R.drawable.kentang)
            singkongBitmap = BitmapFactory.decodeResource(resources, R.drawable.singkong)
            jagungBitmap = BitmapFactory.decodeResource(resources, R.drawable.jagung)

            // ===========================================
            // DYNAMIC FOOD BITMAPS - PROTEIN
            // ===========================================
            ikanBitmap = BitmapFactory.decodeResource(resources, R.drawable.ikan)
            ayamBitmap = BitmapFactory.decodeResource(resources, R.drawable.ayam)
            tempeBitmap = BitmapFactory.decodeResource(resources, R.drawable.tempe)
            tahuBitmap = BitmapFactory.decodeResource(resources, R.drawable.tahu)
            kacangBitmap = BitmapFactory.decodeResource(resources, R.drawable.kacang)

            // ===========================================
            // DYNAMIC FOOD BITMAPS - SAYURAN
            // ===========================================
            bayamBitmap = BitmapFactory.decodeResource(resources, R.drawable.bayam)
            brokoliBitmap = BitmapFactory.decodeResource(resources, R.drawable.brokoli)
            wortelBitmap = BitmapFactory.decodeResource(resources, R.drawable.wortel)
            kangkungBitmap = BitmapFactory.decodeResource(resources, R.drawable.kangkung)
            sawiBitmap = BitmapFactory.decodeResource(resources, R.drawable.sawi)

            // ===========================================
            // DYNAMIC FOOD BITMAPS - BUAH
            // ===========================================
            apelBitmap = BitmapFactory.decodeResource(resources, R.drawable.apel)
            pisangBitmap = BitmapFactory.decodeResource(resources, R.drawable.pisang)
            jerukBitmap = BitmapFactory.decodeResource(resources, R.drawable.jeruk)
            pepayaBitmap = BitmapFactory.decodeResource(resources, R.drawable.pepaya)
            manggaBitmap = BitmapFactory.decodeResource(resources, R.drawable.mangga)

            frightenedBitmap = burgerBitmap
            Log.d("PacmanSurfaceView", "All multi-instance dynamic food bitmaps loaded successfully")
        } catch (e: Exception) {
            Log.e("PacmanSurfaceView", "Error loading multi-instance dynamic food bitmaps", e)
        }
    }

    // ===========================================
    // FIXED MULTI-INSTANCE DYNAMIC FOOD BITMAP METHODS
    // ===========================================

    // Get bitmap berdasarkan character dengan support untuk multi-instance
    private fun getMultiInstanceFoodBitmap(foodChar: Char?): Bitmap? {
        if (foodChar == null) return null
        return when (foodChar) {
            // ===========================================
            // KARBOHIDRAT BITMAPS - MULTI-INSTANCE
            // ===========================================
            GameConstants.NASI_CHAR -> nasiBitmap
            GameConstants.UBI_CHAR -> ubiBitmap
            GameConstants.KENTANG_CHAR -> kentangBitmap
            GameConstants.SINGKONG_CHAR -> singkongBitmap
            GameConstants.JAGUNG_CHAR -> jagungBitmap

            // ===========================================
            // PROTEIN BITMAPS - MULTI-INSTANCE
            // ===========================================
            GameConstants.IKAN_CHAR -> ikanBitmap
            GameConstants.AYAM_CHAR -> ayamBitmap
            GameConstants.TEMPE_CHAR -> tempeBitmap
            GameConstants.TAHU_CHAR -> tahuBitmap
            GameConstants.KACANG_CHAR -> kacangBitmap

            // ===========================================
            // SAYURAN BITMAPS - MULTI-INSTANCE
            // ===========================================
            GameConstants.BAYAM_CHAR -> bayamBitmap
            GameConstants.BROKOLI_CHAR -> brokoliBitmap
            GameConstants.WORTEL_CHAR -> wortelBitmap
            GameConstants.KANGKUNG_CHAR -> kangkungBitmap
            GameConstants.SAWI_CHAR -> sawiBitmap

            // ===========================================
            // BUAH BITMAPS - MULTI-INSTANCE
            // ===========================================
            GameConstants.APEL_CHAR -> apelBitmap
            GameConstants.PISANG_CHAR -> pisangBitmap
            GameConstants.JERUK_CHAR -> jerukBitmap
            GameConstants.PEPAYA_CHAR -> pepayaBitmap
            GameConstants.MANGGA_CHAR -> manggaBitmap

            // Legacy foods (backwards compatibility)
            GameConstants.RICE_CHAR -> riceBitmap
            GameConstants.FISH_CHAR -> fishBitmap
            GameConstants.VEGETABLE_CHAR -> vegetableBitmap
            GameConstants.FRUIT_CHAR -> fruitBitmap

            else -> null
        }
    }

    // Check apakah character adalah multi-instance dynamic food
    private fun isMultiInstanceDynamicFood(char: Char?): Boolean {
        if (char == null) return false
        return GameConstants.isCarbFood(char) ||
                GameConstants.isProteinFood(char) ||
                GameConstants.isVegetableFood(char) ||
                GameConstants.isFruitFood(char)
    }

    // ===========================================
    // ENHANCED FOOD DRAWING WITH LEVEL-BASED SCALING
    // ===========================================

    // Metode untuk menggambar makanan dinamis multi-instance berdasarkan character
    private fun drawMultiInstanceDynamicFood(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        foodChar: Char
    ) {
        val bitmap = getMultiInstanceFoodBitmap(foodChar)
        if (bitmap != null) {
            // Scale factor berdasarkan level untuk variasi visual
            val scaleFactor = when (currentLevel) {
                0 -> 1.1f  // Level 1 - sedikit lebih kecil
                1 -> 1.2f  // Level 2 - normal
                2 -> 1.3f  // Level 3 - sedikit lebih besar
                3 -> 1.4f  // Level 4 - lebih besar
                4 -> 1.5f  // Level 5 - paling besar
                else -> 1.2f
            }

            val adjustedSize = size * scaleFactor
            val left = cx - adjustedSize / 2
            val top = cy - adjustedSize / 2
            val right = cx + adjustedSize / 2
            val bottom = cy + adjustedSize / 2

            val destRect = RectF(left, top, right, bottom)

            // Add a subtle shadow effect for multi-instance foods
            val shadowPaint = Paint().apply {
                alpha = 50
                color = Color.BLACK
            }
            val shadowRect = RectF(left + 2, top + 2, right + 2, bottom + 2)
            canvas.drawOval(shadowRect, shadowPaint)

            // Draw category indicator ring for dynamic foods
            if (GameConstants.isDynamicFood(foodChar)) {
                drawCategoryIndicator(canvas, cx, cy, adjustedSize, foodChar)
            }

            // Draw the main food bitmap
            canvas.drawBitmap(bitmap, null, destRect, null)

            Log.v("PacmanSurfaceView", "Drew ${GameConstants.getFoodName(foodChar)} at ($cx, $cy) with scale $scaleFactor")
        } else {
            Log.w("PacmanSurfaceView", "No bitmap found for food char: $foodChar")
        }
    }


    // Metode untuk memberitahu listener tentang perubahan arah dengan logging
    private fun notifyDirectionChange(direction: Direction) {
        Log.d("PacmanSurfaceView", "notifyDirectionChange called with direction: $direction")
        Log.d("PacmanSurfaceView", "Listener is null: ${directionChangeListener == null}")
        directionChangeListener?.onDirectionChange(direction)
        Log.d("PacmanSurfaceView", "Direction change notification sent")
    }

    // Override untuk menangani sentuhan dengan logging detail
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val actionString = when (event.action) {
            MotionEvent.ACTION_DOWN -> "ACTION_DOWN"
            MotionEvent.ACTION_MOVE -> "ACTION_MOVE"
            MotionEvent.ACTION_UP -> "ACTION_UP"
            MotionEvent.ACTION_CANCEL -> "ACTION_CANCEL"
            else -> "ACTION_OTHER(${event.action})"
        }

        Log.d("PacmanSurfaceView", "Touch event: $actionString at (${event.x}, ${event.y})")

        val gestureResult = gestureDetector.onTouchEvent(event)
        Log.d("PacmanSurfaceView", "Gesture detector result: $gestureResult")

        // Return true jika gesture detector menangani atau fallback ke super
        return gestureResult || super.onTouchEvent(event)
    }

    private fun startDrawing() {
        drawingJob = coroutineScope.launch(Dispatchers.Default) {
            while (currentCoroutineContext().isActive) {
                val canvas: Canvas? = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        synchronized(holder) {
                            if(isPlaying){
                                drawGameElements(canvas)
                            }
                            else{
                                drawMultiInstanceStartScreen(canvas, width = width.toFloat(),height.toFloat())
                            }
                        }
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }
                delay(16)
            }
        }
    }

    fun setGameBoardData(gameBoardData: StateFlow<BoardData>) {
        collectMapDataJob?.cancel()
        collectMapDataJob = coroutineScope.launch {
            gameBoardData.collect {
                mapMatrix = it.gameBoardData
                scorer = it.score
                pacmanLives = it.pacmanLives
                gameState = it.gameStatus
                currentLevel = it.currentLevel // Track current level for display
                wallPaint.color = when (it.currentLevel) {
                    0 -> Color.BLUE
                    1 -> resources.getColor(R.color.levelOne, resources.newTheme())
                    2 -> resources.getColor(R.color.levelTwo, resources.newTheme())
                    3 -> resources.getColor(R.color.levelThree, resources.newTheme())
                    4 -> resources.getColor(R.color.levelFour, resources.newTheme())
                    else -> Color.BLUE
                }
            }
        }
    }

    fun setPacmanData(pacmanData: StateFlow<PacmanData>) {
        collectPacmanDataJob?.cancel()
        collectPacmanDataJob = coroutineScope.launch {
            pacmanData.collect {
                pacmanEnergizerState = it.energizerStatus
                pacmanDirection = it.pacmanDirection
                pacmanHealth = it.health
                riceEaten = it.riceEaten
                fishEaten = it.fishEaten
                vegetableEaten = it.vegetableEaten
                fruitEaten = it.fruitEaten

                val pairPosition = convertPositionToPair(it.pacmanPosition)
                if (shouldAnimate(pacmanPosition, pairPosition)) {
                    animateActorTo(
                        pacmanPosition,
                        pairPosition,
                        pacmanDirection,
                        it.speedDelay
                    ) { pair ->
                        pacmanPosition = pair
                    }
                } else {
                    pacmanPosition = pairPosition
                }
            }
        }
    }

    fun setBlinkyData(blinkyData: StateFlow<GhostData>) {
        collectBlinkyDataJob?.cancel()
        collectBlinkyDataJob = coroutineScope.launch {
            blinkyData.collect {
                blinkyDirection = it.ghostDirection
                blinkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(blinkyPosition, pairPosition)) {
                    animateActorTo(
                        blinkyPosition,
                        pairPosition,
                        blinkyDirection,
                        it.ghostDelay
                    ) { pair ->
                        blinkyPosition = pair
                    }
                } else {
                    blinkyPosition = pairPosition
                }
            }
        }
    }

    fun setInkyData(inkyData: StateFlow<GhostData>) {
        collectInkyDataJob?.cancel()
        collectInkyDataJob = coroutineScope.launch {
            inkyData.collect {
                inkyDirection = it.ghostDirection
                inkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(inkyPosition, pairPosition)) {
                    animateActorTo(
                        inkyPosition,
                        pairPosition,
                        inkyDirection,
                        it.ghostDelay
                    ) { pair ->
                        inkyPosition = pair
                    }
                } else {
                    inkyPosition = pairPosition
                }
            }
        }
    }

    fun setPinkyData(pinkyData: StateFlow<GhostData>) {
        collectPinkyDataJob?.cancel()
        collectPinkyDataJob = coroutineScope.launch {
            pinkyData.collect {
                pinkyDirection = it.ghostDirection
                pinkyIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(pinkyPosition, pairPosition)) {
                    animateActorTo(
                        startPosition = pinkyPosition,
                        targetPosition = pairPosition,
                        direction = pinkyDirection,
                        speedDelay = it.ghostDelay,
                        updatePosition = { pair ->
                            pinkyPosition = pair
                        }
                    )
                } else {
                    pinkyPosition = pairPosition
                }
            }
        }
    }

    fun setClydeData(clydeData: StateFlow<GhostData>) {
        collectClydeDataJob?.cancel()
        collectClydeDataJob = coroutineScope.launch {
            clydeData.collect {
                clydeDirection = it.ghostDirection
                clydeIsAlive = it.ghostLifeStatement
                val pairPosition = convertPositionToPair(it.ghostPosition)
                if (shouldAnimate(clydePosition, pairPosition)) {
                    animateActorTo(
                        startPosition = clydePosition,
                        targetPosition = pairPosition,
                        direction = clydeDirection,
                        speedDelay = it.ghostDelay
                    ) { pair ->
                        clydePosition = pair
                    }
                } else {
                    clydePosition = pairPosition
                }
            }
        }
    }

    fun stopAllCurrentJobs() {
        collectMapDataJob?.let { if(it.isActive) it.cancel()}
        collectPacmanDataJob?.let { if(it.isActive) it.cancel()}
        collectBlinkyDataJob?.let { if(it.isActive) it.cancel()}
        collectInkyDataJob?.let { if(it.isActive) it.cancel()}
        collectClydeDataJob?.let { if(it.isActive) it.cancel()}
        collectPinkyDataJob?.let { if(it.isActive) it.cancel()}
    }

    fun stopDrawJob(){
        drawingJob?.let { if(it.isActive) it.cancel()}
    }

    private fun animateActorTo(
        startPosition: Pair<Float, Float>,
        targetPosition: Pair<Float, Float>,
        direction: Direction,
        speedDelay: Long,
        updatePosition: (Pair<Float, Float>) -> Unit,
    ) {
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = (speedDelay.times(0.9)).toLong()

            addUpdateListener { animation ->
                val progress = animation.animatedValue as Float
                val newPosition =
                    linearInterpolation(
                        startPosition,
                        targetPosition,
                        progress,
                        direction
                    )
                updatePosition(newPosition)
            }

            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    updatePosition(targetPosition)
                }

                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }

        animator.start()
    }

    private fun linearInterpolation(
        startPosition: Pair<Float, Float>,
        endPosition: Pair<Float, Float>,
        step: Float,
        direction: Direction
    ): Pair<Float, Float> =
        when (direction) {
            Direction.RIGHT -> {
                Pair(
                    endPosition.first,
                    startPosition.second + step * (endPosition.second - startPosition.second)
                )
            }

            Direction.LEFT -> {
                Pair(
                    endPosition.first,
                    startPosition.second + step * (endPosition.second - startPosition.second)
                )
            }

            Direction.UP -> {
                Pair(
                    startPosition.first + step * (endPosition.first - startPosition.first),
                    endPosition.second
                )
            }

            Direction.DOWN -> {
                Pair(
                    startPosition.first + step * (endPosition.first - startPosition.first),
                    endPosition.second
                )
            }

            Direction.NOWHERE -> {
                Pair(endPosition.first, endPosition.second)
            }
        }

    private fun shouldAnimate(
        startPosition: Pair<Float, Float>,
        endPosition: Pair<Float, Float>
    ): Boolean {
        if (startPosition == endPosition) return false
        if (abs(startPosition.first - endPosition.first) > 2f) return false
        if (abs(startPosition.second - endPosition.second) > 2f) return false
        return true
    }

    fun setActiveGameView(isPlayingGame:Boolean){
        isPlaying = isPlayingGame
        Log.d("PacmanSurfaceView", "Game active state changed to: $isPlayingGame")
    }

    fun changePauseGameStatus(pauseGame: Boolean) {
        isGamePaused = pauseGame
        Log.d("PacmanSurfaceView", "Game pause state changed to: $pauseGame")
    }

    fun changeSoundGameStatus(soundActivate: Boolean) {
        soundIsMuted = soundActivate
    }

    private fun createPaint(
        color: Int = Color.BLACK,
        style: Paint.Style = Paint.Style.FILL,
        strokeWidth: Float = 0f,
        textSize: Float = 0f,
        isAntiAlias: Boolean = false,
        textAlign: Paint.Align = Paint.Align.CENTER
    ): Paint =
        Paint().apply {
            this.color = color
            this.style = style
            this.strokeWidth = strokeWidth
            this.textSize = textSize
            this.isAntiAlias = isAntiAlias
            this.textAlign = textAlign
        }

    fun pauseGameDraw() {
        isGamePaused = true
    }

    fun resumeGameDraw() {
        isGamePaused = false
    }

    // Metode untuk menggambar junk food (ghosts)
    private fun drawJunkFood(
        canvas: Canvas,
        cx: Float,
        cy: Float,
        size: Float,
        ghostType: String,
        isEnergized: Boolean,
        isAlive: Boolean,
        direction: Direction
    ) {
        if (!isAlive) return

        val bitmap = when {
            isEnergized -> frightenedBitmap
            else -> when (ghostType) {
                GhostsIdentifiers.BLINKY -> burgerBitmap
                GhostsIdentifiers.PINKY -> sodaBitmap
                GhostsIdentifiers.INKY -> donatBitmap
                GhostsIdentifiers.CLYDE -> chipsBitmap
                else -> burgerBitmap
            }
        }

        val scaleFactor = 1.4f
        val adjustedSize = size * scaleFactor
        val left = cx - adjustedSize / 2
        val top = cy - adjustedSize / 2
        val right = cx + adjustedSize / 2
        val bottom = cy + adjustedSize / 2

        val destRect = RectF(left, top, right, bottom)
        canvas.drawBitmap(bitmap, null, destRect, null)
    }

    // ===========================================
    // ENHANCED DRAWGAMEELEMENTS WITH IMPROVED DYNAMIC FOOD SUPPORT
    // ===========================================
    private fun drawGameElements(canvas: Canvas) {
        val cellSize: Float = width.toFloat() / 28

        canvas.drawRect(0f,0f,width.toFloat(),height.toFloat(),emptySpace)

        // draw map dengan ukuran wall yang diperkecil dan empty space yang diperbesar
        for (i in 0 until mapMatrix.getRows()) {
            for (j in 0 until mapMatrix.getColumns()) {
                val left = (j * cellSize)
                val top = (i * cellSize)
                val right = left + cellSize
                val bottom = top + cellSize

                when (mapMatrix.getElementByPosition(i, j)) {
                    BoardController.EMPTY_SPACE,BoardController.BLANK_SPACE -> canvas.drawRect(
                        left,
                        top,
                        right,
                        bottom,
                        emptySpace
                    )

                    // Wall (kotak biru) diperkecil dengan margin yang lebih besar
                    BoardController.WALL_CHAR -> canvas.drawRect(
                        left + 6,    // Margin diperbesar dari 3 ke 6
                        top + 6,     // Margin diperbesar dari 3 ke 6
                        right - 6,   // Margin diperbesar dari 3 ke 6
                        bottom - 6,  // Margin diperbesar dari 3 ke 6
                        wallPaint
                    )

                    BoardController.PELLET_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawPoint((left + right) / 2, (top + bottom) / 2, food)
                    }

                    BoardController.ENERGIZER_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        canvas.drawCircle(
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize / 3,
                            energizer
                        )
                    }

                    BoardController.BELL_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, emptySpace)
                        drawBell(
                            canvas,
                            (left + right) / 2,
                            (top + bottom) / 2,
                            cellSize * 0.7f,
                            cellSize * 0.9f
                        )
                    }

                    BoardController.GHOST_DOOR_CHAR -> {
                        canvas.drawRect(left, top, right, bottom, doorPaint)
                    }

                    // ===========================================
                    // MULTI-INSTANCE DYNAMIC FOOD DRAWING - HANDLE ALL FOOD TYPES
                    // ===========================================
                    else -> {
                        val cell = mapMatrix.getElementByPosition(i, j)
                        if (cell != null && isMultiInstanceDynamicFood(cell)) {
                            // Background kotak hitam diperbesar
                            canvas.drawRect(left - 2, top - 2, right + 2, bottom + 2, emptySpace)

                            // Draw dynamic food dengan enhanced rendering
                            drawMultiInstanceDynamicFood(
                                canvas,
                                (left + right) / 2,
                                (top + bottom) / 2,
                                cellSize * 1.2f, // Ukuran makanan diperbesar
                                cell
                            )

                            // Add level indicator for dynamic foods
                            if (GameConstants.isDynamicFood(cell)) {
                                drawLevelIndicator(canvas, left, top, cellSize, currentLevel)
                            }
                        }
                    }
                }
            }
        }

        // Menggambar area bawah (kotak hitam diperbesar)
        for (j in 0 until mapMatrix.getColumns()) {
            val left = (j * cellSize)
            val top = (mapMatrix.getRows() * cellSize)
            val right = left + cellSize
            val bottom = top + cellSize
            canvas.drawRect(left - 2, top - 2, right + 2, bottom + 2, emptySpace) // Diperbesar
        }

        // draw sound Icon dengan bahasa Indonesia - pindah ke pojok kiri
        val leftSpeaker = cellSize
        val topSpeaker = (mapMatrix.getRows() + 1) * cellSize

        if (soundIsMuted) {
            canvas.drawText("SUARA MATI", leftSpeaker, topSpeaker, soundPaint)
        } else {
            canvas.drawText("SUARA HIDUP", leftSpeaker, topSpeaker, soundPaint)
        }

        canvas.drawText("Skor: $scorer", cellSize, cellSize, scorerPaint)

        // Display current level info with food count - ENHANCED
        val levelInfo = when (currentLevel) {
            0 -> "Level 1 - Target: 4 Jenis (Nasi, Ikan, Bayam, Apel)"
            1 -> "Level 2 - Target: 8 Jenis (+ Ubi, Ayam, Brokoli, Pisang)"
            2 -> "Level 3 - Target: 12 Jenis (+ Kentang, Tempe, Wortel, Jeruk)"
            3 -> "Level 4 - Target: 16 Jenis (+ Singkong, Tahu, Kangkung, Pepaya)"
            4 -> "Level 5 - Target: 20 Jenis (+ Jagung, Kacang, Sawi, Mangga)"
            else -> "Level ${currentLevel + 1} - ${GameConstants.getMaxInstancesForLevel(currentLevel)} Makanan"
        }
        canvas.drawText(levelInfo, cellSize, cellSize + 30, healthInfoPaint)

        // Display food variety progress
        val varietyInfo = "Variasi: ${getCurrentFoodVariety()}/${GameConstants.getMaxInstancesForLevel(currentLevel)} jenis"
        canvas.drawText(varietyInfo, cellSize, cellSize + 60, healthInfoPaint)

        // draw pacman lives (kotak hitam diperbesar)
        val left1 = (1 * cellSize)
        val top1 = (35 * cellSize)
        val bottom1 = top1 + cellSize

        for (i in 1..pacmanLives) {
            val leftLive = left1 + (i - 1) * (cellSize + 10)
            val rightLive = leftLive + cellSize
            // Background diperbesar
            canvas.drawRect(leftLive - 2, top1 - 2, rightLive + 2, bottom1 + 2, emptySpace)
            canvas.drawArc(leftLive, top1, rightLive, bottom1, 225f, 270f, true, pacmanPaint)
        }

        // Draw Pacman dengan background kotak hitam diperbesar
        val pacmanLeft = (pacmanPosition.second * cellSize)
        val pacmanTop = (pacmanPosition.first * cellSize)
        val pacmanRight = pacmanLeft + cellSize
        val pacmanBottom = pacmanTop + cellSize

        // Background kotak hitam diperbesar
        canvas.drawRect(pacmanLeft - 2, pacmanTop - 2, pacmanRight + 2, pacmanBottom + 2, emptySpace)
        drawPacman(canvas, pacmanLeft, pacmanTop, pacmanRight, pacmanBottom)

        // Draw ghosts sebagai junk food dengan background kotak hitam diperbesar
        val blinkyLeft = (blinkyPosition.second * cellSize)
        val blinkyTop = (blinkyPosition.first * cellSize)
        val blikyRight = blinkyLeft + cellSize
        val blinkyBottom = blinkyTop + cellSize

        canvas.drawRect(blinkyLeft - 2, blinkyTop - 2, blikyRight + 2, blinkyBottom + 2, emptySpace)
        //HERE No 3
        drawJunkFood(
            canvas,
            (blikyRight + blinkyLeft) / 2,
            (blinkyTop + blinkyBottom) / 2,
            cellSize * 1.2f, // Diperbesar
            GhostsIdentifiers.BLINKY,
            pacmanEnergizerState,
            blinkyIsAlive,
            blinkyDirection
        )

        val inkyLeft = (inkyPosition.second * cellSize)
        val inkyTop = (inkyPosition.first * cellSize)
        val inkyRight = inkyLeft + cellSize
        val inkyBottom = inkyTop + cellSize

        canvas.drawRect(inkyLeft - 2, inkyTop - 2, inkyRight + 2, inkyBottom + 2, emptySpace)
        drawJunkFood(
            canvas,
            (inkyRight + inkyLeft) / 2,
            (inkyTop + inkyBottom) / 2,
            cellSize * 1.2f, // Diperbesar
            GhostsIdentifiers.INKY,
            pacmanEnergizerState,
            inkyIsAlive,
            inkyDirection
        )

        if(currentLevel >= 1) {
            val pinkyLeft = (pinkyPosition.second * cellSize)
            val pinkyTop = (pinkyPosition.first * cellSize)
            val pinkyRight = pinkyLeft + cellSize
            val pinkyBottom = pinkyTop + cellSize

            canvas.drawRect(pinkyLeft - 2, pinkyTop - 2, pinkyRight + 2, pinkyBottom + 2, emptySpace)
            drawJunkFood(
                canvas,
                (pinkyRight + pinkyLeft) / 2,
                (pinkyTop + pinkyBottom) / 2,
                cellSize * 1.2f, // Diperbesar
                GhostsIdentifiers.PINKY,
                pacmanEnergizerState,
                pinkyIsAlive,
                pinkyDirection
            )
        }

        if(currentLevel >= 2) {
            val clydeLeft = (clydePosition.second * cellSize)
            val clydeTop = (clydePosition.first * cellSize)
            val clydeRight = clydeLeft + cellSize
            val clydeBottom = clydeTop + cellSize

            canvas.drawRect(clydeLeft - 2, clydeTop - 2, clydeRight + 2, clydeBottom + 2, emptySpace)
            drawJunkFood(
                canvas,
                (clydeRight + clydeLeft) / 2,
                (clydeTop + clydeBottom) / 2,
                cellSize * 1.2f, // Diperbesar
                GhostsIdentifiers.CLYDE,
                pacmanEnergizerState,
                clydeIsAlive,
                clydeDirection
            )
        }

        if (isGamePaused) {
            drawPauseScreen(canvas, width.toFloat(), height.toFloat())
        }
        if (gameState == GameStatus.LOSE) {
            drawLoseScreen(canvas, width.toFloat(), height.toFloat())
        }
        if (gameState == GameStatus.WON) {
            drawWinScreen(canvas, width.toFloat(), height.toFloat())
        }
    }

    private fun drawPacman(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float
    ) {
        canvas.drawRect(left, top, right, bottom, emptySpace)
        if (mouthOpen) {
            when (pacmanDirection) {
                Direction.RIGHT -> {
                    canvas.drawArc(left, top, right, bottom, 45f, 270f, true, pacmanPaint)
                }

                Direction.LEFT -> {
                    canvas.drawArc(left, top, right, bottom, 225f, 270f, true, pacmanPaint)
                }

                Direction.UP -> {
                    canvas.drawArc(left, top, right, bottom, 315f, 270f, true, pacmanPaint)
                }

                Direction.DOWN -> {
                    canvas.drawArc(left, top, right, bottom, 135f, 270f, true, pacmanPaint)
                }

                Direction.NOWHERE -> {}
            }
        } else {
            canvas.drawArc(
                left, top, right, bottom, 0f, 360f, true, pacmanPaint
            )
        }
    }

    private fun drawPauseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, pauseScreenPaint)
        val xPos = width / 2
        val yPos = (height / 2 - (pauseMessagePaint.descent() + pauseMessagePaint.ascent()) / 2)
        canvas.drawText("JEDA", xPos, yPos, pauseMessagePaint)
    }

    private fun drawWinScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, endGameScreenPaint)
        val xPos = width / 2
        val yPos =
            (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("KAMU MENANG", xPos, yPos, endGameMessageScreenPaint)
    }

    private fun drawLoseScreen(
        canvas: Canvas,
        width: Float,
        height: Float
    ) {
        canvas.drawRect(0f, 0f, width, height, endGameScreenPaint)
        val xPos = width / 2
        val yPos =
            (height / 2 - (endGameMessageScreenPaint.descent() + endGameMessageScreenPaint.ascent()) / 2)
        canvas.drawText("KAMU KALAH", xPos, yPos, endGameMessageScreenPaint)
    }

    private fun drawBell(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        bellWidth: Float,
        bellHeight: Float
    ) {
        val top = centerY - bellHeight / 2
        val bottom = centerY + bellHeight / 2

        val path = Path().apply {
            moveTo(centerX - bellWidth / 2, top + bellHeight / 4)
            quadTo(centerX, top - bellHeight / 4, centerX + bellWidth / 2, top + bellHeight / 4)
            lineTo(centerX + bellWidth / 2, bottom - bellHeight / 6)
            quadTo(
                centerX,
                bottom + bellHeight / 8,
                centerX - bellWidth / 2,
                bottom - bellHeight / 6
            )
            close()
        }

        canvas.drawPath(path, bellPaint)
        canvas.drawCircle(centerX, bottom - bellHeight / 8, bellHeight / 8, bellPaint)
    }

    // ===========================================
    // CATEGORY INDICATOR FOR DYNAMIC FOODS
    // ===========================================

    private fun drawCategoryIndicator(canvas: Canvas, cx: Float, cy: Float, size: Float, foodChar: Char) {
        val indicatorPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            alpha = 180
        }

        // Set color based on food category
        indicatorPaint.color = when {
            GameConstants.isCarbFood(foodChar) -> Color.YELLOW       // Karbohidrat = Yellow
            GameConstants.isProteinFood(foodChar) -> Color.RED       // Protein = Red
            GameConstants.isVegetableFood(foodChar) -> Color.GREEN  // Sayuran = Green
            GameConstants.isFruitFood(foodChar) -> Color.MAGENTA    // Buah = Magenta
            else -> Color.WHITE
        }

        // Draw subtle ring indicator
        val ringRadius = size / 2 + 5f
        canvas.drawCircle(cx, cy, ringRadius, indicatorPaint)
    }

    // ===========================================
    // LEVEL INDICATOR FOR DYNAMIC SYSTEM
    // ===========================================

    private fun drawLevelIndicator(canvas: Canvas, left: Float, top: Float, cellSize: Float, level: Int) {
        val indicatorPaint = Paint().apply {
            color = Color.WHITE
            textSize = cellSize * 0.15f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            alpha = 150
        }

        // Draw small level number in corner
        canvas.drawText(
            "L${level + 1}",
            left + cellSize * 0.2f,
            top + cellSize * 0.25f,
            indicatorPaint
        )
    }

    // ===========================================
    // FOOD VARIETY TRACKING FOR DISPLAY
    // ===========================================

    private fun getCurrentFoodVariety(): Int {
        // This should be called from game data, but for display purposes:
        var variety = 0
        if (riceEaten > 0) variety++
        if (fishEaten > 0) variety++
        if (vegetableEaten > 0) variety++
        if (fruitEaten > 0) variety++

        // Note: For full dynamic tracking, this should get data from PacmanData
        // variety += pacmanData.getFoodVarietyScore()

        return variety
    }


    // ===========================================
    // ENHANCED START SCREEN WITH DYNAMIC SYSTEM INFO
    // ===========================================
    private fun drawMultiInstanceStartScreen(canvas: Canvas, width: Float, height: Float) {
        canvas.drawRect(0f, 0f, width, height, emptySpace)
        canvas.drawText("Health Pac-Man", width / 2f, 200f, tittleMainScreen)
        canvas.drawText("Gesek layar untuk bergerak", width / 2f, height / 2f + 25f, textMainScreenPaint)

        // Enhanced info tentang sistem makanan multi-instance dinamis
//        val infoY = height / 2f + 100f
//        canvas.drawText("Sistem Multi-Instance Dinamis:", width / 2f, infoY, healthInfoPaint)
//        canvas.drawText("✨ Level 1: 4 makanan → Level 5: 20 makanan! ✨", width / 2f, infoY + 40f, healthInfoPaint)
//        canvas.drawText("🔄 Same-type respawn untuk semua makanan", width / 2f, infoY + 80f, healthInfoPaint)
//        canvas.drawText("🌈 Beragam jenis makanan spawn bersamaan", width / 2f, infoY + 120f, healthInfoPaint)
//        canvas.drawText("⚖️ Jaga keseimbangan gizi sesuai Isi Piringku", width / 2f, infoY + 160f, healthInfoPaint)
//        canvas.drawText("🚫 Hindari Makanan Tidak Sehat (Junk Food)!", width / 2f, infoY + 200f, warningPaint)
//
//        // Add level progression info
//        canvas.drawText("Progresi Level:", width / 2f, infoY + 260f, healthInfoPaint)
//        canvas.drawText("L1: Nasi+Ikan+Bayam+Apel", width / 2f, infoY + 290f, textMainScreenPaint)
//        canvas.drawText("L2: +Ubi+Ayam+Brokoli+Pisang", width / 2f, infoY + 320f, textMainScreenPaint)
//        canvas.drawText("L3: +Kentang+Tempe+Wortel+Jeruk", width / 2f, infoY + 350f, textMainScreenPaint)
//        canvas.drawText("L4: +Singkong+Tahu+Kangkung+Pepaya", width / 2f, infoY + 380f, textMainScreenPaint)
//        canvas.drawText("L5: +Jagung+Kacang+Sawi+Mangga", width / 2f, infoY + 410f, textMainScreenPaint)
    }

    // ===========================================
    // TAMBAHAN UTILITY METHODS
    // ===========================================

    // Method untuk check apakah food character adalah dynamic (bukan legacy)
    private fun isDynamicFoodChar(char: Char): Boolean {
        return char in listOf(
            GameConstants.NASI_CHAR, GameConstants.UBI_CHAR, GameConstants.KENTANG_CHAR,
            GameConstants.SINGKONG_CHAR, GameConstants.JAGUNG_CHAR,
            GameConstants.IKAN_CHAR, GameConstants.AYAM_CHAR, GameConstants.TEMPE_CHAR,
            GameConstants.TAHU_CHAR, GameConstants.KACANG_CHAR,
            GameConstants.BAYAM_CHAR, GameConstants.BROKOLI_CHAR, GameConstants.WORTEL_CHAR,
            GameConstants.KANGKUNG_CHAR, GameConstants.SAWI_CHAR,
            GameConstants.APEL_CHAR, GameConstants.PISANG_CHAR, GameConstants.JERUK_CHAR,
            GameConstants.PEPAYA_CHAR, GameConstants.MANGGA_CHAR
        )
    }

    // Method untuk mendapatkan nama makanan untuk display
    private fun getFoodDisplayName(char: Char): String {
        return GameConstants.getFoodName(char)
    }

    // Method untuk mendapatkan kategori makanan untuk display
    private fun getFoodCategory(char: Char): String {
        return GameConstants.getFoodCategory(char)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d("PacmanSurfaceView", "Surface created")
        startDrawing()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d("PacmanSurfaceView", "Surface changed - width: $width, height: $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d("PacmanSurfaceView", "Surface destroyed")
        stopDrawJob()
    }
}