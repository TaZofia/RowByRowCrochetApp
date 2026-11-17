package com.example.rowbyrowapp.domain.engine

import com.example.rowbyrowapp.domain.formatting.CrochetPatternFormatter
import com.example.rowbyrowapp.domain.graph.CrochetGraph
import com.example.rowbyrowapp.domain.folding.foldRepetitions
import com.example.rowbyrowapp.domain.sequitur.runSequiturAsMap
import kotlin.collections.iterator

class PatternTransformationEngine (graph: CrochetGraph) {

    private val aliasGen = AliasGenerator()
    private val patternGenerator = PatternGenerator(graph, aliasGen)

    fun fromGraphToReadablePattern(): String{
        val rawStringsPerRound = patternGenerator.makePattern()
        val sb = StringBuilder()

        for((round, rawString) in rawStringsPerRound) {
            sb.append(round).append(": ")
            val grammar = runSequiturAsMap(rawString)
            val folded = foldRepetitions(grammar)

            val formatter = CrochetPatternFormatter(aliasGen)
            val readableForm = formatter.format(folded.toMutableList())
            sb.append(readableForm).append("\n")
        }
        return sb.toString().ifEmpty { "Unable to generate instruction" }
    }
}


