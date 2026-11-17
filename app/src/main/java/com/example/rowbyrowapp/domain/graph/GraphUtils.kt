package com.example.rowbyrowapp.domain.graph

import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex

// Wyszukiwanie określonych wierzchołków

fun CrochetGraph.findInsert(stitch: StitchVertex) : StitchVertex? {
    val edges = adjList[stitch.id] ?: return null
    val insertEdge = edges.firstOrNull { it.edgeType == EdgeType.INSERT } ?: return null
    return insertEdge.oldVertex
}

fun CrochetGraph.findLastStitch(): StitchVertex {
    return vertices.asReversed().firstOrNull { it.isEnd }   // asReversed() bo wtedy pewnie szybciej znajdziemy ostatnie oczko
        ?: throw IllegalStateException("[ERROR] No end vertex found")
}

fun CrochetGraph.targetForNewRound(stitch: StitchVertex, groupedRounds: Map<Int, List<StitchVertex>>): StitchVertex? {
    val prev = getPrevious(stitch) ?: return null
    val target = adjList[prev.id]
        ?.firstOrNull { it.edgeType == EdgeType.SLIP_STITCH }
        ?.oldVertex
    val previousRoundNum = stitch.round - 1
    return target ?: groupedRounds[previousRoundNum]?.firstOrNull()
}

// Logika przemieszczania się i odległości

fun CrochetGraph.followPattern() : MutableList<StitchVertex> {

    val listFromLastToFirst = mutableListOf<StitchVertex>()

    var currentStitch = findLastStitch()
    var previousStitch = getPrevious(currentStitch)

    listFromLastToFirst.add(currentStitch)

    while (previousStitch != null) {
        currentStitch = previousStitch
        previousStitch = getPrevious(currentStitch)
        listFromLastToFirst.add(currentStitch)
    }
    return listFromLastToFirst      // w kolejności od ostatniego do pierwszego oczka
}

fun CrochetGraph.countStitchDistance(current : StitchVertex, lastInsertPoint : StitchVertex?) : Int {
    var currentStitch : StitchVertex? = current
    var dist = 0

    if(lastInsertPoint == null) {   // jeżeli nie ma punktu odniesienia to automatycznie next
        return 1
    }

    while (currentStitch != null && currentStitch.id != lastInsertPoint.id) {
        dist++
        currentStitch = getPrevious(currentStitch)

        if(currentStitch?.type == StitchType.CH) {
            val hole = findHoleByChain(currentStitch)
            if(hole?.id == lastInsertPoint.id) break
        }
    }
    return dist
}

fun CrochetGraph.countHoleDistance(current : StitchVertex, lastInsertPoint : StitchVertex?) : Int {
    if(lastInsertPoint == null) return 1

    val chains = getChainsForHole(current)
    var dist = 0
    var lastHole : StitchVertex? = null

    var currentStitch : StitchVertex? = chains.last()

    while (currentStitch != null && currentStitch.id != lastInsertPoint.id) {

        if(currentStitch.type == StitchType.CH) {
            val hole = findHoleByChain(currentStitch)
            if(hole?.id == lastInsertPoint.id) break
            if(hole?.id != lastHole?.id) {
                dist++
                lastHole = hole
            }
        }
        currentStitch = getPrevious(currentStitch)

    }
    return dist
}


// Zarządzanie chain-space, czyli wierzchołkami typu HOLE

fun CrochetGraph.findHoleByChain(stitch: StitchVertex): StitchVertex? {
    if (stitch.type != StitchType.CH) return null

    // Przeszukujemy wierzchołki i znajdujemy HOLE, który w swojej adjList
    // posiada krawędź SURROUNDING prowadzącą do podanego oczka 'stitch'
    return vertices.firstOrNull { vertex ->
        vertex.type == StitchType.HOLE &&
                adjList[vertex.id]?.any { edge ->
                    edge.edgeType == EdgeType.SURROUNDING && edge.oldVertex.id == stitch.id
                } == true
    }
}

fun CrochetGraph.getChainsForHole(hole : StitchVertex): MutableList<StitchVertex> {
    if(hole.type != StitchType.HOLE) error("Argument is not a hole")
    val adjEdges = adjList[hole.id] ?: error("Hole must have a chain")
    val chains = mutableListOf<StitchVertex>()

    for (edge in adjEdges) {
        if(edge.edgeType == EdgeType.SURROUNDING) {
            chains.add(edge.oldVertex)
        }
    }
    return chains
}
