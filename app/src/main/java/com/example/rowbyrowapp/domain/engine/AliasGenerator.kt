package com.example.rowbyrowapp.domain.engine

class AliasGenerator {

    private val availableAliases = (('A'..'Z') + ('a'..'z') + ('0'..'9')).iterator()

    private val descriptionToAlias = mutableMapOf<String, Char>()
    private val aliasToDescription = mutableMapOf<Char, String>()


    // zwraca istniejący alias lub przydziela nowy z puli
    fun getAlias(description: String): Char {
        return descriptionToAlias.getOrPut(description) {
            if (!availableAliases.hasNext()) {
                throw IllegalStateException("No more aliases available")
            }
            val alias = availableAliases.next()
            aliasToDescription[alias] = description
            alias
        }
    }

    fun getDescription(alias: Char): String? = aliasToDescription[alias]

    fun getAllMappings(): Map<Char, String> = aliasToDescription

}