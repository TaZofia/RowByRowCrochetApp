package com.example.rowbyrowapp.domain.graph

import com.example.rowbyrowapp.domain.model.StitchVertex
import kotlin.collections.any

class CrochetGraph {
    val vertices = mutableListOf<StitchVertex>()
    val adjList = mutableMapOf<Int, MutableList<Edge>>()   // id wierzchołka i jego lista krawędzi


    fun addVertex(vertex: StitchVertex) {
        adjList[vertex.id] = mutableListOf()
        vertices.add(vertex)
    }
    fun addEdge(newVertex: StitchVertex, oldVertex: StitchVertex, edgeType: EdgeType) : Edge {
        val edge = Edge(newVertex, oldVertex, edgeType)
        adjList[newVertex.id]?.add(edge)
        return edge
    }

    fun getPrevious(stitch : StitchVertex) : StitchVertex? {
        val listOfAdjacent = adjList[stitch.id] ?: return null
        for (adj in listOfAdjacent) {
            if (adj.edgeType == EdgeType.PREVIOUS)
                return adj.oldVertex
        }
        return null
    }

    fun removeEdge(edge: Edge) {
        adjList[edge.newVertex.id]?.remove(edge)
    }

    fun removeVertex(vertex: StitchVertex) {
        vertices.remove(vertex)
        adjList.remove(vertex.id)

        // czyszczenie również wszystkich krawędzi, które do niego wchodzą
        adjList.values.forEach { edges ->
            edges.removeAll { edge ->
                edge.oldVertex.id == vertex.id || edge.newVertex.id == vertex.id
            }
        }
    }

    fun hasThisEdgeBetween(v1: StitchVertex, v2: StitchVertex, typeOfEdge: EdgeType) : Boolean {
        val hasForward = adjList[v1.id]?.any { it.oldVertex.id == v2.id && it.edgeType == typeOfEdge  } == true
        val hasBackward = adjList[v2.id]?.any { it.oldVertex.id == v1.id && it.edgeType == typeOfEdge } == true
        return hasForward || hasBackward
    }
}