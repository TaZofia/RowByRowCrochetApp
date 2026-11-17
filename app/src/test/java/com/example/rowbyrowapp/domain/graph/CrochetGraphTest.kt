package com.example.rowbyrowapp.domain.graph
import com.example.rowbyrowapp.domain.model.StitchType
import com.example.rowbyrowapp.domain.model.StitchVertex
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class CrochetGraphTest {

    private lateinit var graph: CrochetGraph

    @BeforeEach
    fun setUp() {
        graph = CrochetGraph()
    }

    private fun createVertex(id: Int, round: Int = 1, isEnd: Boolean = false): StitchVertex {
        return StitchVertex(
            id = id,
            type = StitchType.SC,
            round = round,
            isStart = id == 1,
            isEnd = isEnd
        )
    }

    @Test
    fun `addVertex - should addVertex and initialize list of edges`() {
        val v1 = createVertex(1)

        graph.addVertex(v1)

        assertTrue(graph.vertices.contains(v1))
        assertTrue(graph.adjList.containsKey(v1.id))
        assertTrue(graph.adjList[v1.id]!!.isEmpty())
    }

    @Test
    fun `addEdge oraz getPrevious - should properly add edge and return previous edge`() {
        val v1 = createVertex(1)
        val v2 = createVertex(2)

        graph.addVertex(v1)
        graph.addVertex(v2)

        val edge = graph.addEdge(newVertex = v2, oldVertex = v1, edgeType = EdgeType.PREVIOUS)

        assertEquals(v2, edge.newVertex)
        assertEquals(v1, edge.oldVertex)
        assertEquals(1, graph.adjList[v2.id]?.size)
        assertEquals(v1, graph.getPrevious(v2))
        assertNull(graph.getPrevious(v1))
    }

    @Test
    fun `removeEdge - should remove only slip stitch edge`() {
        val v1 = createVertex(1)
        val v2 = createVertex(2)
        val v3 = createVertex(3)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)

        val previousEdge = graph.addEdge(newVertex = v2, oldVertex = v1, edgeType = EdgeType.PREVIOUS)
        val slipEdge = graph.addEdge(newVertex = v2, oldVertex = v3, edgeType = EdgeType.SLIP_STITCH)

        assertEquals(2, graph.adjList[v2.id]?.size)

        graph.removeEdge(slipEdge)

        val remainingEdges = graph.adjList[v2.id]!!
        assertEquals(1, remainingEdges.size)
        assertFalse(remainingEdges.contains(slipEdge))
        assertTrue(remainingEdges.contains(previousEdge))
        assertEquals(v1, graph.getPrevious(v2))
    }

    @Test
    fun `removeVertex - should remove vertex and its key to adjList`() {
        val v1 = createVertex(1)

        graph.addVertex(v1)
        graph.removeVertex(v1)

        assertFalse(graph.vertices.contains(v1))
        assertFalse(graph.adjList.containsKey(v1.id))
    }

    @Test
    fun `removeVertex - should remove edges from that vertex`() {
        val v1 = createVertex(1)
        val v2 = createVertex(2)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addEdge(newVertex = v2, oldVertex = v1, edgeType = EdgeType.PREVIOUS)

        graph.removeVertex(v2)

        assertFalse(graph.vertices.contains(v2))
        assertFalse(graph.adjList.containsKey(v2.id))
    }

    @Test
    fun `removeVertex - should remove edges to that vertex`() {
        val v1 = createVertex(1)
        val v2 = createVertex(2)
        val v3 = createVertex(3)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addVertex(v3)

        graph.addEdge(newVertex = v2, oldVertex = v1, edgeType = EdgeType.PREVIOUS)
        val slipEdge = graph.addEdge(newVertex = v3, oldVertex = v1, edgeType = EdgeType.SLIP_STITCH)

        graph.removeVertex(v1)

        val v3Edges = graph.adjList[v3.id]!!
        assertFalse(v3Edges.contains(slipEdge))
        assertTrue(v3Edges.isEmpty())

        assertTrue(graph.adjList[v2.id]!!.isEmpty())
        assertNull(graph.getPrevious(v2))
    }

    @Test
    fun `removeVertex - undo scenario`() {
        val v1 = createVertex(1)
        val v2 = createVertex(2)
        val v3 = createVertex(3)

        graph.addVertex(v1)
        graph.addVertex(v2)
        graph.addEdge(v2, v1, EdgeType.PREVIOUS)

        graph.addVertex(v3)
        graph.addEdge(v3, v2, EdgeType.PREVIOUS)

        assertEquals(v2, graph.getPrevious(v3))
        assertEquals(v1, graph.getPrevious(v2))

        graph.removeVertex(v3)

        assertEquals(2, graph.vertices.size)
        assertEquals(v1, graph.getPrevious(v2))
        assertNull(graph.adjList[v3.id])
    }

    @Test
    fun `findLastStitch - should throw exception if there is no vertex with flag isEnd = true `() {
        val v1 = createVertex(1, isEnd = false)
        graph.addVertex(v1)

        assertThrows<IllegalStateException> {
            graph.findLastStitch()
        }
    }
}