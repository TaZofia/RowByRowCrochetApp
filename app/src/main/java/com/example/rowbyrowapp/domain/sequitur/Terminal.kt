package com.example.rowbyrowapp.domain.sequitur

class Terminal(theValue: Int) : Symbol() {
    init {
        value = theValue
        prev = null
        next = null
    }

    override fun cleanUp() {
        val p = prev
        val n = next
        if (p != null && n != null) {
            join(p, n)
        }
        deleteDigram()
    }

    override fun copy(): Symbol = Terminal(value)
}