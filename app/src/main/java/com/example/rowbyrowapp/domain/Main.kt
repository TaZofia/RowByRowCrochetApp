package com.example.rowbyrowapp.domain

import com.example.rowbyrowapp.domain.engine.PatternTransformationEngine
import com.example.rowbyrowapp.domain.graph.EdgeType
import com.example.rowbyrowapp.domain.graph.GraphBuilder
import com.example.rowbyrowapp.domain.graph.findHoleByChain
import com.example.rowbyrowapp.domain.model.StitchType

fun main() {

    val builder = GraphBuilder()

    // ==========================================
    // RUNDA 0: Kółko początkowe (baza)
    // ==========================================
    val r0_ch1 = builder.addStitch(StitchType.CH, 0)
    r0_ch1.isStart = true
    val r0_ch2 = builder.addStitch(StitchType.CH, 0)
    val r0_ch3 = builder.addStitch(StitchType.CH, 0)
    val r0_ch4 = builder.addStitch(StitchType.CH, 0)
    val r0_ch5 = builder.addStitch(StitchType.CH, 0)

    builder.connectStitches(r0_ch2, r0_ch1, EdgeType.PREVIOUS)
    builder.connectStitches(r0_ch3, r0_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r0_ch4, r0_ch3, EdgeType.PREVIOUS)
    builder.connectStitches(r0_ch5, r0_ch4, EdgeType.PREVIOUS)

    builder.connectStitches(r0_ch5, r0_ch1, EdgeType.SLIP_STITCH)// Zamknięcie kółka bazowego

    val tempGraph = builder.build()
    val hole = tempGraph.findHoleByChain(r0_ch2)

    // ==========================================
    // RUNDA 1: Środek kwadratu (4 grupy po 3 DC)
    // Wszystkie DC z tej rundy mają jako PARENT kółko bazowe
    // ==========================================

    // Zaczynamy od 3 oczek łańcuszka zamiast pierwszego słupka
    val r1_ch1 = builder.addStitch(StitchType.CH, 1)
    val r1_ch2 = builder.addStitch(StitchType.CH, 1)
    val r1_ch3 = builder.addStitch(StitchType.CH, 1)
    builder.connectStitches(r1_ch1, r0_ch5, EdgeType.PREVIOUS) // Pierwsze oczko nowej rundy bazuje na r0_ch1
    builder.connectStitches(r1_ch2, r1_ch1, EdgeType.PREVIOUS)
    builder.connectStitches(r1_ch3, r1_ch2, EdgeType.PREVIOUS)

    /**  chyba nie ma sensu żeby CH miało inserta w HOLE bo nie wchodzi ch pod HOLE
    if (hole != null) {
        builder.connectStitches(r1_ch1, hole, EdgeType.INSERT)
    }
    */


    // Grupa 1: (Ten oszukany słupek wyżej + 2 kolejne DC + 2 CH na róg)
    val r1_dc2 = builder.addStitch(StitchType.DC, 1)
    val r1_dc3 = builder.addStitch(StitchType.DC, 1)
    val r1_corner1_ch1 = builder.addStitch(StitchType.CH, 1)
    val r1_corner1_ch2 = builder.addStitch(StitchType.CH, 1)

    builder.connectStitches(r1_dc2, r1_ch3, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc3, r1_dc2, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner1_ch1, r1_dc3, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner1_ch2, r1_corner1_ch1, EdgeType.PREVIOUS)

    // Kotwiczenie słupków w pierścieniu bazowym
    if (hole != null) {
        builder.connectStitches(r1_dc2, hole, EdgeType.INSERT)
        builder.connectStitches(r1_dc3, hole, EdgeType.INSERT)
    }

    // Grupa 2: 3 DC + 2 CH (róg)
    val r1_dc4 = builder.addStitch(StitchType.DC, 1)
    val r1_dc5 = builder.addStitch(StitchType.DC, 1)
    val r1_dc6 = builder.addStitch(StitchType.DC, 1)
    val r1_corner2_ch1 = builder.addStitch(StitchType.CH, 1)
    val r1_corner2_ch2 = builder.addStitch(StitchType.CH, 1)

    builder.connectStitches(r1_dc4, r1_corner1_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc5, r1_dc4, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc6, r1_dc5, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner2_ch1, r1_dc6, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner2_ch2, r1_corner2_ch1, EdgeType.PREVIOUS)

    listOf(r1_dc4, r1_dc5, r1_dc6).forEach {
        if (hole != null) {
            builder.connectStitches(it, hole, EdgeType.INSERT)
        }
    }

    // Grupa 3: 3 DC + 2 CH (róg)
    val r1_dc7 = builder.addStitch(StitchType.DC, 1)
    val r1_dc8 = builder.addStitch(StitchType.DC, 1)
    val r1_dc9 = builder.addStitch(StitchType.DC, 1)
    val r1_corner3_ch1 = builder.addStitch(StitchType.CH, 1)
    val r1_corner3_ch2 = builder.addStitch(StitchType.CH, 1)

    builder.connectStitches(r1_dc7, r1_corner2_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc8, r1_dc7, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc9, r1_dc8, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner3_ch1, r1_dc9, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner3_ch2, r1_corner3_ch1, EdgeType.PREVIOUS)

    listOf(r1_dc7, r1_dc8, r1_dc9).forEach {
        if (hole != null) {
            builder.connectStitches(it, hole, EdgeType.INSERT)
        }
    }

    // Grupa 4
    val r1_dc10 = builder.addStitch(StitchType.DC, 1)
    val r1_dc11 = builder.addStitch(StitchType.DC, 1)
    val r1_dc12 = builder.addStitch(StitchType.DC, 1)
    val r1_corner4_ch1 = builder.addStitch(StitchType.CH, 1)
    val r1_corner4_ch2 = builder.addStitch(StitchType.CH, 1)

    //r1_corner4_ch2.isEnd = true   // oznaczamy oczko końcowe


    builder.connectStitches(r1_dc10, r1_corner3_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc11, r1_dc10, EdgeType.PREVIOUS)
    builder.connectStitches(r1_dc12, r1_dc11, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner4_ch1, r1_dc12, EdgeType.PREVIOUS)
    builder.connectStitches(r1_corner4_ch2, r1_corner4_ch1, EdgeType.PREVIOUS)



    listOf(r1_dc10, r1_dc11, r1_dc12).forEach {
        if (hole != null) {
            builder.connectStitches(it, hole, EdgeType.INSERT)
        }
    }
    // Zamknięcie rundy 1: oczko ścisłe łączy się z 3. oczkiem łańcuszka początku rundy
    builder.connectStitches(r1_corner4_ch2, r1_ch3, EdgeType.SLIP_STITCH)


    // KOLEJNA RUNDA - 2
    val r2_ch1 = builder.addStitch(StitchType.CH, 2)
    val r2_ch2 = builder.addStitch(StitchType.CH, 2)
    val r2_ch3 = builder.addStitch(StitchType.CH, 2)
    val r2_middle1_ch1 = builder.addStitch(StitchType.CH, 2)
    val r2_middle1_ch2 = builder.addStitch(StitchType.CH, 2)

    builder.connectStitches(r2_ch1, r1_corner4_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_ch2, r2_ch1, EdgeType.PREVIOUS)
    builder.connectStitches(r2_ch3, r2_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_middle1_ch1, r2_ch3, EdgeType.PREVIOUS)
    builder.connectStitches(r2_middle1_ch2, r2_middle1_ch1, EdgeType.PREVIOUS)

    val r2_dc1 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc1, r2_middle1_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc1, r1_dc3, EdgeType.INSERT)

    val r2_dc2 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc2, r2_dc1, EdgeType.PREVIOUS)

    val tempGraph2 = builder.build()
    val hole1 = tempGraph2.findHoleByChain(r1_corner1_ch1)

    if (hole1 != null) {
        builder.connectStitches(r2_dc2, hole1, EdgeType.INSERT)
    }

    val r2_corner1_ch1 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner1_ch1, r2_dc2, EdgeType.PREVIOUS)
    val r2_corner1_ch2 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner1_ch2, r2_corner1_ch1, EdgeType.PREVIOUS)

    val r2_dc3 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc3, r2_corner1_ch2, EdgeType.PREVIOUS)
    if (hole1 != null) {
        builder.connectStitches(r2_dc3, hole1, EdgeType.INSERT)
    }
    val r2_dc4 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc4, r2_dc3, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc4, r1_dc4, EdgeType.INSERT)





    val r2_middle2_ch1 = builder.addStitch(StitchType.CH, 2)
    val r2_middle2_ch2 = builder.addStitch(StitchType.CH, 2)

    builder.connectStitches(r2_middle2_ch1, r2_dc4, EdgeType.PREVIOUS)
    builder.connectStitches(r2_middle2_ch2, r2_middle2_ch1, EdgeType.PREVIOUS)

    val r2_dc5 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc5, r2_middle2_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc5, r1_dc6, EdgeType.INSERT)

    val r2_dc6 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc6, r2_dc5, EdgeType.PREVIOUS)

    val hole2 = tempGraph2.findHoleByChain(r1_corner2_ch1)

    if (hole2 != null) {
        builder.connectStitches(r2_dc6, hole2, EdgeType.INSERT)
    }

    val r2_corner2_ch1 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner2_ch1, r2_dc6, EdgeType.PREVIOUS)
    val r2_corner2_ch2 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner2_ch2, r2_corner2_ch1, EdgeType.PREVIOUS)

    val r2_dc7 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc7, r2_corner2_ch2, EdgeType.PREVIOUS)
    if (hole2 != null) {
        builder.connectStitches(r2_dc7, hole2, EdgeType.INSERT)
    }
    val r2_dc8 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc8, r2_dc7, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc8, r1_dc7, EdgeType.INSERT)


    val r2_middle3_ch1 = builder.addStitch(StitchType.CH, 2)
    val r2_middle3_ch2 = builder.addStitch(StitchType.CH, 2)

    builder.connectStitches(r2_middle3_ch1, r2_dc8, EdgeType.PREVIOUS)
    builder.connectStitches(r2_middle3_ch2, r2_middle3_ch1, EdgeType.PREVIOUS)

    val r2_dc9 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc9, r2_middle3_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc9, r1_dc9, EdgeType.INSERT)

    val r2_dc10 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc10, r2_dc9, EdgeType.PREVIOUS)

    val hole3 = tempGraph2.findHoleByChain(r1_corner3_ch1)

    if (hole3 != null) {
        builder.connectStitches(r2_dc10, hole3, EdgeType.INSERT)
    }

    val r2_corner3_ch1 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner3_ch1, r2_dc10, EdgeType.PREVIOUS)
    val r2_corner3_ch2 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner3_ch2, r2_corner3_ch1, EdgeType.PREVIOUS)

    val r2_dc11 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc11, r2_corner3_ch2, EdgeType.PREVIOUS)
    if (hole3 != null) {
        builder.connectStitches(r2_dc11, hole3, EdgeType.INSERT)
    }
    val r2_dc12 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc12, r2_dc11, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc12, r1_dc10, EdgeType.INSERT)



    val r2_middle4_ch1 = builder.addStitch(StitchType.CH, 2)
    val r2_middle4_ch2 = builder.addStitch(StitchType.CH, 2)

    builder.connectStitches(r2_middle4_ch1, r2_dc12, EdgeType.PREVIOUS)
    builder.connectStitches(r2_middle4_ch2, r2_middle4_ch1, EdgeType.PREVIOUS)

    val r2_dc13 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc13, r2_middle4_ch2, EdgeType.PREVIOUS)
    builder.connectStitches(r2_dc13, r1_dc12, EdgeType.INSERT)

    val r2_dc14 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc14, r2_dc13, EdgeType.PREVIOUS)

    val hole4 = tempGraph2.findHoleByChain(r1_corner4_ch1)

    if (hole4 != null) {
        builder.connectStitches(r2_dc14, hole4, EdgeType.INSERT)
    }

    val r2_corner4_ch1 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner4_ch1, r2_dc14, EdgeType.PREVIOUS)
    val r2_corner4_ch2 = builder.addStitch(StitchType.CH, 2)
    builder.connectStitches(r2_corner4_ch2, r2_corner4_ch1, EdgeType.PREVIOUS)

    val r2_dc15 = builder.addStitch(StitchType.DC, 2)
    builder.connectStitches(r2_dc15, r2_corner4_ch2, EdgeType.PREVIOUS)
    if (hole4 != null) {
        builder.connectStitches(r2_dc15, hole4, EdgeType.INSERT)
    }

    // oznaczamy jako koniec
    r2_dc15.isEnd = true


    builder.connectStitches(r2_dc15, r2_ch3, EdgeType.SLIP_STITCH)


    val graph = builder.build()

    val patternEngine = PatternTransformationEngine(graph)

    patternEngine.fromGraphToReadablePattern()

}
