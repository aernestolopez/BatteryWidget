package com.example.batterywidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object ArcBitmapGenerator {

    fun createDoubleArcBitmap(
        phonePercent: Int,
        headsetPercent: Int,
        phoneCharging: Boolean,
        headsetConnected: Boolean,
        size: Int
    ): Bitmap {

        // Hacemos que el tamaño del bitmap sea más grande para acomodar ambos arcos.
        val bitmapWidth = size * 2 // Doble del tamaño original para separar los arcos
        val bitmap = Bitmap.createBitmap(bitmapWidth, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // ===== Medidas =====
        val outerStroke = size * 0.10f
        val innerStroke = size * 0.08f
        val gap = size * 0.04f

        // ===== Rectángulos de los arcos =====

        val outerRect = RectF(
            outerStroke,
            outerStroke,
            bitmapWidth / 2f - outerStroke,
            size - outerStroke
        )


        val innerRect = RectF(
            bitmapWidth / 2f + gap,
            outerStroke + gap,
            bitmapWidth - outerStroke,
            size - outerStroke - gap
        )


        val basePaintOuter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = outerStroke
            color = 0x33FFFFFF
        }

        val basePaintInner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = innerStroke
            color = 0x22FFFFFF
        }

        val phonePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = outerStroke
            strokeCap = Paint.Cap.ROUND
            color = when {
                phoneCharging -> 0xFF4CAF50.toInt() // verde
                phonePercent <= 20 -> 0xFFFF5252.toInt() // rojo
                else -> 0xFFFFFFFF.toInt()
            }
        }

        val headsetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = innerStroke
            strokeCap = Paint.Cap.ROUND
            color = if (headsetConnected)
                0xFFFFFFFF.toInt()
            else
                0x55FFFFFF
        }


        // Dibuja el arco exterior (de teléfono) en la mitad izquierda
        canvas.drawArc(outerRect, 180f, 180f, false, basePaintOuter)

        // Dibuja el arco interior (de cascos) en la mitad derecha
        canvas.drawArc(innerRect, 180f, 180f, false, basePaintInner)


        if (phonePercent >= 0) {
            val sweep = 180f * phonePercent / 100f
            canvas.drawArc(outerRect, 180f, sweep, false, phonePaint)
        }


        if (headsetConnected && headsetPercent >= 0) {
            val sweep = 180f * headsetPercent / 100f
            canvas.drawArc(innerRect, 180f, sweep, false, headsetPaint)
        }

        return bitmap
    }
}
