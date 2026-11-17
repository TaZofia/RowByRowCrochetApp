package com.example.rowbyrowapp.domain.engine

import com.example.rowbyrowapp.domain.graph.CrochetGraph
import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PatternGeneratorTest {

    private lateinit var graph: CrochetGraph
    private lateinit var aliasGenerator: AliasGenerator
    private lateinit var patternGenerator: PatternGenerator

    @BeforeEach
    fun setUp() {
        graph = CrochetGraph()
        aliasGenerator = AliasGenerator()
        patternGenerator = PatternGenerator(graph, aliasGenerator)
    }

    @Test
    fun `should generate pattern for basic round without insert targets`() {
        val v1 = StitchVertex(id = 1, type = StitchType.CH, round = 1)
        val v2 = StitchVertex(id = 2, type = StitchType.CH, round = 1, isEnd = true)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addEdge(v2, v1, EdgeType.PREVIOUS)

        val pattern = patternGenerator.makePattern()

        assertEquals(1, pattern.size)   // mapa posiada tylko jedną rundę
        assertEquals("AA", pattern[1])  // pattern na pierwszą rundę
        assertEquals("CH", aliasGenerator.getDescription('A'))
    }

    @Test
    fun `should generate pattern with stitch inserted into hole`() {
        val hole = StitchVertex(id = 1, type = StitchType.HOLE, round = 1)
        val dc = StitchVertex(id = 2, type = StitchType.DC, round = 2, isEnd = true)

        graph.addVertex(hole)
        graph.addVertex(dc)
        graph.addEdge(dc, hole, EdgeType.INSERT)

        val pattern = patternGenerator.makePattern()

        assertEquals("A", pattern[2])  //alias dla dc w rundzie drugiej - pierwsza to hole
        assertEquals("DC in hole", aliasGenerator.getDescription('A'))
    }

    @Test
    fun `should correctly format distance info for consecutive stitches`() {
        // Round 1: Target stitches (v1 -> v2)
        val r1v1 = StitchVertex(id = 1, type = StitchType.SC, round = 1)
        val r1v2 = StitchVertex(id = 2, type = StitchType.SC, round = 1)

        // Round 2: Stitches inserting into Round 1 (v3 -> v4 -> v5)
        val r2v3 = StitchVertex(id = 3, type = StitchType.DC, round = 2)
        val r2v4 = StitchVertex(id = 4, type = StitchType.DC, round = 2)
        val r2v5 = StitchVertex(id = 5, type = StitchType.DC, round = 2, isEnd = true)

        graph.addVertex(r1v1)
        graph.addVertex(r1v2)
        graph.addVertex(r2v3)
        graph.addVertex(r2v4)
        graph.addVertex(r2v5)

        // Graph chain: r2v5 -> r2v4 -> r2v3 -> r1v2 -> r1v1
        graph.addEdge(r1v2, r1v1, EdgeType.PREVIOUS)
        graph.addEdge(r2v3, r1v2, EdgeType.PREVIOUS)
        graph.addEdge(r2v4, r2v3, EdgeType.PREVIOUS)
        graph.addEdge(r2v5, r2v4, EdgeType.PREVIOUS)

        // Insert targets: r2v3 inserts into r1v1, r2v4 inserts into r1v2
        graph.addEdge(r2v3, r1v1, EdgeType.INSERT)
        graph.addEdge(r2v4, r1v2, EdgeType.INSERT)

        // SLIP_STITCH defines the initial lastInsertTarget for Round 2
        graph.addEdge(r2v3, r1v1, EdgeType.SLIP_STITCH)

        val pattern = patternGenerator.makePattern()

        // "SC" -> A
        // rnd 1 -> AA
        //
        assertEquals("DC in the same stitch", aliasGenerator.getDescription('B'))
        assertEquals("DC in next stitch", aliasGenerator.getDescription('C'))
        assertEquals("DC", aliasGenerator.getDescription('D'))
        assertEquals("BCD", pattern[2])

    }
}