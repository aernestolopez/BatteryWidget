package com.example.batterywidget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object ArcBitmapGenerator {

    fun createSingleArcBitmap(
        percent: Int,
        charging: Boolean,
        enabled: Boolean,
        size: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val stroke = size * 0.12f
        val padding = size * 0.10f
        val rect = RectF(padding, padding, size - padding, size - padding)

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            color = 0x22FFFFFF 
        }

        val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            strokeCap = Paint.Cap.ROUND
            color = when {
                !enabled -> 0x44FFFFFF
                percent <= 20 && !charging -> 0xFFFF5252.toInt()
                else -> 0xFF2DDA73.toInt()
            }
        }

        // Arco más abierto (estilo referencia): de 170 grados a 370 (barrido de 200)
        val startAngle = 170f
        val sweepAngle = 200f

        canvas.drawArc(rect, startAngle, sweepAngle, false, basePaint)

        if (percent >= 0 && enabled) {
            val progressSweep = sweepAngle * (percent / 100f)
            canvas.drawArc(rect, startAngle, progressSweep, false, progressPaint)
        }

        return bitmap
    }
}
