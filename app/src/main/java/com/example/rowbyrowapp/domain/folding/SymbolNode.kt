package com.example.rowbyrowapp.domain.folding

// pojedyncze symbole zarówno terminalne jak nieterminalne w drzewie AST
data class SymbolNode(
    val name : String,
    val isNonTerminal: Boolean,
    override val count: Int = 1
) : ASTNode