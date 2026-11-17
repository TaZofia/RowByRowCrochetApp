package com.example.rowbyrowapp.domain.folding

// grupy symboli
data class GroupNode(
    var children : MutableList<ASTNode> = mutableListOf(),  // zagnieżdżone powtórzenia
    override val count : Int = 1
) : ASTNode
