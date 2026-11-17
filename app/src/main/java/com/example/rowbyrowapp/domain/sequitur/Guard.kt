package com.example.rowbyrowapp.domain.sequitur

class Guard(var r: Rule) : Symbol() {

    init {
        value = 0
        prev = this
        next = this
    }
    override fun cleanUp() {
        val previousSymbol = prev ?: return
        val nextSymbol = next ?: return
        join(previousSymbol, nextSymbol)
    }

    override fun deleteDigram() {
        // Do nothing
    }

    override fun check(): Boolean = false

    override fun copy(): Symbol = Guard(r)
}