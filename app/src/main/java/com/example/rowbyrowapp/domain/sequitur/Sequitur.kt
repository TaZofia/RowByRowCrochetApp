package com.example.rowbyrowapp.domain.sequitur

import com.example.rowbyrowapp.domain.folding.ASTNode
import kotlin.text.iterator

fun runSequitur(text: String): String = buildSequiturRule(text).getRules()

fun runSequiturAsMap(text: String): Map<String, List<ASTNode>> = buildSequiturRule(text).getRulesAsMap()


private fun buildSequiturRule(text: String): Rule {
    Symbol.theDigrams.clear()
    Rule.numRules = 0

    val firstRule = Rule()
    try {
        for (char in text) {
            firstRule.last().insertAfter(Terminal(char.code))
            firstRule.last().prev?.check()
        }
    } finally {
        Symbol.theDigrams.clear()
    }
    return firstRule
}