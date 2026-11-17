package com.example.rowbyrowapp.ui.editor.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import com.example.rowbyrowapp.ui.editor.model.ConnectionLine
import com.example.rowbyrowapp.ui.editor.model.PreviewElement
import kotlin.math.roundToInt

const val BASE_GRID_SIZE_DP = 40f

@Composable
fun WorkPanel(
    modifier: Modifier = Modifier,
    placedStitches: List<PlacedElement>,
    previewElement: PreviewElement? = null,
    highlightedVertexId: Int? = null,
    editingElement: PlacedElement? = null,
    connectionLines: List<ConnectionLine> = emptyList(),
    onGridClick: (gridX: Float, gridY: Float) -> Unit,
    onElementMove: (element: PlacedElement, newGridX: Float, newGridY: Float) -> Unit,
    onElementRotate: (element: PlacedElement, newRotation: Float) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var dragOffsetPx by remember(editingElement) { mutableStateOf(Offset.Zero) }  // do przenoszenia
    var dragRotationDelta by remember(editingElement) { mutableFloatStateOf(0f) }  // do rotowania

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clipToBounds()
            // 1. Gest przesuwania i powiększania widoku
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.5f, 3.0f)
                    offset += pan
                }
            }
    ) {
        val density = LocalDensity.current
        val baseGridPx = with(density) { BASE_GRID_SIZE_DP.dp.toPx() }

        val center = Offset(
            x = with(density) { maxWidth.toPx() / 2f },
            y = with(density) { maxHeight.toPx() / 2f }
        )

        val gridTransformer = remember(baseGridPx, scale, offset, center) {
            GridTransformer(baseGridPx, scale, offset, center)
        }

        // Layer do wykrywania pojedynczego kliknięcia i przeliczania na pozycję w siatce
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(scale, offset) {
                    detectTapGestures { tapOffset ->
                        val (gridX, gridY) = gridTransformer.pxToGrid(tapOffset)
                        onGridClick(gridX, gridY)
                    }
                }
        ) {
            // Nieskończona siatka w tle
            InfiniteGridBackground(
                scale = scale,
                offset = offset,
                baseGridSizeDp = BASE_GRID_SIZE_DP
            )

            // linie insert i slip-stitch
            ConnectionLinesCanvas(
                connectionLines = connectionLines,
                editingElement = editingElement,
                dragOffsetPx = dragOffsetPx,
                gridTransformer = gridTransformer
            )

            // Wyświetlanie postawionych oczek
            placedStitches.forEach { placed ->
                PlacedStitchNode(
                    placed = placed,
                    isEditing = placed.id == editingElement?.id,
                    isHighlighted = placed is PlacedStitch && placed.vertex.id == highlightedVertexId,
                    scale = scale,
                    baseGridPx = baseGridPx,
                    dragOffsetPx = dragOffsetPx,
                    dragRotationDelta = dragRotationDelta,
                    gridTransformer = gridTransformer,
                    onDragOffsetChange = { dragOffsetPx = it },
                    onElementMove = onElementMove
                )
            }

            // rotacja
            if (editingElement is PlacedStitch) {
                val stitchPx = gridTransformer.gridToPx(
                    editingElement.gridX,
                    editingElement.gridY,
                    dragOffsetPx
                )
                val baseRotation = editingElement.rotation
                val activeRotation = baseRotation + dragRotationDelta

                StitchRotationHandle(
                    stitchPx = stitchPx,
                    currentRotation = activeRotation,
                    onRotatePreview = { previewAngle ->
                        dragRotationDelta = previewAngle - baseRotation
                    },
                    onRotateEnd = { finalAngle ->
                        dragRotationDelta = 0f
                        onElementRotate(editingElement, finalAngle)
                    }
                )
            }

            // wyświetlanie potencjalnego oczka podglądu (slip stitch)
            previewElement?.let { preview ->
                val stitchPx = gridTransformer.gridToPx(preview.gridX, preview.gridY)

                val iconSizeDp = (48 * scale).dp
                val iconSizePx = with(density) { iconSizeDp.toPx() }

                Icon(
                    painter = painterResource(id = preview.stitchItem.iconRes),
                    contentDescription = "Slip stitch preview",
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(iconSizeDp)
                        .offset {
                            IntOffset(
                                (stitchPx.x - iconSizePx / 2f).roundToInt(),
                                (stitchPx.y - iconSizePx / 2f).roundToInt()
                            )
                        }
                )
            }
        }
    }
}