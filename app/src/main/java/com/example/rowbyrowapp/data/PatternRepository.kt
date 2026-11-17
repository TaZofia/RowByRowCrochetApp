package com.example.rowbyrowapp.data

import android.content.Context
import com.example.rowbyrowapp.domain.graph.CrochetGraph
import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.graph.GraphBuilder
import com.example.rowbyrowapp.domain.model.StitchVertex
import com.example.rowbyrowapp.ui.editor.model.PlacedElement
import com.example.rowbyrowapp.ui.editor.model.PlacedSlipStitch
import com.example.rowbyrowapp.ui.editor.model.PlacedStitch
import com.example.rowbyrowapp.ui.editor.model.availableStitches
import kotlin.collections.get

data class RestoredPattern(
    val name: String,
    val currentRound: Int,
    val elements: List<PlacedElement>,
    val lastPlacedVertex: StitchVertex?
)

class PatternRepository(private val appContext: Context) {

    fun getAllPatterns(): List<SavedPattern> = PatternStorage.getAllPatterns(appContext)

    fun deletePattern(name: String) {
        PatternStorage.deletePattern(appContext, name)
    }

    fun savePattern(
        name: String,
        currentRound: Int,
        lastPlacedVertexId: Int?,
        graph: CrochetGraph,
        placedElements: List<PlacedElement>
    ) {
        val vertices = graph.vertices.map { v ->
            SavedVertex(id = v.id, type = v.type, round = v.round, isEnd = v.isEnd)
        }

        // Pominięcie EdgeType.INSERT, bo są przeliczane automatycznie po wczytaniu
        val edges = graph.adjList.values
            .flatten()
            .filter { it.edgeType != EdgeType.INSERT }
            .distinct()
            .map { edge ->
                SavedEdge(
                    newId = edge.newVertex.id,
                    oldId = edge.oldVertex.id,
                    type = edge.edgeType
                )
            }

        val elements = placedElements.map { element ->
            when (element) {
                is PlacedStitch -> SavedElement(
                    isSlipStitch = false,
                    vertexId = element.vertex.id,
                    gridX = element.gridX,
                    gridY = element.gridY,
                    rotation = element.rotation
                )
                is PlacedSlipStitch -> SavedElement(
                    isSlipStitch = true,
                    fromVertexId = element.edge.newVertex.id,
                    toVertexId = element.edge.oldVertex.id,
                    gridX = element.gridX,
                    gridY = element.gridY,
                    round = element.round
                )
            }
        }

        val patternToSave = SavedPattern(
            name = name,
            currentRound = currentRound,
            lastPlacedVertexId = lastPlacedVertexId,
            vertices = vertices,
            edges = edges,
            elements = elements
        )

        PatternStorage.savePattern(appContext, patternToSave)
    }

    fun restorePattern(
        pattern: SavedPattern,
        graphBuilder: GraphBuilder,
        graph: CrochetGraph
    ): RestoredPattern {
        graphBuilder.restore(
            savedVertices = pattern.vertices.map {
                StitchVertex(id = it.id, type = it.type, round = it.round, isEnd = it.isEnd)
            },
            savedEdges = pattern.edges.map { Triple(it.newId, it.oldId, it.type) }
        )

        val vertexMap = graph.vertices.associateBy { it.id }
        val slipStitchItem = availableStitches.first { it.isSlipStitch }

        // odtworzenie elementów na siatce UI
        val restoredElements = pattern.elements.mapNotNull { el ->
            if (!el.isSlipStitch) {
                val vertex = vertexMap[el.vertexId] ?: return@mapNotNull null
                val stitchItem = availableStitches.firstOrNull { it.type == vertex.type }
                    ?: return@mapNotNull null

                PlacedStitch(
                    vertex = vertex,
                    stitchItem = stitchItem,
                    gridX = el.gridX,
                    gridY = el.gridY,
                    rotation = el.rotation
                )
            } else {
                val edge = graph.adjList[el.fromVertexId]?.firstOrNull {
                    it.edgeType == EdgeType.SLIP_STITCH && it.oldVertex.id == el.toVertexId
                } ?: return@mapNotNull null

                PlacedSlipStitch(
                    edge = edge,
                    stitchItem = slipStitchItem,
                    gridX = el.gridX,
                    gridY = el.gridY,
                    round = el.round
                )
            }
        }
        // przywrócenie ostatnio postawionego oczka (żeby kolejne oczka łączyły się krawędzią PREVIOUS)
        val lastVertex = pattern.lastPlacedVertexId?.let { vertexMap[it] }
            ?: restoredElements.filterIsInstance<PlacedStitch>().lastOrNull()?.vertex

        return RestoredPattern(
            name = pattern.name,
            currentRound = pattern.currentRound,
            elements = restoredElements,
            lastPlacedVertex = lastVertex
        )
    }
}