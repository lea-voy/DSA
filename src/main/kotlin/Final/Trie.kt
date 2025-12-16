package Final

import java.io.File

/**
 * Tree of letters, with nodes connected to one another and used to find/complete words.
 */
class Trie() {
    // pre-making root variable
    val root = Final.Node(character = ' ')

    /**
     * Code to populate Trie class, iterates through letters to check if they already have nodes, then makes a new node if needed
     *
     * @param input: string passed in
     */
    fun insertString(input: String) {
        //indexing through letters, making them children of each other if needed
        var curr = root
        val stringLength = input.length
        for (i in 0 until stringLength){
            val currentChar = input[i]
            val maybeNode = curr.child(currentChar)
            if (maybeNode != null) {
                if (i == stringLength-1) {
                    maybeNode.wordEnd = true
                }
                curr = maybeNode
            }
            else {
                val newNode = Node(currentChar, curr)
                //println("adding child to ${curr.character} with child value ${currentChar}") //for debugging!
                if (i == stringLength-1) {
                    newNode.wordEnd = true
                }
                curr.children.add(newNode)
                curr = newNode
            }
        }
    }

    /**
     * return the node with the character you're looking for, null if nonexistent
     *
     * @param query character you're looking for a node of
     * @param startNode node to start search for, helpful to narrow down but defaults to root if nothing entered
     * @return a [Node] if found
     */
    fun getNode(query: Char, startNode: Node = root): Node? {
        //println(startNode.character) // for debugging
        // search through all vals to see if there's a node for the query char
        // old code, didn't work anymore once it got too big but didn't wanna delete completely

        /*if(startNode.character != query && startNode.children.isNotEmpty()) {
            for (childNode in startNode.children){
                if (childNode.character == query){
                    return childNode
                }
                else {
                    //recursion woag
                    val returnValue = getNode(query, childNode)
                    if (returnValue != null) {
                        return returnValue
                    }
                }
            }
        }*/

        // breadth-first search to find the first node of the requested char, can pass in a starting point
        val nodeQueue = LinkedList<Node>()
        var currNode: Node?
        if(startNode.character != query && startNode.children.isNotEmpty()) {
            nodeQueue.append(startNode)
        }

        while (!nodeQueue.isEmpty()){
            currNode = nodeQueue.popFront()
            if (currNode?.children != null){
                for (letter in currNode.children){
                    nodeQueue.append(letter)
                    if (letter.character == query){
                        return letter
                    }
                }
            }
        }
        return null
    }

    /**
     * recursively prints all children of given node, as well as the children's children, etc;
     * if none found, print the word that can be traced from the node (if there is one),
     * otherwise print that it doesn't have children
     *
     * @param curr node that it's checking the children of, defaults to root if nothing passed in
     */
    fun printChildren(curr: Final.Node? = root){
        if (curr?.children?.isNotEmpty() == true) {
            for (child in curr.children) {
                println(child.character)
                printChildren(child)
            }
        }
        else if (curr?.wordEnd == true){
            println(traceWord(curr))
        }
        else{
            println("${curr?.character} does not have children")
        }
    }

    /**
     * If a node passed in is the end of a word, trace back and return that word
     *
     * @param end node you're checking
     * @return word ending at [end], or null if none
     */
    fun traceWord(end: Node?): StringBuilder?{
        //println("reached test point 1") // for debugging
        if (end?.wordEnd == true){
            //println("reached test point 2") // for debugging
            val word = StringBuilder("")
            word.append(end.character)
            var curr = end
            //println(curr.parent?.character) // for debugging
            while (curr?.parent != root && curr?.parent != null){
                word.append(curr.parent?.character)
                curr = curr.parent
            }
            word.reverse()
            return word
        }
        else{
            println("${end?.character} is not the end of a word")
            return null
        }
    }

    /**
     * return a list of all saved words starting with the given string, unsorted (returns in order of tree levels)
     *
     * @param input string to find words based off of
     * @return a list of all possible words, or various warning messages if unavailable
     */
    fun finishWord(input: String): List<String>{
        val firstChar = input[0]
        val lastChar = input[input.length-1]

        // check if the first letter is in the tree
        val startingNode = getNode(firstChar)
        if (startingNode == null || ! root.children.contains(startingNode)) {
            return (listOf<String>("No words starting with the same letter")) // i know it's ugly but i needed to make all returns into lists so that i could actually return a list of words
        }

        // check if the last letter is in the tree
        val furthestNode = getNode(lastChar, startingNode)
        if (furthestNode == null) {
            return (listOf<String>("No words containing the final letter"))
        }

        // now go through each letter and trace down tree, break if no word like that
        var prevNode = startingNode
        for (i in 1 until input.length){
            val currNode = getNode(input[i], prevNode!!)
            if (currNode != null && prevNode.children.contains(currNode)){
                prevNode = currNode
            }
            else { return (listOf<String>("Input string not found in saved words")) }
        }

        // if no prev conditions triggered, look for nearest words
        val nodeQueue = LinkedList<Node>()
        nodeQueue.append(furthestNode)
        val foundWords = mutableListOf<String>()
        var currNode = furthestNode
        while (!nodeQueue.isEmpty()){
            currNode = nodeQueue.popFront()
            if (currNode?.children != null){
                for (letter in currNode.children){
                    nodeQueue.append(letter)
                    if (letter.wordEnd == true){
                        val foundWord = traceWord(letter)
                        val word = foundWord.toString()
                        foundWords.add(word)
                    }
                }
            }
        }
        return(foundWords)
    }

