import java.io.File
/**
 * Represents a mapping of keys to values.
 * @param K the type of the keys
 * @param V the type of the values
 */
class AssociativeArray<K, V> {
    val hashTable: Array<MutableList<Pair<K, V>>> = Array(53) { mutableListOf() }

    /**
     * Insert the mapping from the key, [key], to the value, [value].
     * If the key already maps to a value, replace the mapping.
     */
    operator fun set(key: K, value: V){
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
    operator fun contains(key: K): Boolean{
        val index = hash(key)
        for ((k, v) in hashTable[index]){
            if (k == key) {
                return true
            }
        }
        return false
    }

    /**
     * @return the value associated with the key [key] or null if it doesn't exist
     */
    operator fun get(key: K): V?{
        val index = hash(key)
        for ((k, v) in hashTable[index]){
            if (k == key) {
                return v
            }
        }
        return null
    }

    /**
     * Remove the key, [key], from the associative array
     * @param key the key to remove
     * @return true if the item was successfully removed and false if the element was not found
     */
    fun remove(key: K): Boolean{
        val index = hash(key)
        val bucket = hashTable[index]
        val iterator = bucket.iterator()
        while (iterator.hasNext()) {
            val (k, _) = iterator.next()
            if (k == key) {
                iterator.remove()
                return true
            }
        }
        return false
    }

    /**
     * @return the number of elements stored in the hash table
     */
    fun size(): Int{
        var number = 0
        for (bucket in hashTable){
            number += bucket.size
        }
        return number
    }

    /**
     * @return the full list of key value pairs for the associative array
     */
    fun keyValuePairs(): List<Pair<K, V>>{
        val fullList = mutableListOf<Pair<K, V>>()
        for(bucket in hashTable){
            for((k,v) in bucket){
                fullList.add(Pair(k, v))
            }
        }
        return fullList
    }

    /**
     * Calculate hash value for input [key]
     */
    fun hash(key: K): Int{
        val keyString = key.toString()
        var hash = 0
        val prime = 31
        for (char in keyString){
            hash = (hash * prime + char.code) % 53
        }
        return hash
    }
}

fun main(){
    // all my assorted testing
    val intHashTest = AssociativeArray<Int, String>()
    intHashTest[19] = "nineteen"
    intHashTest[328493371] = "that's a long number"
    println(intHashTest[19])
    println(intHashTest[328493371])
    println(intHashTest.size())
    println(intHashTest.contains(19))
    println(intHashTest.contains(18))
    println(intHashTest.remove(30))
    println(intHashTest.remove(19))
    println(intHashTest.keyValuePairs())


    val stringHashTest = AssociativeArray<String, String>()
    stringHashTest["hi"] = "hello world!"
    println(stringHashTest["hi"])
    stringHashTest["hi"] = "goodbye."
    stringHashTest["cat"] = "meow"
    stringHashTest["eepy"] = "zzz"
    stringHashTest["hii"] = "haiii!!!"
    println(stringHashTest.keyValuePairs())


    // now the Markov text analysis yay! Using Aesop's Fables :)
    val text = File("Aesop.txt").readText()
    val words = text . lowercase() . split(Regex("\\W+")) . filter { it.isNotEmpty() }

    val Markov = AssociativeArray<String, AssociativeArray<String, Int>>()
    for(i in 0 until words.size-1) {
        val current = words[i]
        val next = words[i + 1]

        if (!Markov.contains(current)) {
            Markov[current] = AssociativeArray()
        }

        val nextList = Markov[current]!!
        val count = nextList[next] ?: 0
        nextList[next] = count + 1
    }

    // printing words that come after certain words in descending order
    println(Markov["the"]!!.keyValuePairs())
    println()

    fun printSorted(word: String){
        println(Markov[word]!!.keyValuePairs().sortedByDescending { it.second })
        println()
    }
    printSorted("the")
    printSorted("cat")
    printSorted("fox")
    printSorted("and")
    printSorted("he")
    printSorted("you")
    printSorted("must")
}