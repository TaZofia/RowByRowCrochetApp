package com.example.rowbyrowapp.ui.editor.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import kotlin.math.roundToInt

@Composable
fun PlacedStitchNode(
    placed: PlacedElement,
    isEditing: Boolean,
    isHighlighted: Boolean,
    scale: Float,
    baseGridPx: Float,
    dragOffsetPx: Offset,
    dragRotationDelta: Float,
    gridTransformer: GridTransformer,
    onDragOffsetChange: (Offset) -> Unit,
    onElementMove: (element: PlacedElement, newGridX: Float, newGridY: Float) -> Unit
) {
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val currentDragPx = if (isEditing) dragOffsetPx else Offset.Zero
    val stitchPx = gridTransformer.gridToPx(placed.gridX, placed.gridY, currentDragPx)

    val iconSizeDp = (42 * scale).dp
    val haloSizeDp = (58 * scale).dp
    val currentSizeDp = if (isEditing) haloSizeDp else iconSizeDp
    val currentSizePx = with(density) { currentSizeDp.toPx() }

    val currentOnElementMove by rememberUpdatedState(onElementMove)
    val currentPlaced by rememberUpdatedState(placed)
    val currentScale by rememberUpdatedState(scale)

    val baseRotation = (placed as? PlacedStitch)?.rotation ?: 0f
    val activeRotation = if (isEditing) baseRotation + dragRotationDelta else baseRotation

    Box(
        modifier = Modifier
            .size(currentSizeDp)
            .offset {
                IntOffset(
                    (stitchPx.x - currentSizePx / 2f).roundToInt(),
                    (stitchPx.y - currentSizePx / 2f).roundToInt()
                )
            }
            .then(
                if (isEditing) {
                    Modifier.pointerInput(placed) {
                        var totalDrag = Offset.Zero
                        detectDragGestures(
                            onDragStart = {
                                totalDrag = Offset.Zero
                                onDragOffsetChange(Offset.Zero)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                totalDrag += dragAmount
                                onDragOffsetChange(totalDrag)
                            },
                            onDragEnd = {
                                // dopiero po zwolnieniu palca jednorazowo aktualizowane jest View Model
                                val scaledGridPx = baseGridPx * currentScale
                                val deltaGridX = totalDrag.x / scaledGridPx
                                val deltaGridY = totalDrag.y / scaledGridPx
                                currentOnElementMove(
                                    currentPlaced,
                                    currentPlaced.gridX + deltaGridX,
                                    currentPlaced.gridY + deltaGridY
                                )
                                totalDrag = Offset.Zero
                                onDragOffsetChange(Offset.Zero)
                            },
                            onDragCancel = {
                                totalDrag = Offset.Zero
                                onDragOffsetChange(Offset.Zero)
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Poświata pod oczkiem edytowanym
        if (isEditing) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.45f),
                            primaryColor.copy(alpha = 0.15f),
                            primaryColor.copy(alpha = 0f)
                        )
                    )
                )
            }
        }

        // Kolor ikony
        val tintColor = when {
            isEditing -> primaryColor
            isHighlighted -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.onSurface
        }

        Icon(
            painter = painterResource(id = placed.stitchItem.iconRes),
            contentDescription = placed.stitchItem.name,
            tint = tintColor,
            modifier = Modifier
                .size(iconSizeDp)
                .graphicsLayer {
                    rotationZ = activeRotation
                }
        )
    }
}
