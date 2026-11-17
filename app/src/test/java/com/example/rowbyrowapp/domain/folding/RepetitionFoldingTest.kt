package com.example.rowbyrowapp.domain.folding

import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Test

class RepetitionFoldingTest {
    @Test
    fun `fold repetitions test - grammar 1` (){

        // dla napisu
        // dcdcdccccdcdcdcdcdcdccccdcdcdcdcdcdccccdcdcdcdcdcdccccdcdcdcdc
        // 4x (3x dc  3x c  3x dc )  dc
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true)
            ),
            "R1" to listOf(
                SymbolNode("R3", isNonTerminal = true),
                SymbolNode("R3", isNonTerminal = true)
            ),
            "R2" to listOf(
                SymbolNode(name = "D", isNonTerminal = false),
                SymbolNode(name = "C", isNonTerminal = false)
            ),
            "R3" to listOf(
                SymbolNode(name = "R4", isNonTerminal = true),
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "R4", isNonTerminal = true)
            ),
            "R4" to listOf(
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true)

            )
        )

        val result = foldRepetitions(grammar)

        val expected = listOf(
            GroupNode(
                count = 4,
                children = mutableListOf(
                    GroupNode(
                        count = 3,
                        children = mutableListOf(
                            SymbolNode(name = "D", isNonTerminal = false, count = 1),
                            SymbolNode(name = "C", isNonTerminal = false, count = 1)
                        )
                    ),
                    SymbolNode(name = "C", isNonTerminal = false, count = 3),
                    GroupNode(
                        count = 3,
                        children = mutableListOf(
                            SymbolNode(name = "D", isNonTerminal = false, count = 1),
                            SymbolNode(name = "C", isNonTerminal = false, count = 1)
                        )
                    )
                )
            ),
            SymbolNode(name = "D", isNonTerminal = false, count = 1),
            SymbolNode(name = "C", isNonTerminal = false, count = 1)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `fold repetitions test - grammar 2` (){

        // dla napisu
        // CCCDCCCDCDDDD
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "D", isNonTerminal = false),
                SymbolNode(name = "D", isNonTerminal = false),
                SymbolNode(name = "D", isNonTerminal = false),
                ),
            "R1" to listOf(
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "R2", isNonTerminal = true)
            ),
            "R2" to listOf(
                SymbolNode(name = "C", isNonTerminal = false),
                SymbolNode(name = "D", isNonTerminal = false)
            )
        )

        val result = foldRepetitions(grammar)
        val expected = listOf(
            GroupNode(
                count = 2,
                children = mutableListOf(
                    SymbolNode(name = "C", isNonTerminal = false, count = 3),
                    SymbolNode(name = "D", isNonTerminal = false, count = 1)
                )
            ),
            SymbolNode(name = "C", isNonTerminal = false, count = 1),
            SymbolNode(name = "D", isNonTerminal = false, count = 4)
        )
        assertEquals(expected, result)

    }
    @Test
    fun `fold repetitions test - grammar 3` (){
        // dla napisu
        // CCCCCDDCCDDCCDDCCDDCCDDCCDDCCDDCCD
        // 5xC  7x(2xD 2xC) D
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R3", isNonTerminal = true),
                SymbolNode(name = "R3", isNonTerminal = true),
                SymbolNode(name = "R3", isNonTerminal = true),
                SymbolNode(name = "R4", isNonTerminal = true),
                SymbolNode(name = "R5", isNonTerminal = true),
            ),
            "R1" to listOf(
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true)
                ),
            "R2" to listOf(
                SymbolNode(name = "C", isNonTerminal = false)
            ),
            "R3" to listOf(
                SymbolNode(name = "R4", isNonTerminal = true),
                SymbolNode(name = "R4", isNonTerminal = true)
            ),
            "R4" to listOf(
                SymbolNode(name = "R5", isNonTerminal = true),
                SymbolNode(name = "R5", isNonTerminal = true),
                SymbolNode(name = "R1", isNonTerminal = true)

            ),
            "R5" to listOf(
                SymbolNode(name = "D", isNonTerminal = false),
            )
        )

        val result = foldRepetitions(grammar)
        val expected = listOf(
            SymbolNode(name = "C", isNonTerminal = false, count = 5),
            GroupNode(
                count = 7,
                children = mutableListOf(
                    SymbolNode(name = "D", isNonTerminal = false, count = 2),
                    SymbolNode(name = "C", isNonTerminal = false, count = 2)
                )
            ),
            SymbolNode(name = "D", isNonTerminal = false, count = 1)
        )

        assertEquals(expected, result)
    }

    @Test
    fun `fold repetitions test - grammar 4`() {
        // A B C B C A
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "A", isNonTerminal = false),
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "A", isNonTerminal = false)
            ),
            "R1" to listOf(
                SymbolNode(name = "B", isNonTerminal = false),
                SymbolNode(name = "C", isNonTerminal = false)
            )
        )

        val expected = listOf(
            SymbolNode(name = "A", isNonTerminal = false, count = 1),
            GroupNode(
                count = 2,
                children = mutableListOf(
                    SymbolNode(name = "B", isNonTerminal = false, count = 1),
                    SymbolNode(name = "C", isNonTerminal = false, count = 1)
                )
            ),
            SymbolNode(name = "A", isNonTerminal = false, count = 1)
        )

        val result = foldRepetitions(grammar)

        assertEquals(expected, result)
    }

    @Test
    fun `fold repetitions test - grammar 5` () {
        // ababcabc
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
            ),
            "R1" to listOf(
                SymbolNode(name = "a", isNonTerminal = false),
                SymbolNode(name = "b", isNonTerminal = false)
            ),
            "R2" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "c", isNonTerminal = false)
            )
        )

        val expected = listOf(
            SymbolNode(name="a", isNonTerminal=false, count=1),
            SymbolNode(name="b", isNonTerminal=false, count=1),
            GroupNode(children=mutableListOf(
                SymbolNode(name="a", isNonTerminal=false, count=1),
                SymbolNode(name="b", isNonTerminal=false, count=1),
                SymbolNode(name="c", isNonTerminal=false, count=1)
            ), count=2)
        )

        val result = foldRepetitions(grammar)

        assertEquals(expected, result)
    }

    @Test
    fun `fold repetitions test - grammar 6`() {
        // b c d c d a b c d
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "a", isNonTerminal = false),
                SymbolNode(name = "R1", isNonTerminal = true),
            ),
            "R1" to listOf(
                SymbolNode(name = "b", isNonTerminal = false),
                SymbolNode(name = "R2", isNonTerminal = true),
            ),
            "R2" to listOf(
                SymbolNode(name = "c", isNonTerminal = false),
                SymbolNode(name = "d", isNonTerminal = false)
            )
        )

        val expected = listOf(
            SymbolNode(name="b", isNonTerminal=false, count=1),
            GroupNode(
                count = 2,
                children = mutableListOf(
                    SymbolNode(name="c", isNonTerminal=false, count=1),
                    SymbolNode(name="d", isNonTerminal=false, count=1),
                )
            ),
            SymbolNode(name="a", isNonTerminal=false, count=1),
            SymbolNode(name="b", isNonTerminal=false, count=1),
            SymbolNode(name="c", isNonTerminal=false, count=1),
            SymbolNode(name="d", isNonTerminal=false, count=1)
        )

        val result = foldRepetitions(grammar)

        assertEquals(expected, result)
    }
    @Test
    fun `fold repetitions test - grammar 7`() {
        val grammar = mapOf(
            "R0" to listOf(
                SymbolNode(name = "R1", isNonTerminal = true),
                SymbolNode(name = "a", isNonTerminal = false),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R2", isNonTerminal = true),
                SymbolNode(name = "R3", isNonTerminal = true),
            ),
            "R1" to listOf(
                SymbolNode(name = "a", isNonTerminal = false),
                SymbolNode(name = "a", isNonTerminal = false),
            ),
            "R2" to listOf(
                SymbolNode(name = "R3", isNonTerminal = true),
                SymbolNode(name = "b", isNonTerminal = false)
            ),
            "R3" to listOf(
                SymbolNode(name = "b", isNonTerminal = false),
                SymbolNode(name = "R1", isNonTerminal = true),
            )
        )

        val expected = listOf(
            SymbolNode(name="a", isNonTerminal=false, count=3),
            GroupNode(
                children=mutableListOf(
                    SymbolNode(name="b", isNonTerminal=false, count=1),
                    SymbolNode(name="a", isNonTerminal=false, count=2),
                    SymbolNode(name="b", isNonTerminal=false, count=1)
                ),
                count=3
            ),
            SymbolNode(name="b", isNonTerminal=false, count=1),
            SymbolNode(name="a", isNonTerminal=false, count=2)
        )

        val result = foldRepetitions(grammar)

        assertEquals(expected, result)
    }
}