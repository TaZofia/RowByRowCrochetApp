package com.example.rowbyrowapp.domain.model

data class StitchVertex(val id: Int, val type: StitchType, val round:Int, var isStart:Boolean = false, var isEnd:Boolean = false) {}


