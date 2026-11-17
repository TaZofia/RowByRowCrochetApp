package com.example.rowbyrowapp.ui.editor.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun InfiniteGridBackground(
    scale: Float,
    offset: Offset,
    baseGridSizeDp: Float = BASE_GRID_SIZE_DP,
    gridColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaledGridSize = baseGridSizeDp.dp.toPx() * scale
        val width = size.width
        val height = size.height

        val centerX = width / 2f + offset.x
        val centerY = height / 2f + offset.y

        val startX = (centerX % scaledGridSize) - scaledGridSize
        val startY = (centerY % scaledGridSize) - scaledGridSize

        var x = startX
        while (x < width + scaledGridSize) {
            drawLine(
                color = gridColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += scaledGridSize
        }

        var y = startY
        while (y < height + scaledGridSize) {
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += scaledGridSize
        }
    }
}