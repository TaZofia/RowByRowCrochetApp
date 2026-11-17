package com.example.rowbyrowapp.domain.sequitur

class NonTerminal(var r: Rule) : Symbol() {
    init {
        r.count++
        value = NUM_TERMINALS + r.number
        prev = null
        next = null
    }

    override fun copy(): Symbol = NonTerminal(r)

    override fun cleanUp() {
        val p = prev
        val n = next
        if (p != null && n != null) {
            join(p, n)
        }
        deleteDigram()
        r.count--
    }


    /**
     * Funkcja wywoływana, jeżeli reguła została użyta tylko raz. Jej zawartość jest wtedy wstawiana w
     * miejsce tej reguły, a sama reguła jest usuwana.
     */
    fun expand() {
        val p = prev ?: return
        val n = next ?: return
        join(p, r.first())
        join(r.last(), n)
    }
}