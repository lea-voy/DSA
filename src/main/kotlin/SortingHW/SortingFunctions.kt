package SortingHW
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.time.DurationUnit

/**
 * Iterating through the whole list to find the minimum,
 * swapping that value with whatever we're counting from first,
 * then going back and going through the rest over and over until all sorted
 */
fun selectionSort(ogList: MutableList<Int>): MutableList<Int> {
    var sortedList = ogList.toMutableList()
    // looking at each spot in list
    for(i in 0 until (ogList.size-1)){
        var currentMin = 999
        var currentMinPosition = 0
        var position = i
        // going through each value in list after our i to find the min
        for(j in (i+1) until (ogList.size)){
            if(sortedList[j]<currentMin){
                currentMin = sortedList[j]
                currentMinPosition = j
            }
        }
        // swapping position of our new min and whatever was first
        if (currentMinPosition != i) {
            sortedList[currentMinPosition] = sortedList[position]
            sortedList[position] = currentMin
        }
    }
    return sortedList
}

/**
 * Comparing each pair of neighbors and swapping them if left>right,
 * then going through over and over until all are sorted
 */
fun bubbleSort(ogList: MutableList<Int>): MutableList<Int> {
    var sortedList = ogList.toMutableList()
    // looking at each pair (if not all sorted already)
    var anySwapped = false
    while(true){
        anySwapped = false
        for(i in 0 until (ogList.size-1)) {
            if (sortedList[i] > sortedList[i + 1]) {
                val temp = sortedList[i]
                sortedList[i] = sortedList[i + 1]
                sortedList[i + 1] = temp
                anySwapped = true
            }
        }
        if (anySwapped == false) {break}
    }
    return sortedList
}

/**
 * Pulling values one at a time (in order), then repeatedly comparing them to their left-hand neighbor
 * and switching the two if they're smaller, until they're in the right spot.
 */
fun insertionSort(ogList: MutableList<Int>): MutableList<Int> {
    var sortedList = ogList.toMutableList()
    // looking at each spot in list, like before
    for(i in 0 until (ogList.size-1)) {
        for(j in i downTo 0){
            if(sortedList[j] > sortedList[j + 1]) {
                val temp = sortedList[j]
                sortedList[j] = sortedList[j + 1]
                sortedList[j + 1] = temp
            }
        }
    }
    return sortedList
}

/**
 * Sort numbers into "buckets" based on their last digits, then the ones before,
 * then before, and etc, until they're all sorted
 */
fun radixSort(ogList: MutableList<Int>): MutableList<Int> {
    var sortedList = ogList.toMutableList()
    // making them all into strings so I can get lengths and index into them
    var listString = mutableListOf<String>()
    for (i in 0 until (sortedList.size)) {
        listString.add(sortedList[i].toString())
    }
    // this is definitely not the most efficient way to do this
    for (digit in 0 until 4) { // let's say we're organizing up to 4 digit numbers for my own sanity
        // making buckets, idk if they're supposed to be lists but that's what we're doing
        var zeros = mutableListOf<String>()
        var ones = mutableListOf<String>()
        var twos = mutableListOf<String>()
        var threes = mutableListOf<String>()
        var fours = mutableListOf<String>()
        var fives = mutableListOf<String>()
        var sixes = mutableListOf<String>()
        var sevens = mutableListOf<String>()
        var eights = mutableListOf<String>()
        var nines = mutableListOf<String>()
        // this is so scuffed i'm so sorry
        for (i in 0 until listString.size) {
            val numStr = listString[i]
            val indexFromRight = numStr.length - 1 - digit
            val char = if (indexFromRight >= 0) numStr[indexFromRight] else '0'

            when (char) {
                '0' -> zeros.add(numStr)
                '1' -> ones.add(numStr)
                '2' -> twos.add(numStr)
                '3' -> threes.add(numStr)
                '4' -> fours.add(numStr)
                '5' -> fives.add(numStr)
                '6' -> sixes.add(numStr)
                '7' -> sevens.add(numStr)
                '8' -> eights.add(numStr)
                '9' -> nines.add(numStr)
            }
        }
        listString = (zeros+ones+twos+threes+fours+fives+sixes+sevens+eights+nines).toMutableList()
    }

    sortedList = listString.map { it.toInt() }.toMutableList()
    return sortedList
}

fun main(){
//    val testerList = mutableListOf(6, 9, 3, 8, 7, 2, 0, 1)
//    println(selectionSort(testerList))
//    println(bubbleSort(testerList))
//    println(insertionSort(testerList))
//    println(radixSort(testerList))
    val selectionRunTimes = mutableListOf<Double>()
    for (size in listOf(10, 100, 1000, 10000, 100000)) {
        val x = (1 until size).map { Random.nextInt(100000) }
        val runTime = measureTime {
            selectionSort(x as MutableList<Int>)
        }
        selectionRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))
    }
    println("Runtimes for selection sort are $selectionRunTimes")

    val bubbleRunTimes = mutableListOf<Double>()
    for (size in listOf(10, 100, 1000, 10000, 100000)) {
        val x = (1 until size).map { Random.nextInt(100000) }
        val runTime = measureTime {
            bubbleSort(x as MutableList<Int>)
        }
        bubbleRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))
    }
    println("Runtimes for bubble sort are $bubbleRunTimes")

    val insertionRunTimes = mutableListOf<Double>()
    for (size in listOf(10, 100, 1000, 10000, 100000)) {
        val x = (1 until size).map { Random.nextInt(100000) }
        val runTime = measureTime {
            insertionSort(x as MutableList<Int>)
        }
        insertionRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))
    }
    println("Runtimes for insertion sort are $insertionRunTimes")

    val radixRunTimes = mutableListOf<Double>()
    for (size in listOf(10, 100, 1000, 10000, 100000)) {
        val x = (1 until size).map { Random.nextInt(100000) }
        val runTime = measureTime {
            radixSort(x as MutableList<Int>)
        }
        radixRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))
    }
    println("Runtimes for radix sort are $radixRunTimes")
}