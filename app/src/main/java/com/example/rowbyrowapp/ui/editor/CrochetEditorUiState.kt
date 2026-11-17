package com.example.rowbyrowapp.ui.editor

import com.example.rowbyrowapp.domain.model.StitchVertex
import com.example.rowbyrowapp.data.SavedPattern
import com.example.rowbyrowapp.ui.editor.model.ConnectionLine
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedSlipStitch
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import com.example.rowbyrowapp.ui.editor.model.PreviewElement
import com.example.rowbyrowapp.ui.editor.model.StitchItem
import com.example.rowbyrowapp.ui.editor.model.availableStitches

// to co widzi użytkownik i dostępne akcje, które może wykonać
data class CrochetEditorUiState(
    val placedElements: List<PlacedElement> = emptyList(),
    val selectedStitch: StitchItem = availableStitches.first(),
    val slipStitchStep: SlipStitchStep = SlipStitchStep.Idle,
    val currentRound: Int = 1,
    val editingElement: PlacedElement? = null,
    val insertLines: List<ConnectionLine> = emptyList(),
    val generatedPattern: String? = null,
    val currentPatternName: String = "",
    val savedPatterns: List<SavedPattern> = emptyList(),
    val shortMessage: String? = null
) {
    val previewElement: PreviewElement?
        get() = when (val step = slipStitchStep) {
            is SlipStitchStep.AwaitingFromVertex -> PreviewElement(selectedStitch, step.gridX, step.gridY)
            is SlipStitchStep.AwaitingToVertex -> PreviewElement(selectedStitch, step.gridX, step.gridY)
            SlipStitchStep.Idle -> null
        }

    // zwraca id pierwszego wierzchołka w trakcie oczekiwania na wybór drugiego wierzchołka
    val highlightedVertexId: Int?
        get() = (slipStitchStep as? SlipStitchStep.AwaitingToVertex)?.fromVertex?.id


    // linie między slip stitch a oczkami które łączy
    val connectionLines: List<ConnectionLine>
        get() {
            val lines = mutableListOf<ConnectionLine>()
            val stitchesMap = placedElements
                .filterIsInstance<PlacedStitch>()
                .associateBy { it.vertex.id }

            // linie istniejących elementów
            placedElements.filterIsInstance<PlacedSlipStitch>().forEach { slip ->
                stitchesMap[slip.edge.newVertex.id]?.let { from ->
                    lines.add(ConnectionLine(slip.gridX, slip.gridY, from.gridX, from.gridY))
                }
                stitchesMap[slip.edge.oldVertex.id]?.let { to ->
                    lines.add(ConnectionLine(slip.gridX, slip.gridY, to.gridX, to.gridY))
                }
            }

            // linia podczas podglądu
            if (slipStitchStep is SlipStitchStep.AwaitingToVertex) {
                stitchesMap[slipStitchStep.fromVertex.id]?.let { from ->
                    lines.add(ConnectionLine(slipStitchStep.gridX, slipStitchStep.gridY, from.gridX, from.gridY))
                }
            }
            lines.addAll(insertLines)

            return lines
        }
}


sealed interface CrochetEditorEvent {
    data class OnGridClick(val gridX: Float, val gridY: Float) : CrochetEditorEvent
    data class OnStitchSelect(val stitch: StitchItem) : CrochetEditorEvent
    data object OnUndo : CrochetEditorEvent
    data object OnNextRound : CrochetEditorEvent
    data class OnElementMove(val element: PlacedElement, val newGridX: Float, val newGridY: Float) : CrochetEditorEvent
    data class OnElementRotate(val element: PlacedElement, val newRotation: Float) : CrochetEditorEvent
    data object OnGeneratePattern : CrochetEditorEvent
    data object OnDismissPatternDialog : CrochetEditorEvent
    data object OnExportImage : CrochetEditorEvent
    data class OnSavePattern(val name: String) : CrochetEditorEvent
    data object OnLoadSavedPatterns : CrochetEditorEvent
    data class OnOpenPattern(val pattern: SavedPattern) : CrochetEditorEvent
    data class OnDeletePattern(val pattern: SavedPattern) : CrochetEditorEvent
    data object OnDismissMessage : CrochetEditorEvent
    data object OnNewPattern : CrochetEditorEvent
}


sealed interface SlipStitchStep {
    data object Idle : SlipStitchStep

    // Krok 1: postawiono ikonę, oczekiwanie na wybór pierwszego wierzchołka (newVertex)
    data class AwaitingFromVertex(val gridX: Float, val gridY: Float) : SlipStitchStep

    // Krok 2: wybrano newVertex, oczekiwanie na wybór drugiego wierzchołka (oldVertex)
    data class AwaitingToVertex(
        val gridX: Float,
        val gridY: Float,
        val fromVertex: StitchVertex
    ) : SlipStitchStep
}