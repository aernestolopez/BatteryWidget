package com.example.batterywidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class ArcBatteryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var batteryPercent = 0

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        color = 0x33FFFFFF
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 14f
        strokeCap = Paint.Cap.ROUND
        color = 0xFFFFFFFF.toInt()
    }

    private val rect = RectF()

    fun setBattery(percent: Int) {
        batteryPercent = percent.coerceIn(0, 100)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height)
        val padding = 16f

        rect.set(
            padding,
            padding,
            size - padding,
            size - padding
        )

        // Arco base (fondo)
        canvas.drawArc(
            rect,
            135f,
            270f,
            false,
            backgroundPaint
        )

        // Arco progreso
        val sweepAngle = 270f * batteryPercent / 100f
        canvas.drawArc(
            rect,
            135f,
            sweepAngle,
            false,
            progressPaint
        )
    }
}
