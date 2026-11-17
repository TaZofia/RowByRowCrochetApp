package com.example.rowbyrowapp.export


import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.provider.MediaStore

object ImageSaver {

    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, title: String = "CrochetPattern"): Boolean {
        val filename = "${title}_${System.currentTimeMillis()}.png"
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/RowByRow")
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return false

        return try {
            resolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}