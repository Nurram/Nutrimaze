package com.myapps.pacman.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class GameMainView(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {


    private val backScreenPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint().apply {
        color = Color.YELLOW
        textSize = 100f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backScreenPaint)
        canvas.drawText("Pac-Man", width / 2f, 200f, titlePaint)
        canvas.drawText("Press Start To Play", width / 2f, height / 2f + 25f, textPaint)
    }
}
