package com.example.rowbyrowapp.ui.editor.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UndoButton(
    modifier: Modifier = Modifier,
    onUndo: () -> Unit
) {
    FloatingActionButton(
        onClick = onUndo,
        modifier = modifier.size(48.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(10.dp)
    ) {
        Text("↩", fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}


