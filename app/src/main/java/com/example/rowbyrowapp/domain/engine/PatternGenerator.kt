package com.example.rowbyrowapp.domain.engine

import com.example.rowbyrowapp.domain.graph.CrochetGraph
import com.example.rowbyrowapp.domain.graph.countHoleDistance
import com.example.rowbyrowapp.domain.graph.countStitchDistance
import com.example.rowbyrowapp.domain.graph.findInsert
import com.example.rowbyrowapp.domain.graph.followPattern
import com.example.rowbyrowapp.domain.graph.targetForNewRound
import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex
import kotlin.collections.iterator


class PatternGenerator(private val graph: CrochetGraph, private val aliasGen: AliasGenerator) {

    private var lastInsertTarget: StitchVertex? = null


    private fun formatDistanceInfo(dist: Int): String {
        return when (dist) {
            0 -> "the same "
            1 -> "next "
            2 -> "2nd "
            3 -> "3rd "
            else -> "${dist}th "
        }
    }
    private fun formatDistanceInfoHole(dist: Int): String {
        return when (dist) {
            0 -> ""
            1 -> ""
            2 -> "2nd "
            3 -> "3rd "
            else -> "${dist}th "
        }
    }

    private fun makeStitchDescription(stitch : StitchVertex) : String {  // słowny opis, który potem będzie miał alias
        val insertTarget = graph.findInsert(stitch)
        if (insertTarget == null) {    // brak określonego miejsca wkłucia
            return stitch.type.toString()
        } else {
            val targetType = insertTarget.type
            if(targetType == StitchType.HOLE) {
                val dist = graph.countHoleDistance(insertTarget, lastInsertTarget)
                lastInsertTarget = insertTarget
                return stitch.type.toString() +  " in " + formatDistanceInfoHole(dist) + "hole"
            } else {
                val dist = graph.countStitchDistance(insertTarget, lastInsertTarget)
                lastInsertTarget = insertTarget
                return stitch.type.toString() + " in " + formatDistanceInfo(dist) + "stitch"
            }
        }
    }

    private fun groupStitchesByRound(): Map<Int, List<StitchVertex>> {
        return graph.followPattern()
            .asReversed()
            .groupBy { it.round }
    }

    fun makePattern(): Map<Int, String> {
        val groupedRounds = groupStitchesByRound()
        val resultMap = mutableMapOf<Int, String>()

        for((round, stitches) in groupedRounds) {
            lastInsertTarget = graph.targetForNewRound(stitches.first(), groupedRounds)
            val roundPattern = stitches.joinToString(separator = "") { stitch ->
                val description = makeStitchDescription(stitch)
                aliasGen.getAlias(description).toString()
            }
            resultMap[round] = roundPattern
        }
        return resultMap
    }
}