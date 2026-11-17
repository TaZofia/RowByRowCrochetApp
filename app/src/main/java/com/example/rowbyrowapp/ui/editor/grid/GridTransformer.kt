package com.example.rowbyrowapp.ui.editor.grid

import androidx.compose.ui.geometry.Offset


// Klasa odpowiedzialna za przeliczanie pozycji między układem pikseli ekranu a siatką

class GridTransformer(
    val baseGridPx: Float,
    val scale: Float,
    val offset: Offset,
    val center: Offset
) {
    val scaledGridPx: Float = baseGridPx * scale


    // Konwersja pozycji z siatki (gridX, gridY) na pozycję w pikselach na ekranie
    fun gridToPx(gridX: Float, gridY: Float, extraPx: Offset = Offset.Zero): Offset {
        val pxX = center.x + offset.x + (gridX * scaledGridPx) + extraPx.x
        val pxY = center.y + offset.y + (gridY * scaledGridPx) + extraPx.y
        return Offset(pxX, pxY)
    }

    // Konwersja pozycji pikselowej na ekranie (np. punkt kliknięcia) na współrzędne siatki
    fun pxToGrid(screenOffset: Offset): Pair<Float, Float> {
        val relativeX = screenOffset.x - (center.x + offset.x)
        val relativeY = screenOffset.y - (center.y + offset.y)
        return Pair(relativeX / scaledGridPx, relativeY / scaledGridPx)
    }


    // Przeliczenie przesunięcie w pikselach (drag delta) na zmianę współrzędnych siatki
    fun deltaPxToGrid(deltaPx: Offset): Pair<Float, Float> {
        return Pair(deltaPx.x / scaledGridPx, deltaPx.y / scaledGridPx)
    }
}