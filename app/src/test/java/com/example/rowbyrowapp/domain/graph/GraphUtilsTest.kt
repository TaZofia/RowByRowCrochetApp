package com.example.rowbyrowapp.domain.graph

import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GraphUtilsTest {

    private lateinit var graph: CrochetGraph

    @BeforeEach
    fun setUp() {
        graph = CrochetGraph()
    }

    // Testy funkcji szukających

    @Test
    fun `findInsert should return target vertex when INSERT edge exists`() {
        val source = StitchVertex(id = 1, type = StitchType.DC, round = 2)
        val target = StitchVertex(id = 2, type = StitchType.SC, round = 1)

        graph.addVertex(source)
        graph.addVertex(target)
        graph.addEdge(source, target, EdgeType.INSERT)

        val result = graph.findInsert(source)

        assertNotNull(result)
        assertEquals(target.id, result?.id)
    }

    @Test
    fun `findInsert should return null when INSERT edge does not exist`() {
        val source = StitchVertex(id = 1, type = StitchType.CH, round = 1)
        graph.addVertex(source)

        val result = graph.findInsert(source)

        assertNull(result)
    }

    @Test
    fun `findLastStitch should return vertex marked as isEnd`() {
        val v1 = StitchVertex(id = 1, type = StitchType.CH, round = 1, isEnd = false)
        val v2 = StitchVertex(id = 2, type = StitchType.CH, round = 1, isEnd = true)

        graph.addVertex(v1)
        graph.addVertex(v2)

        val result = graph.findLastStitch()

        assertEquals(v2.id, result.id)
    }

    @Test
    fun `findLastStitch should throw exception when no end vertex exists`() {
        val v1 = StitchVertex(id = 1, type = StitchType.CH, round = 1, isEnd = false)
        graph.addVertex(v1)

        assertThrows<IllegalStateException> {
            graph.findLastStitch()
        }
    }

    @Test
    fun `targetForNewRound should return SLIP_STITCH target when available`() {
        val startRound2 = StitchVertex(id = 3, type = StitchType.DC, round = 2)
        val endRound1 = StitchVertex(id = 2, type = StitchType.DC, round = 1)
        val slipTarget = StitchVertex(id = 1, type = StitchType.DC, round = 1)
        val dummyTarget = StitchVertex(id = 99, type = StitchType.SC, round = 1)

        graph.addVertex(startRound2)
        graph.addVertex(endRound1)
        graph.addVertex(slipTarget)

        graph.addEdge(startRound2, endRound1, EdgeType.PREVIOUS)
        graph.addEdge(endRound1, slipTarget, EdgeType.SLIP_STITCH)

        // dummy po to, żeby upewnić się, że na pewno wygrywa SLIP-STITCH
        val groupedRounds = mapOf(
            1 to listOf(dummyTarget, slipTarget),
            2 to listOf(startRound2)
        )

        val result = graph.targetForNewRound(startRound2, groupedRounds)

        assertNotNull(result)
        assertEquals(slipTarget.id, result?.id)
    }

    @Test
    fun `targetForNewRound should fallback to first stitch of previous round when SLIP_STITCH is missing`() {
        val startRound2 = StitchVertex(id = 3, type = StitchType.DC, round = 2)
        val endRound1 = StitchVertex(id = 2, type = StitchType.SC, round = 1)
        val firstInRound1 = StitchVertex(id = 1, type = StitchType.CH, round = 1)

        graph.addVertex(startRound2)
        graph.addVertex(endRound1)
        graph.addVertex(firstInRound1)

        // Połączenie PREVIOUS, ale BRAK krawędzi SLIP_STITCH
        graph.addEdge(startRound2, endRound1, EdgeType.PREVIOUS)

        val groupedRounds = mapOf(
            1 to listOf(firstInRound1, endRound1),
            2 to listOf(startRound2)
        )

        val result = graph.targetForNewRound(startRound2, groupedRounds)

        assertNotNull(result)
        assertEquals(firstInRound1.id, result?.id)
    }

    @Test
    fun `targetForNewRound should return null when stitch has no previous vertex`() {
        val isolatedStitch = StitchVertex(id = 1, type = StitchType.CH, round = 1)
        graph.addVertex(isolatedStitch)

        val groupedRounds = mapOf(1 to listOf(isolatedStitch))

        val result = graph.targetForNewRound(isolatedStitch, groupedRounds)

        assertNull(result)
    }

    // Testy przemieszczania się i zliczania odległości

    @Test
    fun `followPattern should return vertices backwards from end to start`() {
        val v1 = StitchVertex(id = 1, type = StitchType.CH, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.SC, round = 1)
        val v3 = StitchVertex(id = 3, type = StitchType.DC, round = 1, isEnd = true)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)

        graph.addEdge(v2, v1, EdgeType.PREVIOUS)
        graph.addEdge(v3, v2, EdgeType.PREVIOUS)

        val pattern = graph.followPattern()

        assertEquals(3, pattern.size)
        assertEquals(listOf(v3.id, v2.id, v1.id), pattern.map { it.id })
    }

    @Test
    fun `countStitchDistance should return 1 when last target is null`() {
        val current = StitchVertex(id = 1, type = StitchType.SC, round = 2)
        graph.addVertex(current)

        val distance = graph.countStitchDistance(current, null)

        assertEquals(1, distance)
    }

    @Test
    fun `countStitchDistance should calculate correct distance traversing PREVIOUS edges`() {
        val v1 = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.SC, round = 1)
        val v3 = StitchVertex(id = 3, type = StitchType.SC, round = 1)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)

        graph.addEdge(v3, v2, EdgeType.PREVIOUS)
        graph.addEdge(v2, v1, EdgeType.PREVIOUS)

        val distance = graph.countStitchDistance(v3, v1)

        assertEquals(2, distance)
    }
    @Test
    fun `countStitchDistance should return 0`() {
        val v1 = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.SC, round = 2)
        val v3 = StitchVertex(id = 3, type = StitchType.SC, round = 2)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)

        graph.addEdge(v3, v2, EdgeType.PREVIOUS)
        graph.addEdge(v2, v1, EdgeType.PREVIOUS)
        graph.addEdge(v3, v1, EdgeType.INSERT)
        graph.addEdge(v2, v1, EdgeType.INSERT)

        val distance = graph.countStitchDistance(v1, v1)
        assertEquals(0, distance)
    }

    @Test
    fun `countHoleDistance should return 1`() {
        val v1 = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.SC, round = 1)
        val v3 = StitchVertex(id = 3, type = StitchType.CH, round = 1)
        val hole = StitchVertex(id = 4, type = StitchType.HOLE, round = 1)
        val v4 = StitchVertex(id = 5, type = StitchType.CH, round = 1)
        val v5 = StitchVertex(id = 6, type = StitchType.SC, round = 1)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)
        graph.addVertex(v4)
        graph.addVertex(v5)
        graph.addVertex(hole)

        graph.addEdge(v2, v1, EdgeType.PREVIOUS)
        graph.addEdge(v3, v2, EdgeType.PREVIOUS)
        graph.addEdge(v4, v3, EdgeType.PREVIOUS)
        graph.addEdge(v5, v4, EdgeType.PREVIOUS)

        graph.addEdge(hole, v3, EdgeType.SURROUNDING)
        graph.addEdge(hole, v4, EdgeType.SURROUNDING)


        val dist = graph.countHoleDistance(hole, v1)
        assertEquals(1, dist)
    }

    @Test
    fun `countHoleDistance should return 2` () {
        val v1 = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.SC, round = 1)
        val v3 = StitchVertex(id = 3, type = StitchType.CH, round = 1)
        val hole1 = StitchVertex(id = 4, type = StitchType.HOLE, round = 1)
        val v4 = StitchVertex(id = 5, type = StitchType.CH, round = 1)
        val v5 = StitchVertex(id = 6, type = StitchType.SC, round = 1)
        val v6 = StitchVertex(id = 7, type = StitchType.CH, round = 1)
        val hole2 = StitchVertex(id = 8, type = StitchType.HOLE, round = 1)
        val v7 = StitchVertex(id = 9, type = StitchType.CH, round = 1)
        val v8 = StitchVertex(id = 10, type = StitchType.SC, round = 1)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)
        graph.addVertex(hole1)
        graph.addVertex(v4)
        graph.addVertex(v5)
        graph.addVertex(v6)
        graph.addVertex(hole2)
        graph.addVertex(v7)
        graph.addVertex(v8)

        graph.addEdge(v2, v1, EdgeType.PREVIOUS)
        graph.addEdge(v3, v2, EdgeType.PREVIOUS)
        graph.addEdge(v4, v3, EdgeType.PREVIOUS)
        graph.addEdge(v5, v4, EdgeType.PREVIOUS)
        graph.addEdge(v6, v5, EdgeType.PREVIOUS)
        graph.addEdge(v7, v6, EdgeType.PREVIOUS)
        graph.addEdge(v8, v7, EdgeType.PREVIOUS)
        graph.addEdge(hole1, v3, EdgeType.SURROUNDING)
        graph.addEdge(hole1, v4, EdgeType.SURROUNDING)
        graph.addEdge(hole2, v6, EdgeType.SURROUNDING)
        graph.addEdge(hole2, v7, EdgeType.SURROUNDING)

        val dist = graph.countHoleDistance(hole2, v1)
        assertEquals(2, dist)
    }

    @Test
    fun `countHoleDistance should return 0`() {
        val v1 = StitchVertex(id = 3, type = StitchType.CH, round = 1)
        val hole = StitchVertex(id = 4, type = StitchType.HOLE, round = 1)
        val v2 = StitchVertex(id = 5, type = StitchType.CH, round = 1)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(hole)

        graph.addEdge(v2, v1, EdgeType.PREVIOUS)

        graph.addEdge(hole, v1, EdgeType.SURROUNDING)
        graph.addEdge(hole, v2, EdgeType.SURROUNDING)


        val dist = graph.countHoleDistance(hole, hole)
        assertEquals(0, dist)
    }

    @Test
    fun `findHoleByChain should return HOLE vertex connected via SURROUNDING edge`() {
        val chain = StitchVertex(id = 1, type = StitchType.CH, round = 1)
        val hole = StitchVertex(id = 2, type = StitchType.HOLE, round = 1)

        graph.addVertex(chain)
        graph.addVertex(hole)
        graph.addEdge(hole, chain, EdgeType.SURROUNDING)

        val result = graph.findHoleByChain(chain)

        assertNotNull(result)
        assertEquals(hole.id, result?.id)
    }

    @Test
    fun `findHoleByChain should return null if stitch is not CH type`() {
        val sc = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        graph.addVertex(sc)

        val result = graph.findHoleByChain(sc)

        assertNull(result)
    }

    @Test
    fun `getChainsForHole should return all CH vertices surrounded by hole`() {
        val hole = StitchVertex(id = 1, type = StitchType.HOLE, round = 1)
        val ch1 = StitchVertex(id = 2, type = StitchType.CH, round = 1)
        val ch2 = StitchVertex(id = 3, type = StitchType.CH, round = 1)

        graph.addVertex(hole)
        graph.addVertex(ch1)
        graph.addVertex(ch2)

        graph.addEdge(hole, ch1, EdgeType.SURROUNDING)
        graph.addEdge(hole, ch2, EdgeType.SURROUNDING)

        val chains = graph.getChainsForHole(hole)

        assertEquals(2, chains.size)
        assertTrue(chains.map { it.id }.containsAll(listOf(2, 3)))
    }

    @Test
    fun `getChainsForHole should throw error when provided vertex is not HOLE`() {
        val sc = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        graph.addVertex(sc)

        assertThrows<IllegalStateException> {
            graph.getChainsForHole(sc)
        }
    }
}