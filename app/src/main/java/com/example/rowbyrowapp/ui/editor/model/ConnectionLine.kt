package com.example.rowbyrowapp.ui.editor.model
enum class ConnectionType {
    SLIP_STITCH,
    INSERT
}
data class ConnectionLine(
    val startX: Float,
    val startY: Float,
    val endX: Float,
    val endY: Float,
    val type: ConnectionType = ConnectionType.SLIP_STITCH
)