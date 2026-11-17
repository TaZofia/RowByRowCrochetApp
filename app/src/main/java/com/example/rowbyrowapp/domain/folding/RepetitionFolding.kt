package com.example.rowbyrowapp.domain.folding

import java.util.LinkedList

fun foldRepetitions(
    grammarMap: Map<String, List<ASTNode>>
): List<ASTNode> {
    // zastosowanie rle dla każdej reguły gramatyki, jeden raz na początku
    val preprocessedGrammar: Map<String, List<ASTNode>> =
        grammarMap.mapValues { (_, nodes) -> rle(nodes) }
    val firstRule = preprocessedGrammar["R0"]
        ?: error("Rule R0 doesn't exist in grammar map")

    return processList(
        firstRule,
        preprocessedGrammar
    )
}

private fun rle(sequence: List<ASTNode>): List<ASTNode> {
    if (sequence.isEmpty()) return emptyList()
    val result = LinkedList<ASTNode>()

    var lastNode = sequence.first()

    for (i in 1 until sequence.size) {
        val currentNode = sequence[i]
        if (canMerge(lastNode, currentNode)) {
            lastNode = merge(
                lastNode as SymbolNode,
                currentNode as SymbolNode
            )
        } else {
            result.add(lastNode)
            lastNode = currentNode
        }
    }
    result.add(lastNode)
    return result
}

private fun expandRule(
    node: SymbolNode,
    grammarMap: Map<String, List<ASTNode>>
): List<ASTNode> {
    val products = grammarMap[node.name]
        ?: error("Rule ${node.name} doesn't exist in grammar map")

    val firstElement = products.firstOrNull()
    if (products.size == 1 && firstElement is SymbolNode) {
        // reguła ma po prawej tylko jeden symbol terminal albo nieterminal
        return listOf(
            SymbolNode(
                firstElement.name,
                firstElement.isNonTerminal,
                firstElement.count * node.count
            )
        )
    }

    return if (node.count == 1) {
        LinkedList(products)
    } else {
        // krotność większa niż 1 - tworzenie GroupNode i przetwarzanie wnętrza
        // wnętrze i tak się z niczym po bokach nie może scalić
        val processedChildren = processList(products, grammarMap)
        listOf(
            GroupNode(processedChildren.toMutableList(), node.count)
        )
    }
}

private fun processList(
    initialSequence: List<ASTNode>,
    grammarMap: Map<String, List<ASTNode>>
): List<ASTNode> {
    val core = LinkedList(initialSequence)
    val it = core.listIterator()
    while (it.hasNext()) {
        when (val node = it.next()) {
            is SymbolNode -> {
                if (node.isNonTerminal) {
                    val expanded = expandRule(
                        node,
                        grammarMap
                    )
                   insertChildrenAndMergeEdges(
                        it,
                        expanded
                   )
                }
            }
            is GroupNode -> {
                val childrenList = node.children as? LinkedList<ASTNode>
                    ?: LinkedList(node.children)
                val processedChildren = processList(
                    childrenList,
                    grammarMap
                )
                if (node.count == 1) {
                    insertChildrenAndMergeEdges(it, processedChildren)
                } else {
                    node.children = processedChildren.toMutableList()
                }
            }
        }
    }
    return core
}

private fun insertChildrenAndMergeEdges(it: MutableListIterator<ASTNode>, children: List<ASTNode>) {
    if (children.size == 1) {
        val single = children.first()
        it.set(single)
        val afterLeft = mergeWithLeft(it, single)
        mergeWithRight(it, afterLeft)
        if (it.hasPrevious()) it.previous()
        return
    }

    val childIt = children.iterator()
    val first = childIt.next()
    it.set(first)
    mergeWithLeft(it, first)

    var current = childIt.next()
    while (childIt.hasNext()) {
        it.add(current)
        current = childIt.next()
    }

    it.add(current)
    mergeWithRight(it, current)
    for (i in 0 until children.size) {
        if (it.hasPrevious()) it.previous()
    }
}

private fun mergeWithLeft(it: MutableListIterator<ASTNode>, node: ASTNode): ASTNode {
    it.previous()
    if (it.hasPrevious()) {
        val left = it.previous()
        if (canMerge(left, node)) {
            val merged = merge(left as SymbolNode, node as SymbolNode)
            it.remove()
            it.next()
            it.set(merged)
            return merged
        }
        it.next()
    }
    it.next()
    return node
}

private fun mergeWithRight(it: MutableListIterator<ASTNode>, node: ASTNode): ASTNode {
    if (it.hasNext()) {
        val right = it.next()
        if (canMerge(node, right)) {
            val merged = merge(node as SymbolNode, right as SymbolNode)
            it.remove()
            it.previous()
            it.set(merged)
            it.next()
            return merged
        }
        it.previous()
    }
    return node
}

private fun canMerge(a: ASTNode, b: ASTNode): Boolean {
    return a is SymbolNode && b is SymbolNode && a.name == b.name && a.isNonTerminal == b.isNonTerminal
}

private fun merge(a: SymbolNode, b: SymbolNode): SymbolNode {
    return SymbolNode(name = a.name, isNonTerminal = a.isNonTerminal, count = a.count + b.count)
}