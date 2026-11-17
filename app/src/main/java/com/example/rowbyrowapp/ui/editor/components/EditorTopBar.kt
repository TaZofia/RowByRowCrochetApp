package com.example.rowbyrowapp.ui.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun EditorTopBar(
    hasElements: Boolean,
    onUndo: () -> Unit,
    onGeneratePattern: () -> Unit,
    onNewPattern: () -> Unit,
    onOpenSaveDialog: () -> Unit,
    onExportImage: () -> Unit,
    onOpenFolderDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showProjectMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UndoButton(onUndo = onUndo)

        Button(
            onClick = onGeneratePattern,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Pattern")
        }

        Box {
            FilledTonalButton(onClick = { showProjectMenu = true }) {
                Text("Project ▾")
            }

            DropdownMenu(
                expanded = showProjectMenu,
                onDismissRequest = { showProjectMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("New pattern") },
                    onClick = {
                        showProjectMenu = false
                        onNewPattern()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Save pattern") },
                    enabled = hasElements,
                    onClick = {
                        showProjectMenu = false
                        onOpenSaveDialog()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Save image to gallery") },
                    enabled = hasElements,
                    onClick = {
                        showProjectMenu = false
                        onExportImage()
                    }
                )
                DropdownMenuItem(
                    text = { Text("My patterns") },
                    onClick = {
                        showProjectMenu = false
                        onOpenFolderDialog()
                    }
                )
            }
        }
    }
}