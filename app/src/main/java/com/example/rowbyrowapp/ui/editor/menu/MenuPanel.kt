package com.example.rowbyrowapp.ui.editor.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rowbyrowapp.ui.editor.model.StitchItem
import com.example.rowbyrowapp.ui.editor.model.availableStitches

@Composable
fun MenuPanel(
    modifier: Modifier = Modifier,
    isLandscape: Boolean,
    selectedStitch: StitchItem,
    onStitchSelect: (StitchItem) -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        if (isLandscape) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableStitches) { stitch ->
                    StitchCard(
                        item = stitch,
                        isSelected = stitch.id == selectedStitch.id,
                        onClick = { onStitchSelect(stitch) }
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(availableStitches) { stitch ->
                    StitchCard(
                        item = stitch,
                        isSelected = stitch.id == selectedStitch.id,
                        onClick = { onStitchSelect(stitch) }
                    )
                }
            }
        }
    }
}

