package com.example.rowbyrowapp.ui.editor.model

import com.example.rowbyrowapp.domain.graph.Edge
import com.example.rowbyrowapp.domain.model.StitchVertex

sealed interface PlacedElement {
    val id: String
    val stitchItem: StitchItem
    val gridX: Float
    val gridY: Float
    val rotation: Float
    val round: Int

    fun withPosition(newGridX: Float, newGridY: Float): PlacedElement
    fun withRotation(newRotation: Float): PlacedElement
}

data class PlacedStitch(
    val vertex: StitchVertex,
    override val stitchItem: StitchItem,
    override val gridX: Float,
    override val gridY: Float,
    override val rotation: Float = 0f,
    override val round: Int = vertex.round
) : PlacedElement {
    override val id: String get() = "stitch_${vertex.id}"

    override fun withPosition(newGridX: Float, newGridY: Float) =
        copy(gridX = newGridX, gridY = newGridY)

    override fun withRotation(newRotation: Float) =
        copy(rotation = newRotation)
}


data class PlacedSlipStitch(
    val edge: Edge,
    override val stitchItem: StitchItem,
    override val gridX: Float,
    override val gridY: Float,
    override val rotation: Float = 0f,
    override val round: Int     // przechowuje numer rundy z momentu postawienia
) : PlacedElement {
    override val id: String get() = "slip_${edge.newVertex.id}_${edge.oldVertex.id}"

    override fun withPosition(newGridX: Float, newGridY: Float) =
        copy(gridX = newGridX, gridY = newGridY)

    override fun withRotation(newRotation: Float) = this // slip stitch nie podlega rotacji
}