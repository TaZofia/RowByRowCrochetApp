package com.example.rowbyrowapp.domain.sequitur


abstract class Symbol {

    var prev: Symbol? = null
    var next: Symbol? = null

    var value: Int = 0    // zawartość symbolu

    companion object {
        private const val PRIME: Int = 2_265_539
        const val NUM_TERMINALS: Int = 100000
        val theDigrams: HashMap<Symbol, Symbol> = HashMap()
    }

    /**
     * Funkcja łącząca dwa symbole i usuwająca stary digram z Hash Mapy
     */
    fun join(left: Symbol, right: Symbol) {
        if(left.next != null) left.deleteDigram()
        left.next = right
        right.prev = left
    }


    abstract fun cleanUp()

    /**
     * Funkcja wstawiająca nowy symbol zaraz po aktualnym symbolu
     */
    fun insertAfter(toInsert: Symbol) {
        val nextSymbol = next ?: return
        join(toInsert, nextSymbol)
        join(this, toInsert)
    }
    /**
     * Funkcja usuwająca digram z Hash Mapy
     */
    open fun deleteDigram() {
        val nextSymbol = next ?: return
        if(nextSymbol is Guard) return
        val dummy = theDigrams[this]

        if(dummy === this) {     // usuwamy tylko wtedy gdy dokładnie ten digram znajduje się w Hash Mapie
            theDigrams.remove(this)
        }
    }


    /**
     * Funkcja sprawdzająca nowy digram. Jeśli gdziekolwiek się on już pojawia, to wywołuje odpowiednią funkcję,
     * w przeciwnym przypadku wstawia digram do Hash Mapy
     */
    open fun check() : Boolean {
        val n = next ?: return false
        if(n is Guard) return false

        if(!theDigrams.containsKey(this)) {
            theDigrams[this] = this
            return false
        }
        val found = theDigrams[this]!!

        if (found.next !== this) match(this, found)   // wykrycie powtórzenia

        return true
    }

    /**
     * Funkcja zastępująca digram nieterminalem
     */
    fun substitute(r: Rule){
        cleanUp()
        next?.cleanUp()
        prev?.insertAfter(NonTerminal(r))
        if(!prev?.check()!!) {
            prev?.next?.check()
        }
    }

    /**
     * Funkcja radząca sobie z takimi samymi digramami
     */
    fun match(newDigram: Symbol, matching: Symbol) {
        val r: Rule

        if(matching.prev is Guard && matching.next?.next is Guard) {
            // jeśli jest już reguła dla tego digramu
            r = (matching.prev as Guard).r
            newDigram.substitute(r)
        } else {
            // jeśli nie to trzeba utworzyć
            r = Rule()
            try {
                val first = newDigram.copy()
                val second = newDigram.next?.copy()
                r.theGuard.next = first
                first.prev = r.theGuard
                first.next = second
                second?.prev = first
                second?.next = r.theGuard
                r.theGuard.prev = second

                theDigrams[first] = first
                matching.substitute(r)
                newDigram.substitute(r)
            } catch(c : CloneNotSupportedException) {
                c.printStackTrace()
            }
        }

        val firstRuleSymbol = r.first()
        if (firstRuleSymbol is NonTerminal && firstRuleSymbol.r.count == 1) {
            firstRuleSymbol.expand()
        }
    }

    override fun hashCode() : Int {
        val nextValueLong = next?.value?.toLong() ?: 0L
        var code = (21599L * value.toLong()) + (20507L * nextValueLong)
        code %= PRIME.toLong()
        return code.toInt()
    }

    override fun equals(other: Any?) : Boolean {
        if (other !is Symbol) return false
        return value == other.value && next?.value == other.next?.value
    }


    abstract fun copy(): Symbol

}