    /**
     * picks out all words from a Markov list and puts them into the trie
     *
     * @param input Markov list you got from analyzing text
     */
    fun populateWithList(input: List<Pair<String, Int>>){
        for (i in input){
            insertString(i.first)
        }
    }

    /**
     * variation of the finishWord() function, where you pass in a Markov list along with your input to get results ranked;
     * if a string of multiple words, sorts recommended words based on their likelihood of following the previous word,
     * or if just one word, sorts recommended based on how often those words come up in total
     *
     * @param input string to finish the word of, can be one word or multiple
     * @param markovList a Markov object, in order to access the list and masterList
     * @return a sorted list of all possible words, along with how often they come up
     */
    fun finishWithMarkov(input: String, markovList: Markov): List<Pair<String, Int>> {
        // split up string to see if it's just the one word or if it's following another
        val words = input.split(" ").filter { it.isNotBlank() }
        val listLen = words.size
        val fullList = finishWord(words[listLen-1])
        val valList = mutableListOf<Pair<String, Int>>()
        for (i in fullList) {
            if (listLen == 1) {
                val num = markovList.masterList[i] ?: 0
                valList.add(Pair(i, num))
            }
            else{
                val refList = markovList.list[words[listLen-2]]!!
                val num = refList[i] ?: 0
                valList.add(Pair(i, num))
            }
        }
        val sortedList = valList.sortedByDescending { it.second }
        return sortedList
    }
}

/**
 * Very simple class to hold node information, including a mutable list of its children and
 * a marker to indicate if it is the end of a word
 * @param character the letter that the node is
 * @param parent the node that this one branches off of, used for tracing back
 */
class Node(var character: Char, var parent: Node? = null) {
    var wordEnd: Boolean = false
    var children: MutableList<Node> = mutableListOf()

    // rarely used, mostly was for debugging, just returns if a node has a child with a certain character
    fun child(c: Char): Node? {
        //println("child ${character} ${c} ${children.find { it.character == c }}") //debugging as well
        return children.find { it.character == c }
    }
}


/**
 * a bunch of nodes linked together individually, with functions to add, "pop", and print
 *
 * @see ListNode
 */
class LinkedList<T>{
    /**
     * different type of node, holds the data for nodes in linked lists specifically
     *
     * @param data value for the node, can be various types
     * @param next link to the node following this one
     * @param prev link to the node before this one
     */
    class ListNode<T>(val data: T, var next: ListNode<T>?, var prev: ListNode<T>?)
    private var front: ListNode<T>? = null
    private var back: ListNode<T>? = null

    /**
     * @return true if linked list is empty
     */
    fun isEmpty(): Boolean {
        return front == null
    }

    /**
     * add a new node with value [data] to the end of the list
     */
    fun append(data: T) {
        if (back == null){
            val newNode = ListNode(data, null, null)
            front = newNode
            back = newNode
        }
        else {
            val newNode = ListNode(data, next = null, prev = back)
            back?.next = newNode
            back = newNode
        }
    }

    /**
     * @return the node at the front of the list, then remove it
     */
    fun popFront(): T? {
        val oldFront = front ?: return null
        front = oldFront.next
        if (front == null) {
            back = null
        }
        else {
            front?.prev = null
        }
        return oldFront.data
    }

    /**
     * @return all nodes as a "list" (string)
     */
    override fun toString(): String {
        val output = StringBuilder("[")
        var current = front
        while (current != null) {
            output.append(current.data)
            if (current.next != null) output.append(", ")
            current = current.next
        }
        output.append("]")
        return output.toString()
    }
}

// also recycling associative arrays code... that's a lot of classes
/**
 * Represents a mapping of keys to values.
 * @param K the type of the keys
 * @param V the type of the values
 */
class AssociativeArray<K, V> {
    val hashTable: Array<MutableList<Pair<K, V>>> = Array(53) { mutableListOf() }

    // random vals for hashing function later
    val a = (1..52).random()
    val b = (0..52).random()

