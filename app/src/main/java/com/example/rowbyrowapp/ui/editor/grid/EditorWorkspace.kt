package com.example.rowbyrowapp.ui.editor.grid
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.rowbyrowapp.ui.editor.CrochetEditorEvent
import com.example.rowbyrowapp.ui.editor.CrochetEditorUiState
import com.example.rowbyrowapp.ui.editor.components.EditorTopBar
import com.example.rowbyrowapp.ui.editor.components.RoundCounterCard

@Composable
fun EditorWorkspace(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    uiState: CrochetEditorUiState,
    onEvent: (CrochetEditorEvent) -> Unit,
    onOpenSaveDialog: () -> Unit,
    onOpenFolderDialog: () -> Unit
) {
    val context = LocalContext.current
    Box(modifier = modifier) {
        // Płótno
        WorkPanel(
            modifier = Modifier.fillMaxSize(),
            placedStitches = uiState.placedElements,
            previewElement = uiState.previewElement,
            highlightedVertexId = uiState.highlightedVertexId,
            editingElement = uiState.editingElement,
            connectionLines = uiState.connectionLines,
            onGridClick = { x, y -> onEvent(CrochetEditorEvent.OnGridClick(x, y)) },
            onElementMove = { element, x, y -> onEvent(CrochetEditorEvent.OnElementMove(element, x, y)) },
            onElementRotate = { element, r -> onEvent(CrochetEditorEvent.OnElementRotate(element, r)) }
        )

        // Górny pasek akcji
        EditorTopBar(
            hasElements = uiState.placedElements.isNotEmpty(),
            onUndo = { onEvent(CrochetEditorEvent.OnUndo) },
            onGeneratePattern = { onEvent(CrochetEditorEvent.OnGeneratePattern) },
            onNewPattern = { onEvent(CrochetEditorEvent.OnNewPattern) },
            onOpenSaveDialog = onOpenSaveDialog,
            onExportImage = { onEvent(CrochetEditorEvent.OnExportImage) },
            onOpenFolderDialog = onOpenFolderDialog,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )

        // Licznik rund
        RoundCounterCard(
            currentRound = uiState.currentRound,
            onNextRound = { onEvent(CrochetEditorEvent.OnNextRound) },
            modifier = Modifier
                .align(if (isLandscape) Alignment.TopEnd else Alignment.BottomStart)
                .padding(16.dp)
        )
    }
}