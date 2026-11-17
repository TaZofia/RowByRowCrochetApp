package com.example.rowbyrowapp.domain.engine

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AliasGeneratorTest {

    private lateinit var aliasGenerator: AliasGenerator

    @BeforeEach
    fun setUp() {
        aliasGenerator = AliasGenerator()
    }

    @Test
    fun `should assign first alias 'A' for new description`() {
        val alias = aliasGenerator.getAlias("first_description")

        assertEquals('A', alias)
    }

    @Test
    fun `should return same alias for same description`() {
        val alias1 = aliasGenerator.getAlias("same_description")
        val alias2 = aliasGenerator.getAlias("same_description")

        assertEquals(alias1, alias2)
    }

    @Test
    fun `should assign consecutive characters in order A-Z, a-z, 0-9`() {
        val aliasA = aliasGenerator.getAlias("description1")
        val aliasB = aliasGenerator.getAlias("description2")

        assertEquals('A', aliasA)
        assertEquals('B', aliasB)
    }

    @Test
    fun `should correctly retrieve description by alias`() {
        val description = "test_description"
        val alias = aliasGenerator.getAlias(description)

        val fetchedDescription = aliasGenerator.getDescription(alias)

        assertEquals(description, fetchedDescription)
    }

    @Test
    fun `should return null for non-existing alias`() {
        val description = aliasGenerator.getDescription('Z')

        assertNull(description)
    }

    @Test
    fun `should return complete mapping of all generated aliases`() {
        aliasGenerator.getAlias("description1")
        aliasGenerator.getAlias("description2")

        val mappings = aliasGenerator.getAllMappings()

        assertEquals(2, mappings.size)
        assertEquals("description1", mappings['A'])
        assertEquals("description2", mappings['B'])
    }

    @Test
    fun `should throw exception when available alias pool is exhausted`() {
        // A-Z (26) + a-z (26) + 0-9 (10)
        val maxAliases = 62

        for (i in 0 until maxAliases) {
            aliasGenerator.getAlias("description_$i")
        }

        val exception = assertThrows<IllegalStateException> {
            aliasGenerator.getAlias("exceeding_limit_description")
        }

        assertEquals("No more aliases available", exception.message)
    }

    @Test
    fun `should transition from 'Z' to 'a' when upper case letters are exhausted`() {
        // 26 aliasów, aby przejść przez cały zakres A-Z
        repeat(26) { index ->
            aliasGenerator.getAlias("uppercase_desc_$index")
        }

        val alias = aliasGenerator.getAlias("first_lowercase_desc")

        assertEquals('a', alias)
    }

    @Test
    fun `should transition from 'z' to '0' when lower case letters are exhausted`() {
        repeat(52) { index ->
            aliasGenerator.getAlias("letter_desc_$index")
        }

        val alias = aliasGenerator.getAlias("first_digit_desc")

        assertEquals('0', alias)
    }
}