package com.example.rowbyrowapp.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rowbyrowapp.data.PatternRepository
import com.example.rowbyrowapp.export.PatternExportManager
import com.example.rowbyrowapp.ui.editor.grid.EditorWorkspace
import com.example.rowbyrowapp.ui.editor.components.PatternSheet
import com.example.rowbyrowapp.ui.editor.components.SavePatternDialog
import com.example.rowbyrowapp.ui.editor.components.SavedPatternsFolderDialog
import com.example.rowbyrowapp.ui.editor.menu.MenuPanel



private val MENU_PANEL_PORTRAIT_HEIGHT = 160.dp
private val MENU_PANEL_LANDSCAPE_WIDTH = 220.dp

// Ekran-wyłącznie układ UI i przekazywanie zdarzeń
@Composable
fun CrochetEditorScreen(
    viewModel: CrochetEditorViewModel = rememberEditorViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showSaveDialog by remember { mutableStateOf(false) }
    var showFolderDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    LaunchedEffect(uiState.shortMessage) {
        uiState.shortMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(CrochetEditorEvent.OnDismissMessage)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(
                    bottom = if (!isLandscape) MENU_PANEL_PORTRAIT_HEIGHT else 0.dp,
                    end = if (isLandscape) MENU_PANEL_LANDSCAPE_WIDTH else 0.dp
                )
            )
        }
    ) { innerPadding ->
        // nałożenie wyliczonych odstępów systemowych na całą zawartość ekranu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CrochetEditorContent(
                uiState = uiState,
                onEvent = viewModel::onEvent,
                onOpenSaveDialog = { showSaveDialog = true },
                onOpenFolderDialog = {
                    viewModel.onEvent(CrochetEditorEvent.OnLoadSavedPatterns)
                    showFolderDialog = true
                }
            )

            uiState.generatedPattern?.let { patternText ->
                PatternSheet(
                    patternText = patternText,
                    onDismiss = { viewModel.onEvent(CrochetEditorEvent.OnDismissPatternDialog) }
                )
            }
            // Okno wpisywania nazwy przy zapisie
            if (showSaveDialog) {
                SavePatternDialog(
                    initialName = uiState.currentPatternName,
                    onDismiss = { showSaveDialog = false },
                    onConfirm = { name ->
                        viewModel.onEvent(CrochetEditorEvent.OnSavePattern(name))
                        showSaveDialog = false
                    }
                )
            }
            // Okno folderu z zapisanymi wzorami
            if (showFolderDialog) {
                SavedPatternsFolderDialog(
                    patterns = uiState.savedPatterns,
                    currentPatternName = uiState.currentPatternName,
                    onDismiss = { showFolderDialog = false },
                    onSelectPattern = { selected ->
                        viewModel.onEvent(CrochetEditorEvent.OnOpenPattern(selected))
                        showFolderDialog = false
                    },
                    onDeletePattern = { pattern ->
                        viewModel.onEvent(CrochetEditorEvent.OnDeletePattern(pattern))
                    }
                )
            }
        }
    }
}
@Composable
fun CrochetEditorContent(
    uiState: CrochetEditorUiState,
    onEvent: (CrochetEditorEvent) -> Unit,
    onOpenSaveDialog: () -> Unit,
    onOpenFolderDialog: () -> Unit
) {

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize()) {
            EditorWorkspace(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                isLandscape = true,
                uiState = uiState,
                onEvent = onEvent,
                onOpenSaveDialog = onOpenSaveDialog,
                onOpenFolderDialog = onOpenFolderDialog
            )

            MenuPanel(
                modifier = Modifier
                    .width(MENU_PANEL_LANDSCAPE_WIDTH)
                    .fillMaxHeight(),
                isLandscape = true,
                selectedStitch = uiState.selectedStitch,
                onStitchSelect = { stitch -> onEvent(CrochetEditorEvent.OnStitchSelect(stitch)) }
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            EditorWorkspace(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                isLandscape = false,
                uiState = uiState,
                onEvent = onEvent,
                onOpenSaveDialog = onOpenSaveDialog,
                onOpenFolderDialog = onOpenFolderDialog
            )
            MenuPanel(
                modifier = Modifier
                    .height(MENU_PANEL_PORTRAIT_HEIGHT)
                    .fillMaxWidth(),
                isLandscape = false,
                selectedStitch = uiState.selectedStitch,
                onStitchSelect = { stitch -> onEvent(CrochetEditorEvent.OnStitchSelect(stitch)) }
            )
        }
    }
}
@Composable
private fun rememberEditorViewModel(): CrochetEditorViewModel {
    return viewModel {
        val app = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
        CrochetEditorViewModel(
            patternRepository = PatternRepository(app),
            exportManager = PatternExportManager(app)
        )
    }
}