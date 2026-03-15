package com.plantdisease.detector

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var progress = 0f
    private var text = ""

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        color = 0xFFE0E0E0.toInt()
    }

    private val fgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 18f
        color = 0xFF2E7D32.toInt()
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF1B5E20.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 52f
        isFakeBoldText = true
    }

    private val subTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF888888.toInt()
        textAlign = Paint.Align.CENTER
        textSize = 28f
    }

    fun setProgress(value: Float, label: String) {
        progress = value
        text = label
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = minOf(width, height).toFloat()
        val padding = 20f
        val rect = RectF(padding, padding, size - padding, size - padding)

        // Arka plan daire
        canvas.drawArc(rect, -90f, 360f, false, bgPaint)

        // İlerleme yayı
        val sweep = 360f * (progress / 100f)
        canvas.drawArc(rect, -90f, sweep, false, fgPaint)

        // Yüzde metni
        val cx = size / 2f
        val cy = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText("${progress.toInt()}%", cx, cy, textPaint)

        // Alt metin
        canvas.drawText("Güven", cx, cy + 40f, subTextPaint)
    }
}