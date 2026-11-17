package com.example.rowbyrowapp.data

import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.model.StitchType
import kotlinx.serialization.Serializable

@Serializable
data class SavedPattern(
    val name: String,
    val currentRound: Int,
    val lastPlacedVertexId: Int?,
    val vertices: List<SavedVertex>,
    val edges: List<SavedEdge>,
    val elements: List<SavedElement>
)

@Serializable
data class SavedVertex(val id: Int, val type: StitchType, val round: Int, val isEnd: Boolean)

@Serializable
data class SavedEdge(val newId: Int, val oldId: Int, val type: EdgeType)

@Serializable
data class SavedElement(
    val isSlipStitch: Boolean,
    val vertexId: Int? = null,      // dla zwykłego oczka
    val fromVertexId: Int? = null,  // dla slip stitch
    val toVertexId: Int? = null,    // dla slip stitch
    val gridX: Float,
    val gridY: Float,
    val rotation: Float = 0f,
    val round: Int = 1
)