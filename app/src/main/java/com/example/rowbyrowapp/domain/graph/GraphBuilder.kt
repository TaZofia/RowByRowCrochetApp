package com.example.rowbyrowapp.domain.graph

import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex


class GraphBuilder {

    private val graph = CrochetGraph()

    private var idCounter = 1
    private var flagChainSpace = false
    private var currentChainSpaceNode: StitchVertex? = null

    fun addStitch(type: StitchType, round: Int): StitchVertex {
        val vertex = StitchVertex(idCounter++, type, round)
        graph.addVertex(vertex)
        return vertex
    }

    fun connectStitches(newVertex: StitchVertex, oldVertex: StitchVertex, edgeType: EdgeType): Edge {
        val edge = graph.addEdge(newVertex, oldVertex, edgeType)

        if(edgeType == EdgeType.PREVIOUS) {
            if (newVertex.type == StitchType.CH) {
                handleChainSpaceOccurrence(newVertex)
            } else {
                closeChainSpace()
            }
        } else if (edgeType == EdgeType.SLIP_STITCH) {
            // zamknięcie okręgu oczkiem ścisłym też kończy tworzenie dziury
            closeChainSpace()
        }
        return edge
    }

    // przyda się przy gui - zakładam że mogę edytować tylko ostatnią rundę
    // TO DO insert ogarnąć
    fun putBetween(vertex1 : StitchVertex, vertex2 : StitchVertex, newVertex: StitchVertex) {

        var previousVertex = vertex1  // referencja
        var nextVertex = vertex2
        if (graph.getPrevious(vertex1)?.id == vertex2.id) {
            previousVertex = vertex2
            nextVertex = vertex1
        }

        graph.adjList[nextVertex.id]?.removeAll { edge ->
            edge.edgeType == EdgeType.PREVIOUS && edge.oldVertex.id == previousVertex.id
        }

        connectStitches(newVertex, previousVertex, EdgeType.PREVIOUS)
        connectStitches(nextVertex, newVertex, EdgeType.PREVIOUS)

    }

    private fun handleChainSpaceOccurrence(vertex: StitchVertex) {
        if(!flagChainSpace || currentChainSpaceNode?.round != vertex.round) {  // nowa dziura jeśli flaga była false, albo zmieniła się runda
            flagChainSpace = true
            val newChainSpace = addStitch(StitchType.HOLE, vertex.round)           // pierwsze oczko łańcuszka zostało dodane, więc odpowiadający obiekt Space też musi powstać
            currentChainSpaceNode = newChainSpace
            graph.addEdge(newChainSpace, vertex, EdgeType.SURROUNDING)       // skierowanie od węzła dziury do węzłów, które go tworzą
        } else {  // flaga już była zaznaczona, a więc jest to nie pierwsze oczko
            currentChainSpaceNode?.let { existingSpace ->
                graph.addEdge(existingSpace, vertex, EdgeType.SURROUNDING)   // skierowanie od węzła dziury do węzłów, które go tworzą
            }
        }
    }
    fun closeChainSpace() {     // czyszczenie
        flagChainSpace = false
        currentChainSpaceNode = null
    }

    // od removeVertex z CrochetGraph różni się tym, że bierze pod uwagę powstałe dziury
    fun removeStitch(vertex: StitchVertex) {
        // podczas usuwania CH sprawdzanie czy nie została dziura
        val associatedHoles = graph.vertices.filter { it.type == StitchType.HOLE }.toList()

        graph.removeVertex(vertex)

        for (hole in associatedHoles) {
            val remainingSurroundings = graph.getChainsForHole(hole)
            if (remainingSurroundings.isEmpty()) {
                graph.removeVertex(hole)
                if (currentChainSpaceNode?.id == hole.id) {
                    currentChainSpaceNode = null
                    flagChainSpace = false
                }
            }
        }
    }

    fun build() : CrochetGraph = graph

    // W GraphBuilder.kt (czysty Kotlin, zero zależności od bibliotek zewnętrznych)
    fun restore(
        savedVertices: List<StitchVertex>,
        savedEdges: List<Triple<Int, Int, EdgeType>> // (newVertexId, oldVertexId, edgeType)
    ) {
        // 1. Wyczyść obecny stan
        graph.vertices.toList().forEach { graph.removeVertex(it) }
        closeChainSpace()

        // 2. Dodaj wierzchołki i zapamiętaj ich pojedyncze instancje w mapie
        val vertexMap = mutableMapOf<Int, StitchVertex>()
        savedVertices.forEach { v ->
            val instance = StitchVertex(id = v.id, type = v.type, round = v.round, isEnd = v.isEnd)
            graph.addVertex(instance)
            vertexMap[instance.id] = instance
        }

        // 3. Odtwórz krawędzie na tych samych instancjach wierzchołków
        savedEdges.forEach { (newId, oldId, edgeType) ->
            val newV = vertexMap[newId]
            val oldV = vertexMap[oldId]
            if (newV != null && oldV != null) {
                graph.addEdge(newV, oldV, edgeType)
            }
        }

        // 4. Ustaw licznik ID powyżej najwyższego istniejącego ID, żeby nowe oczka nie nadpisały starych
        idCounter = (savedVertices.maxOfOrNull { it.id } ?: 0) + 1

        // 5. Jeśli ostatnie oczko w grafie to niezamknięty CH, przywróć stan dziury (chain space)
        val lastStitch = savedVertices.lastOrNull { it.type != StitchType.HOLE }
        if (lastStitch?.type == StitchType.CH) {
            val openHole = savedVertices.lastOrNull { it.type == StitchType.HOLE && it.round == lastStitch.round }
            if (openHole != null) {
                flagChainSpace = true
                currentChainSpaceNode = vertexMap[openHole.id]
            }
        }
    }
}