package com.example.rowbyrowapp.ui.editor.grid
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun StitchRotationHandle(
    stitchPx: Offset,
    currentRotation: Float,
    onRotatePreview: (previewAngle: Float) -> Unit,
    onRotateEnd: (finalAngle: Float) -> Unit,
    modifier: Modifier = Modifier,
    leverLengthDp: Dp = 75.dp,
    handleSizeDp: Dp = 30.dp,
    primaryColor: Color = MaterialTheme.colorScheme.primary
) {
    val density = LocalDensity.current
    val leverLengthPx = with(density) { leverLengthDp.toPx() }
    val handleSizePx = with(density) { handleSizeDp.toPx() }

    // wyliczenie pozycji uchwytu w pikselach względem aktualnego kąta
    val rad = Math.toRadians((currentRotation - 90f).toDouble())
    val handlePx = Offset(
        x = stitchPx.x + (leverLengthPx * cos(rad)).toFloat(),
        y = stitchPx.y + (leverLengthPx * sin(rad)).toFloat()
    )

    var touchPx by remember { mutableStateOf(Offset.Zero) }

    val currentHandlePx by rememberUpdatedState(handlePx)
    val currentOnRotatePreview by rememberUpdatedState(onRotatePreview)
    val currentOnRotateEnd by rememberUpdatedState(onRotateEnd)
    val currentStitchPx by rememberUpdatedState(stitchPx)

    Box(modifier = modifier.fillMaxSize()) {
        // przerywana linia łącząca środek oczka z uchwytem
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = primaryColor,
                start = stitchPx,
                end = handlePx,
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(12f, 8f), 0f
                )
            )
        }

        //uchwyt
        Box(
            modifier = Modifier
                .size(handleSizeDp)
                .offset {
                    IntOffset(
                        (handlePx.x - handleSizePx / 2f).roundToInt(),
                        (handlePx.y - handleSizePx / 2f).roundToInt()
                    )
                }
                .background(primaryColor, shape = CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            touchPx = currentHandlePx
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            touchPx += dragAmount

                            val vector = touchPx - currentStitchPx
                            val angle = Math.toDegrees(
                                atan2(vector.y.toDouble(), vector.x.toDouble())
                            ).toFloat() + 90f

                            currentOnRotatePreview(angle)
                        },
                        onDragEnd = {
                            val vector = touchPx - currentStitchPx
                            val angle = Math.toDegrees(
                                atan2(vector.y.toDouble(), vector.x.toDouble())
                            ).toFloat() + 90f

                            currentOnRotateEnd(angle)
                        },
                        onDragCancel = {
                            val vector = touchPx - currentStitchPx
                            val angle = Math.toDegrees(
                                atan2(vector.y.toDouble(), vector.x.toDouble())
                            ).toFloat() + 90f

                            currentOnRotateEnd(angle)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        MaterialTheme.colorScheme.onPrimary,
                        shape = CircleShape
                    )
            )
        }
    }
}