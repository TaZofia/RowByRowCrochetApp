package com.example.rowbyrowapp.ui.editor

import com.example.rowbyrowapp.domain.graph.CrochetGraph
import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex
import com.example.rowbyrowapp.ui.editor.model.ConnectionLine
import com.example.rowbyrowapp.ui.editor.model.ConnectionType
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import kotlin.collections.filter
import kotlin.collections.firstOrNull
import kotlin.collections.removeAll
import kotlin.math.hypot

// klasa potrzebna w trakcie zmieniania położenia oczka na siatce
// potrzebne jest ponowne przeliczenie miejsca wkłucia
class InsertEdgeCalculator {

    fun recalculate(
        graph: CrochetGraph,
        elements: List<PlacedElement>
    ): List<ConnectionLine> {
        val stitches = elements.filterIsInstance<PlacedStitch>()
        val stitchesMap = stitches.associateBy { it.vertex.id }

        // Obliczanie pozycji dziur
        val holePositions = mutableMapOf<Int, Pair<Float, Float>>()
        graph.vertices.filter { it.type == StitchType.HOLE }.forEach { hole ->
            val surroundings = graph.adjList[hole.id]
                ?.filter { it.edgeType == EdgeType.SURROUNDING }
                ?.mapNotNull { stitchesMap[it.oldVertex.id] } ?: emptyList()

            if (surroundings.isNotEmpty()) {
                val avgX = surroundings.map { it.gridX }.average().toFloat()
                val avgY = surroundings.map { it.gridY }.average().toFloat()
                holePositions[hole.id] = Pair(avgX, avgY)
            }
        }

        // szukanie najlepszego, najbliższego miejsca wkłucia (INSERT)
        stitches.forEach { placed ->
            val v = placed.vertex
            // Tylko zwykłe oczka (nie CH, HOLE) potrzebują krawędzi INSERT
            if (v.type != StitchType.CH  && v.type != StitchType.HOLE) {
                graph.adjList[v.id]?.removeAll { it.edgeType == EdgeType.INSERT }

                val candidates = mutableListOf<TargetCandidate>()

                // dodanie zwykłych oczek jako cele
                stitches.forEach { other ->
                    if (other.vertex.id != v.id &&
                        other.vertex.type != StitchType.CH &&
                        other.vertex.type != StitchType.HOLE
                    ) {
                        candidates.add(TargetCandidate(other.vertex, other.gridX, other.gridY))
                    }
                }

                // dodanie dziur jako cele
                holePositions.forEach { (holeId, pos) ->
                    graph.vertices.find { it.id == holeId }?.let { holeVertex ->
                        candidates.add(TargetCandidate(holeVertex, pos.first, pos.second))
                    }
                }

                // Wybierz najbliższy cel (priorytetowo z wcześniejszych rund)
                val target = candidates
                    .filter { candidate ->
                        if (candidate.vertex.type == StitchType.HOLE) {
                            candidate.vertex.round < v.round // dziura dostępna tylko dla wyższych rund
                        } else {
                            candidate.vertex.round < v.round
                        }
                    }
                    .minByOrNull { hypot(it.x - placed.gridX, it.y - placed.gridY) }
                    ?: candidates
                        .filter { it.vertex.type != StitchType.HOLE && it.vertex.id != v.id }
                        .minByOrNull { hypot(it.x - placed.gridX, it.y - placed.gridY) }


                if (target != null) {
                    graph.addEdge(newVertex = v, oldVertex = target.vertex, edgeType = EdgeType.INSERT)
                }
            }
        }

        // generowanie linii inert do UI
        val generatedInsertLines = mutableListOf<ConnectionLine>()

        stitches.forEach { placed ->
            val insertEdge = graph.adjList[placed.vertex.id]?.firstOrNull { it.edgeType == EdgeType.INSERT }
            if (insertEdge != null) {
                val targetVertex = insertEdge.oldVertex
                val targetPos = if (targetVertex.type == StitchType.HOLE) {
                    holePositions[targetVertex.id]
                } else {
                    stitchesMap[targetVertex.id]?.let { Pair(it.gridX, it.gridY) }
                }

                if (targetPos != null) {
                    generatedInsertLines.add(
                        ConnectionLine(
                            startX = placed.gridX,
                            startY = placed.gridY,
                            endX = targetPos.first,
                            endY = targetPos.second,
                            type = ConnectionType.INSERT
                        )
                    )
                }
            }
        }
        return generatedInsertLines
    }
    private data class TargetCandidate(
        val vertex: StitchVertex,
        val x: Float,
        val y: Float
    )
}