    /**
     * Insert the mapping from the key, [key], to the value, [value].
     * If the key already maps to a value, replace the mapping.
     */
    operator fun set(key: K, value: V) {
        val index = hash(key)
        val bucket = hashTable[index]
        for (i in bucket.indices) {
            if (bucket[i].first == key) {
                bucket[i] = Pair(key, value)
                return
            }
        }
        bucket.add(Pair(key, value))
    }

    /**
     * @return true if [key] is a key in the associative array
     */
    operator fun contains(key: K): Boolean {
        val index = hash(key)
        for ((k, v) in hashTable[index]) {
            if (k == key) {
                return true
            }
        }
        return false
    }

    /**
     * @return the value associated with the key [key] or null if it doesn't exist
     */
    operator fun get(key: K): V? {
        val index = hash(key)
        for ((k, v) in hashTable[index]) {
            if (k == key) {
                return v
            }
        }
        return null
    }

    /**
     * @return the full list of key value pairs for the associative array
     */
    fun keyValuePairs(): List<Pair<K, V>> {
        val fullList = mutableListOf<Pair<K, V>>()
        for (bucket in hashTable) {
            for ((k, v) in bucket) {
                fullList.add(Pair(k, v))
            }
        }
        return fullList
    }

    /**
     * Calculate hash value for input [key]
     */
    fun hash(key: K): Int {
        val keyString = key.toString()
        var hash = 0
        for (char in keyString) {
            hash = (hash+(a * char.code + b)) % 53
        }
        return hash
    }
}

/**
 * Class to hold the various lists related to Markov text analysis, as well as functions to generate them
 */
class Markov() {
    // list is the classic Markov list, where you have a list of words and then each of them has a list of words and how often they follow the first word
    // masterList gets the grand total of how often every word shows up in the given file, useful for when a word isn't following another
    val list = AssociativeArray<String, AssociativeArray<String, Int>>()
    val masterList = AssociativeArray<String, Int>()

    /**
     * goes through the file given by [path], splitting it all up into words and
     * saving how many times each word follows another word, as well as the total number of appearances of each
     */
    fun markovAnalysis(path: String) {
        val text = File(path).readText()
        val words = text.lowercase().split(Regex("\\W+")).filter { it.isNotEmpty() }

        for (i in 0 until words.size - 1) {
            val current = words[i]
            val next = words[i + 1]

            if (!list.contains(current)) {
                list[current] = AssociativeArray()
            }

            if(!masterList.contains(current)) {
                masterList[current] = 0
            }

            val nextList = list[current]!!
            val count = nextList[next] ?: 0
            nextList[next] = count + 1

            val totalCount = masterList[current]?: 0
            masterList[current] = totalCount + 1
        }
    }

    /**
     * @returns a list of the most common words in total, sorted largest to smallest
     */
    fun returnTotals(): List<Pair<String, Int>> {
        return (masterList.keyValuePairs().sortedByDescending {it.second})
    }

    /**
     * prints a list of the most commonly following words for a given word, sorted largest to smallest
     */
    fun printSorted(word: String){
        println(list[word]!!.keyValuePairs().sortedByDescending { it.second })
        println()
    }
}



fun main(){
    // various random tests from all throughout the process
    val trie = Trie()
    trie.insertString("cat")
    trie.insertString("cap")

    println("Searching for node c:")
    val plswork = trie.getNode('c')
    println(plswork)
    println(plswork?.character)
    println("Printing children:")
    trie.printChildren(plswork)

    println("Searching for node a:")
    val raaaah = trie.getNode('a', plswork!!)
    println(raaaah)
    println(raaaah?.character)
    println("Printing children:")
    trie.printChildren(raaaah)

    println("Testing full print children:")
    trie.printChildren()

    println("Check if a would be able to trace back:")
    println(trie.traceWord(raaaah))

    println("Tracing word back from t:")
    val lastNode = trie.getNode('t')
    println(trie.traceWord(lastNode))

    println("Tracing word back from p:")
    val aaaaaaaaaaaa = trie.getNode('p')
    println(trie.traceWord(aaaaaaaaaaaa))

    trie.insertString("clap")
    trie.insertString("wow")
    trie.insertString("woag")
    trie.insertString("raahhh")
    trie.insertString("rage")

    println("Printing all children:")
    trie.printChildren()

    println(trie.finishWord("grrr"))
    println(trie.finishWord("rags"))
    println(trie.finishWord("clamp"))

    println(trie.finishWord("ra"))

    val analysisTime = Markov()
    analysisTime.markovAnalysis("Aesop.txt")
    println(analysisTime.returnTotals())

    trie.populateWithList(analysisTime.returnTotals())

    println(trie.finishWord("th"))
    println(trie.finishWord("ra"))

    println(trie.finishWithMarkov("th", analysisTime))
    println(trie.finishWithMarkov("ra", analysisTime))

    println(trie.finishWithMarkov("the ca", analysisTime))
    println(trie.finishWithMarkov("when they ra", analysisTime))
    println(trie.finishWithMarkov("he ro", analysisTime))
}