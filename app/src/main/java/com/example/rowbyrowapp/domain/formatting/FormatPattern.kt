package com.example.rowbyrowapp.domain.formatting

import com.example.rowbyrowapp.domain.engine.AliasGenerator
import com.example.rowbyrowapp.domain.folding.ASTNode
import com.example.rowbyrowapp.domain.folding.GroupNode
import com.example.rowbyrowapp.domain.folding.SymbolNode

class CrochetPatternFormatter(private val aliasGen: AliasGenerator) {

    fun format(foldedList : MutableList<ASTNode>) : String {
        val sb = StringBuilder()

        for((index, element) in foldedList.withIndex()) {
            if (index > 0) {
                sb.append(", ")
            }
            when(element){
                is SymbolNode -> {
                    val prefix = element.count
                    if (prefix == 1) {
                        sb.append(decodeName(element.name))
                    } else {
                        sb.append(element.count.toString()).append("x").append(decodeName(element.name))
                    }
                }
                is GroupNode -> {
                    val prefix = element.count
                    if (prefix == 1) {
                        sb.append("(").append(format(element.children)).append(")")
                    } else {
                        sb.append(element.count.toString()).append("x(").append(format(element.children)).append(")")
                    }
                }
            }
        }
        return sb.toString()
    }

    private fun decodeName(name : String) : String {
        if(name.length == 1){  // chcemy alias, który jest zawsze jednym znakiem
            val aliasChar = name.first()
            val description = aliasGen.getDescription(aliasChar)
            if(description != null){
                return description
            }
        }
        return name
    }
}


