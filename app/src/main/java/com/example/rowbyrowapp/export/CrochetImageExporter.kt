package com.example.rowbyrowapp.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.core.content.ContextCompat
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import kotlin.math.max
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withRotation

object CrochetImageExporter {

    fun createPatternBitmap(
        context: Context,
        placedElements: List<PlacedElement>
    ): Bitmap? {
        if (placedElements.isEmpty()) return null

        // granice wzoru
        val minX = placedElements.minOf { it.gridX }
        val maxX = placedElements.maxOf { it.gridX }
        val minY = placedElements.minOf { it.gridY }
        val maxY = placedElements.maxOf { it.gridY }

        val rawWidth = max(1f, maxX - minX)
        val rawHeight = max(1f, maxY - minY)

        // tworzenie kwadratowej siatki z marginesem, aby wzór nie był rozciągany w galerii
        val maxGridDim = max(rawWidth, rawHeight)
        val padding = 2.5f
        val sideGridSize = maxGridDim + (padding * 2)

        val centerX = (minX + maxX) / 2f
        val centerY = (minY + maxY) / 2f

        val startX = centerX - (sideGridSize / 2f)
        val startY = centerY - (sideGridSize / 2f)

        // wysoka rozdzielczość
        val targetSizePx = max(1080f, sideGridSize * 120f)
        val cellSizePx = targetSizePx / sideGridSize

        val bitmap = createBitmap(targetSizePx.toInt(), targetSizePx.toInt())
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        fun toPxX(gridX: Float): Float = (gridX - startX) * cellSizePx
        fun toPxY(gridY: Float): Float = (gridY - startY) * cellSizePx

        // zachowanie naturalnych proporcji ikon
        val baseIconPx = cellSizePx * 0.85f

        placedElements.forEach { element ->
            val drawable = ContextCompat.getDrawable(context, element.stitchItem.iconRes)?.mutate()
            if (drawable != null) {
                drawable.setTint(Color.BLACK)

                // Pobranie i zachowanie oryginalnych proporcji wektora ikony
                val intrinsicW = drawable.intrinsicWidth.toFloat()
                val intrinsicH = drawable.intrinsicHeight.toFloat()
                val aspect = if (intrinsicW > 0 && intrinsicH > 0) intrinsicW / intrinsicH else 1f

                val iconW: Float
                val iconH: Float
                if (aspect >= 1f) {
                    iconW = baseIconPx
                    iconH = baseIconPx / aspect
                } else {
                    iconH = baseIconPx
                    iconW = baseIconPx * aspect
                }

                val pxX = toPxX(element.gridX)
                val pxY = toPxY(element.gridY)

                val left = (pxX - iconW / 2f).toInt()
                val top = (pxY - iconH / 2f).toInt()
                val right = (left + iconW).toInt()
                val bottom = (top + iconH).toInt()

                drawable.setBounds(left, top, right, bottom)

                canvas.withRotation(element.rotation, pxX, pxY) {
                    drawable.draw(this)
                }
            }
        }

        return bitmap
    }
}