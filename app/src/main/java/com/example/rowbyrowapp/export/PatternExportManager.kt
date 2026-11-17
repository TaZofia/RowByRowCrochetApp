package com.example.rowbyrowapp.export

import android.content.Context
import com.example.rowbyrowapp.ui.editor.model.PlacedElement

class PatternExportManager(private val appContext: Context) {

    fun exportToGallery(placedElements: List<PlacedElement>): Boolean {
        val bitmap = CrochetImageExporter.createPatternBitmap(
            context = appContext,
            placedElements = placedElements
        ) ?: return false

        return ImageSaver.saveBitmapToGallery(appContext, bitmap)
    }
}