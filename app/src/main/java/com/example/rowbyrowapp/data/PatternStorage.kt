package com.example.rowbyrowapp.data

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


object PatternStorage {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private fun getPatternsDir(context: Context): File {
        return File(context.filesDir, "patterns").apply {
            if (!exists()) mkdirs()
        }
    }

    fun savePattern(context: Context, pattern: SavedPattern) {
        val safeFileName = pattern.name
            .trim()
            .ifEmpty { "pattern_${System.currentTimeMillis()}" }
            .replace(Regex("[^a-zA-Z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ _-]"), "_")

        val file = File(getPatternsDir(context), "$safeFileName.json")
        file.writeText(json.encodeToString(pattern))
    }

    fun getAllPatterns(context: Context): List<SavedPattern> {
        return getPatternsDir(context)
            .listFiles { file -> file.extension == "json" }
            ?.sortedByDescending { it.lastModified() }
            ?.mapNotNull { file ->
                runCatching {
                    json.decodeFromString<SavedPattern>(file.readText())
                }.getOrNull()
            } ?: emptyList()
    }

    fun deletePattern(context: Context, patternName: String) {
        val safeFileName = patternName
            .trim()
            .replace(Regex("[^a-zA-Z0-9ąćęłńóśźżĄĆĘŁŃÓŚŹŻ _-]"), "_")
        File(getPatternsDir(context), "$safeFileName.json").delete()
    }
}