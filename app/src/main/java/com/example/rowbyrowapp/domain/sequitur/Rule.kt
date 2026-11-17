package com.example.rowbyrowapp.domain.sequitur

import com.example.rowbyrowapp.domain.folding.ASTNode
import com.example.rowbyrowapp.domain.folding.SymbolNode

class Rule {
    var theGuard: Guard = Guard(this)
    var count: Int = 0
    var number: Int = numRules++
    var index: Int = 0

    companion object {
        var numRules: Int = 0
    }

    fun first(): Symbol = theGuard.next!!

    fun last(): Symbol = theGuard.prev!!

    fun getRules(): String {

        val rules = ArrayList<Rule>(numRules)
        var processedRules = 0
        val text = StringBuilder()

        text.append("Usage\tRule\n")
        rules.add(this)

        while (processedRules < rules.size) {
            val currentRule = rules[processedRules]
            text.append(" ")
                .append(currentRule.count)
                .append("\tR")
                .append(processedRules)
                .append(" -> ")

            var sym = currentRule.first()
            while (sym !is Guard) {
                if (sym is NonTerminal) {
                    val referedTo = sym.r
                    val index: Int
                    if (rules.size > referedTo.index && rules[referedTo.index] == referedTo) {
                        index = referedTo.index
                    } else {
                        index = rules.size
                        referedTo.index = index
                        rules.add(referedTo)
                    }
                    text.append('R').append(index)
                } else {
                    if (sym.value == ' '.code) {
                        text.append('_')
                    } else if (sym.value == '\n'.code) {
                        text.append("\\n")
                    } else {
                        text.append(sym.value.toChar())
                    }
                }
                text.append(' ')
                sym = sym.next!!
            }
            text.append('\n')
            processedRules++
        }
        return text.toString()
    }

    fun getRulesAsMap() : Map<String,List<ASTNode>> {

        val rules = ArrayList<Rule>(numRules)
        var processedRules = 0
        val rulesAsMap = mutableMapOf<String,List<ASTNode>>()

        rules.add(this)

        while (processedRules < rules.size) {
            val currentRule = rules[processedRules]
            val symbolsInCurrentRule = mutableListOf<ASTNode>()

            var sym = currentRule.first()
            while (sym !is Guard) {
                if (sym is NonTerminal) {
                    val referedTo = sym.r
                    val index: Int
                    if (rules.size > referedTo.index && rules[referedTo.index] == referedTo) {
                        index = referedTo.index
                    } else {
                        index = rules.size
                        referedTo.index = index
                        rules.add(referedTo)
                    }
                    symbolsInCurrentRule.add(SymbolNode(name = "R$index", isNonTerminal = true))
                } else {
                    val charStr = when (sym.value) {
                        ' '.code -> "_"
                        '\n'.code -> "\\n"
                        else -> sym.value.toChar().toString()
                    }
                    symbolsInCurrentRule.add(SymbolNode(name = charStr, isNonTerminal = false))
                }
                sym = sym.next!!
            }
            rulesAsMap["R$processedRules"] = symbolsInCurrentRule
            processedRules++
        }
        return rulesAsMap
    }
}