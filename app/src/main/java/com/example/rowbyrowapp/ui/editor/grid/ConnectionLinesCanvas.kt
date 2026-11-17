package com.example.rowbyrowapp.ui.editor.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.rowbyrowapp.ui.editor.model.ConnectionLine
import com.example.rowbyrowapp.ui.editor.model.ConnectionType
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import kotlin.math.abs

@Composable
fun ConnectionLinesCanvas(
    connectionLines: List<ConnectionLine>,
    editingElement: PlacedElement?,
    dragOffsetPx: Offset,
    gridTransformer: GridTransformer,
    modifier: Modifier = Modifier
) {
    // wspólne dla insert i slip-stitch
    val density = LocalDensity.current
    val lineColor = MaterialTheme.colorScheme.primaryContainer
    val lineStrokeWidthPx = with(density) { 2.dp.toPx() }

    // efekt dla linii insert
    val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        connectionLines.forEach { line ->
            // sprawdzenie, czy dany koniec linii należy do przeciąganego elementu
            val isStartEditing = editingElement != null &&
                    abs(line.startX - editingElement.gridX) < 0.001f &&
                    abs(line.startY - editingElement.gridY) < 0.001f

            val isEndEditing = editingElement != null &&
                    abs(line.endX - editingElement.gridX) < 0.001f &&
                    abs(line.endY - editingElement.gridY) < 0.001f

            val startExtraPx = if (isStartEditing) dragOffsetPx else Offset.Zero
            val endExtraPx = if (isEndEditing) dragOffsetPx else Offset.Zero

            val startPx = gridTransformer.gridToPx(line.startX, line.startY, startExtraPx)
            val endPx = gridTransformer.gridToPx(line.endX, line.endY, endExtraPx)

            if (line.type == ConnectionType.INSERT) {
                drawLine(
                    color = lineColor,
                    start = startPx,
                    end = endPx,
                    strokeWidth = lineStrokeWidthPx,
                    pathEffect = dashedEffect
                )
            } else {
                drawLine(
                    color = lineColor,
                    start = startPx,
                    end = endPx,
                    strokeWidth = lineStrokeWidthPx
                )
            }
        }
    }
}