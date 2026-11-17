package com.example.rowbyrowapp.domain.graph

import com.example.rowbyrowapp.domain.model.StitchVertex

data class Edge(val newVertex: StitchVertex, val oldVertex: StitchVertex, val edgeType: EdgeType)
