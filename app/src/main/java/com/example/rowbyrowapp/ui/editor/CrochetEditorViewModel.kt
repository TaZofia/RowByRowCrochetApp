package com.example.rowbyrowapp.ui.editor

import androidx.lifecycle.ViewModel
import com.example.rowbyrowapp.data.PatternRepository
import com.example.rowbyrowapp.domain.engine.PatternTransformationEngine
import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.graph.GraphBuilder
import com.example.rowbyrowapp.domain.model.StitchVertex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.hypot
import com.example.rowbyrowapp.data.SavedPattern
import com.example.rowbyrowapp.export.PatternExportManager
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedSlipStitch
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import com.example.rowbyrowapp.ui.editor.model.StitchItem

class CrochetEditorViewModel(
    private val exportManager: PatternExportManager,
    private val patternRepository: PatternRepository,
    private val insertEdgeCalculator: InsertEdgeCalculator = InsertEdgeCalculator()
) : ViewModel() {

    private val graphBuilder = GraphBuilder()
    private val graph = graphBuilder.build()    // graf przechowywany w pamięci
    private val _uiState = MutableStateFlow(CrochetEditorUiState())   // modyfikowalny wewnętrzny stan
    val uiState: StateFlow<CrochetEditorUiState> = _uiState.asStateFlow()  // publiczny stan, trafiający do Compose UI
    private var lastPlacedVertex: StitchVertex? = null

    fun onEvent(event: CrochetEditorEvent) {
        when (event) {
            is CrochetEditorEvent.OnGridClick -> handleGridClick(event.gridX, event.gridY)
            is CrochetEditorEvent.OnStitchSelect -> handleStitchSelect(event.stitch)
            is CrochetEditorEvent.OnUndo -> handleUndo()
            is CrochetEditorEvent.OnNextRound -> handleNextRound()
            is CrochetEditorEvent.OnElementMove -> handleElementMove(event.element, event.newGridX, event.newGridY)
            is CrochetEditorEvent.OnElementRotate -> handleElementRotate(event.element, event.newRotation)
            is CrochetEditorEvent.OnGeneratePattern -> handleGeneratePattern()
            is CrochetEditorEvent.OnDismissPatternDialog -> _uiState.update { it.copy(generatedPattern = null) }
            is CrochetEditorEvent.OnExportImage -> handleExportImage()
            is CrochetEditorEvent.OnSavePattern -> handleSavePattern(event.name)
            is CrochetEditorEvent.OnLoadSavedPatterns -> refreshSavedPatterns()
            is CrochetEditorEvent.OnOpenPattern -> handleOpenPattern(event.pattern)
            is CrochetEditorEvent.OnDeletePattern -> handleDeletePattern(event.pattern)
            is CrochetEditorEvent.OnDismissMessage -> _uiState.update { it.copy(shortMessage = null) }
            is CrochetEditorEvent.OnNewPattern -> handleNewPattern()
        }
    }
    private fun handleNewPattern() {
        graphBuilder.restore(savedVertices = emptyList(), savedEdges = emptyList())
        lastPlacedVertex = null

        _uiState.update {
            it.copy(
                placedElements = emptyList(),
                slipStitchStep = SlipStitchStep.Idle,
                currentRound = 1,
                editingElement = null,
                insertLines = emptyList(),
                generatedPattern = null,
                currentPatternName = "",
            )
        }
    }

    private fun handleSavePattern(name: String) {
        val state = _uiState.value

        patternRepository.savePattern(
            name = name,
            currentRound = state.currentRound,
            lastPlacedVertexId = lastPlacedVertex?.id,
            graph = graph,
            placedElements = state.placedElements
        )

        _uiState.update {
            it.copy(
                currentPatternName = name,
                savedPatterns = patternRepository.getAllPatterns(),
                shortMessage = "Pattern '$name' successfully saved."
            )
        }
    }

    private fun handleOpenPattern(pattern: SavedPattern) {

        val restored = patternRepository.restorePattern(
            pattern,
            graphBuilder,
            graph
        )
        lastPlacedVertex = restored.lastPlacedVertex

        _uiState.update {
            it.copy(
                placedElements = restored.elements,
                currentRound = restored.currentRound,
                currentPatternName = restored.name,
                slipStitchStep = SlipStitchStep.Idle,
                editingElement = null,
                generatedPattern = null,
                insertLines = insertEdgeCalculator.recalculate(graph, restored.elements)
            )
        }
    }

    private fun refreshSavedPatterns() {
        _uiState.update {
            it.copy(savedPatterns = patternRepository.getAllPatterns())
        }
    }

    private fun handleDeletePattern(pattern: SavedPattern) {
        patternRepository.deletePattern(pattern.name)
        refreshSavedPatterns()
    }

    private fun handleExportImage() {
        val currentState = _uiState.value
        val elements = currentState.placedElements
        if (elements.isEmpty()) {
            _uiState.update { it.copy(shortMessage = "No pattern to save.") }
            return
        }

        val success = exportManager.exportToGallery(elements)
        val message = if (success) {
            "Pattern image saved successfully."
        } else {
            "Unable to save pattern image."
        }
        _uiState.update { it.copy(shortMessage = message) }
    }
    private fun handleGeneratePattern() {
        val currentState = _uiState.value
        val lastPlacedStitch = currentState.placedElements.filterIsInstance<PlacedStitch>().lastOrNull()

        if (lastPlacedStitch == null) {
            _uiState.update {
                it.copy(generatedPattern = "Place first stitch to generate pattern.")
            }
            return
        }
        graph.vertices.forEach { it.isEnd = false }  // czyszczenie starych flag is_end
        // Bezpośrednie ustawienie flagi na obiekcie wierzchołka
        lastPlacedStitch.vertex.isEnd = true

        // Generowanie wzoru
        try {
            val engine = PatternTransformationEngine(graphBuilder.build())
            val resultPattern = engine.fromGraphToReadablePattern()
            _uiState.update { it.copy(generatedPattern = resultPattern) }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update {
                it.copy(generatedPattern = "Error while generating pattern:\n${e.localizedMessage ?: "Unknown error"}")
            }
        }
    }
    private fun handleStitchSelect(stitch: StitchItem) {
        _uiState.update { currentState ->
            currentState.copy(
                selectedStitch = stitch,
                slipStitchStep = SlipStitchStep.Idle
            )
        }
    }
    private fun handleNextRound() {
        graphBuilder.closeChainSpace()
        _uiState.update { currentState ->
            currentState.copy(currentRound = currentState.currentRound + 1)
        }
    }

    private fun handleGridClick(gridX: Float, gridY: Float) {
        val currentState = _uiState.value
        val selectedStitch = currentState.selectedStitch

        // jeśli jesteśmy w kroku 2 lub 3 stawiania slip stich
        if (currentState.slipStitchStep != SlipStitchStep.Idle) {
            handleSlipStitchClick(gridX, gridY, currentState)
            return
        }

        // sprawdzamy w co kliknięto
        val clickedElement = currentState.placedElements
            .minByOrNull { hypot(it.gridX - gridX, it.gridY - gridY) }
            ?.takeIf { hypot(it.gridX - gridX, it.gridY - gridY) < 0.6f }

        if (clickedElement != null) {
            // kliknięty element jest w trybie edycji
            _uiState.update { it.copy(editingElement = clickedElement) }
        } else {
            // kliknięcie pustego miejsca to tworzenie nowego elementu
            if (selectedStitch.isSlipStitch) {
                handleSlipStitchClick(gridX, gridY, currentState)
            } else {
                handleRegularStitchClick(gridX, gridY, currentState)
            }
        }
    }

    private fun handleSlipStitchClick(gridX: Float, gridY: Float, currentState: CrochetEditorUiState) {
        val clickedStitch = currentState.placedElements
            .filterIsInstance<PlacedStitch>()
            .minByOrNull { hypot(it.gridX - gridX, it.gridY - gridY) }
            ?.takeIf { hypot(it.gridX - gridX, it.gridY - gridY) < 0.6f }

        when (val step = currentState.slipStitchStep) {
            is SlipStitchStep.Idle -> {
                // Krok 1: kliknięcie w siatkę miejsce dla slip-stitch
                // oczekiwanie na wybranie wierzchołka, z którego wychodzi krawędź
                _uiState.update {
                    it.copy(
                        slipStitchStep = SlipStitchStep.AwaitingFromVertex(gridX, gridY),
                        editingElement = null
                    )
                }
            }

            is SlipStitchStep.AwaitingFromVertex -> {
                // Krok 2: wybranie oczka, z którego wychodzi krawędź
                // oczekiwanie na wybranie wierzchołka, do którego wchodzi krawędź
                if (clickedStitch != null) {
                    _uiState.update {
                        it.copy(
                            slipStitchStep = SlipStitchStep.AwaitingToVertex(
                                gridX = step.gridX,
                                gridY = step.gridY,
                                fromVertex = clickedStitch.vertex
                            )
                        )
                    }
                }
            }

            is SlipStitchStep.AwaitingToVertex -> {
                // Krok 3: wybranie oczka, do którego wchodzi krawędź
                if (clickedStitch != null && isValidSlipStitch(clickedStitch.vertex, step.fromVertex)) {

                    // dodanie krawędzi do grafu
                    val edge = graphBuilder.connectStitches(
                        newVertex = step.fromVertex,
                        oldVertex = clickedStitch.vertex,
                        edgeType = EdgeType.SLIP_STITCH
                    )

                    // dodanie slip stitch
                    val newSlipStitch =
                        PlacedSlipStitch(
                            edge = edge,
                            stitchItem = currentState.selectedStitch,
                            gridX = step.gridX,
                            gridY = step.gridY,
                            round = currentState.currentRound
                        )

                    val updatedElements = currentState.placedElements + newSlipStitch

                    _uiState.update {
                        it.copy(
                            placedElements = updatedElements,
                            slipStitchStep = SlipStitchStep.Idle,
                            editingElement = newSlipStitch,
                            insertLines = insertEdgeCalculator.recalculate(graph, updatedElements)
                        )
                    }
                }
            }
        }
    }
    private fun isValidSlipStitch(fromVertex: StitchVertex, toVertex: StitchVertex): Boolean {
        // zabronione łączenie z samym sobą
        if (fromVertex.id == toVertex.id) return false

        val duplicateOfSlipStitchEdgeExists = graph.hasThisEdgeBetween(
            v1 = fromVertex,
            v2 = toVertex,
            typeOfEdge = EdgeType.SLIP_STITCH
        )
        return ! duplicateOfSlipStitchEdgeExists
    }
    private fun handleRegularStitchClick(gridX: Float, gridY: Float, currentState: CrochetEditorUiState) {
        val stitchType = currentState.selectedStitch.type ?: return
        val newVertex = graphBuilder.addStitch(stitchType, currentState.currentRound)

        lastPlacedVertex?.let { prevVertex ->
            graphBuilder.connectStitches(
                newVertex = newVertex,
                oldVertex = prevVertex,
                edgeType = EdgeType.PREVIOUS
            )
        }
        val newPlacedStitch =
            PlacedStitch(
                vertex = newVertex,
                stitchItem = currentState.selectedStitch,
                gridX = gridX,
                gridY = gridY,
                rotation = 0f
            )

        lastPlacedVertex = newVertex
        val updatedElements = currentState.placedElements + newPlacedStitch

        _uiState.update {
            it.copy(
                placedElements = updatedElements,
                editingElement = newPlacedStitch,
                insertLines = insertEdgeCalculator.recalculate(graph, updatedElements)
            )
        }
    }

    private fun handleUndo() {
        val currentState = _uiState.value
        if (currentState.placedElements.isNotEmpty()) {
            val elements = currentState.placedElements.toMutableList()
            val removedUI = elements.removeAt(elements.size - 1)

            // TO DO graph builder niech ma usuwanie krawędzi
            when (removedUI) {
                is PlacedStitch -> graphBuilder.removeStitch(removedUI.vertex)
                is PlacedSlipStitch -> graph.removeEdge(removedUI.edge)
            }
            lastPlacedVertex = elements.filterIsInstance<PlacedStitch>().lastOrNull()?.vertex
            val updatedRound = elements.lastOrNull()?.round ?: 1


            val updatedEditingElement = if (currentState.editingElement == removedUI) {
                elements.lastOrNull()
            } else {
                currentState.editingElement
            }

            _uiState.update {
                it.copy(
                    placedElements = elements,
                    slipStitchStep = SlipStitchStep.Idle,    // podczas cofania reset kroków slip stitch
                    currentRound = updatedRound,
                    editingElement = updatedEditingElement,
                    insertLines = insertEdgeCalculator.recalculate(graph, elements)
                )
            }
        }
    }
    private fun handleElementMove(element: PlacedElement, newGridX: Float, newGridY: Float) {
        _uiState.update { currentState ->
            // aktualizacja listy elementów z jednym z nich zmienionym
            val updatedElements = currentState.placedElements.map { current ->
                if (current.id == element.id) {
                    current.withPosition(newGridX = newGridX, newGridY = newGridY)
                } else current
            }
            // aktualizacja edytowanego elementu
            val updatedEditing = currentState.editingElement?.let { current ->
                if (current.id == element.id) {
                    current.withPosition(newGridX, newGridY)
                } else current
            }
            currentState.copy(
                placedElements = updatedElements,
                editingElement = updatedEditing,
                insertLines = insertEdgeCalculator.recalculate(graph,updatedElements)
            )
        }
    }

    private fun handleElementRotate(element: PlacedElement, newRotation: Float) {
        // normalizacja kąta do przedziału od 0 do 360
        val normalizedRotation = (newRotation % 360f + 360f) % 360f

        _uiState.update { currentState ->
            val updatedElements = currentState.placedElements.map { current ->
                if (current.id == element.id) {
                    current.withRotation(normalizedRotation)
                } else current
            }
            val updatedEditing = currentState.editingElement?.let { current ->
                if (current.id == element.id) {
                    current.withRotation(normalizedRotation)
                } else current
            }

            currentState.copy(
                placedElements = updatedElements,
                editingElement = updatedEditing
            )
        }
    }
}