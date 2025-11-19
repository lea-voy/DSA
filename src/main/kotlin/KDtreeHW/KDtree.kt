package KDtreeHW
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.measureTime
import kotlin.time.DurationUnit
import kotlin.time.Duration

class KDtree(val fullList: List<Node>, val dimensions: Int) {
    /**
     * Code to build the tree, used recursively
     *
     * @param points: the list of nodes to be tree-d
     * @param depth: how far into the overall tree this one starts, used in the recursion
     * @return the first node from the new tree, which has the links to left and right children and the rest of the tree
     */
    fun buildTree(points: List<Node>, depth: Int): Node {
        if (points.size > 1){
            // finding median, then splitting up list into 2 smaller
            val axis = depth % dimensions
            val sortedNodes = selectionSort(points, axis)
            val sortedVals = mutableListOf<Int>()

            for (i in sortedNodes.indices){
                sortedVals.add(sortedNodes[i].getCoordinate(axis))
            }

            val length = sortedNodes.size
            val medianIndex = length/2
            val medianPoint = sortedNodes[medianIndex]
            val leftHalf = sortedNodes.slice(0 until medianIndex)
            val rightHalf = sortedNodes.slice((medianIndex+1) until length)

            // then using all that to recursively build more nodes to make tree
            val node = Node(medianPoint.coordinates, axis)

            if(leftHalf.isNotEmpty()) {
                node.left = buildTree(leftHalf, (depth + 1))
            }
            if(rightHalf.isNotEmpty()) {
                node.right = buildTree(rightHalf, (depth + 1))
            }

            return node
        }
        
        // base case, recursion goes down to here and then it can start building the nodes 
        if (points.size == 1){
            val point = points[0]
            return Node(point.coordinates, depth % dimensions)
        }

        // this used to get triggered when something was split down to 2, since one node stays as axis and then one becomes the child, but the other child spot is empty
        // i'm pretty sure i'm protected against it now but kotlin wants me to have *something* to return in case the other statements aren't true
        throw IllegalArgumentException("Point list empty")
    }

    /**
     * Wrapper for the recursive nearest function, so that you only need to pass in 2 things, and then it figures out the other inputs for you
     *
     * @param query: the node you pass in to find nearest neighbor for
     * @param root: the root of your tree, so that it can traverse it
     * @return the node that is the closest neighbor to the query point, found with the k-D tree
     */
    fun findNearest(query: Node, root: Node): Node {
        return recursiveNearest(root, query, root, euclidianDistance(root, query))
    }

    // mine failed very hard the first time so redoing it all and apparently it shld be recursive too?? sick and twisted
    /**
     * Code to recursively find the nearest neighbor to the query point
     *
     * @param current: the node currently being focused on for that iteration
     * @param query: the node you passed in to find nearest neighbor for
     * @param best: the current nearest neighbor node
     * @param bestDist: the distance to the best neighbor
     * @return the closest neighbor to the query point, at least for that recursion
     */
    fun recursiveNearest(current: Node?, query: Node, best: Node, bestDist: Double): Node {
        if (current == null){return best}

        val axis = current.axis
        val dist = euclidianDistance(current, query)

        var newBest = best
        var newBestDist = bestDist

        if (dist < newBestDist){
            newBest = current
            newBestDist = dist
        }

        val diff = query.getCoordinate(axis) - current.getCoordinate(axis)

        // deciding which branch to go down and also saving the secondary option to check if needed
        var primary: Node?
        var secondary: Node?
        if (diff < 0){
            primary = current.left
            secondary = current.right
        }
        else {
            primary = current.right
            secondary = current.left
        }

        // now......recursion
        if (primary != null) {
            newBest = recursiveNearest(primary, query, newBest, newBestDist)
            newBestDist = euclidianDistance(newBest, query)
        }

        // checking distance from axis to see if we need to check other branch as well
        val axisDist = kotlin.math.abs(diff)
        if (axisDist < newBestDist && secondary != null){
            newBest = recursiveNearest(secondary, query, newBest, newBestDist)
        }

        return newBest
    }

    /**
     * Code to find the nearest neighbor with brute force; going through every single other node and calculating distance for all of them
     *
     * @param query: the node you pass in to find nearest neighbor for
     * @return the node closest to the query point
     */
    fun bruteForce(query: Node): Node {
        var minDist = euclidianDistance(fullList[0], query)
        var nearestNeighbor = fullList[0]

        for (node in 1 until fullList.size) {
            val distance = euclidianDistance(fullList[node], query)
            if (distance < minDist) {
                minDist = distance
                nearestNeighbor = fullList[node]
            }
        }

        return nearestNeighbor
    }

    /**
     * Code to calculate the Euclidian distance between any 2 nodes
     *
     * @param point1: the first node
     * @param point2: the second node
     * @return the double of the Euclidian distance
     */
    fun euclidianDistance(point1: Node, point2: Node): Double {
        var sum = 0
        for (i in 0 until dimensions){
            val difference = point1.getCoordinate(i) - point2.getCoordinate(i)
            sum += (difference*difference) //squaring it lol
        }
        return sqrt(sum.toDouble())
    }
}

// bringing back my old selection sort code and modifying to work w/ nodes
/**
 * Code to sort a given list of nodes according to a specified dimension
 *
 * @param ogList: the original, unordered list of nodes
 * @param dimension: the axis along which you're ordering nodes (which one to look at)
 * @return a sorted mutable list of nodes
 */
fun selectionSort(ogList: List<Node>, dimension: Int): MutableList<Node> {
    val sortedVals = mutableListOf<Int>()
    val sortedList = ogList.toMutableList()

    for (i in 0 until ogList.size){
        sortedVals.add(ogList[i].getCoordinate(dimension))
    }

    // looking at each spot in list
    for(i in 0 until (ogList.size-1)){
        var currentMin = Int.MAX_VALUE
        var currentMinPosition = 0
        val position = i
        // going through each value in list after our i to find the min
        for(j in (i+1) until (ogList.size)){
            if(sortedList[j].getCoordinate(dimension) < currentMin){
                currentMin = sortedList[j].getCoordinate(dimension) //attempts to avoid null errors will NOT stop me, kotlin.
                currentMinPosition = j
            }
        }
        // swapping position of our new min and whatever was first
        if (currentMinPosition != i) {
            val minNode = sortedList[currentMinPosition] //saving val so i can swap
            sortedList[currentMinPosition] = sortedList[position]
            sortedList[position] = minNode
        }
    }
    return sortedList
}


// much simpler than originally attempted, just needs to hold references to left and right and have a way to return
// coordinates at a certain dimension (technically doesn't even need that, but I found it a bit easier to use at times)
class Node(var coordinates: List<Int>, val axis: Int) {
    var left: Node? = null
    var right: Node? = null

    fun getCoordinate(dimension: Int): Int {
        return coordinates[dimension]
    }
}


/**
 * Benchmark [KDTree] against brute force nearest neighbor.
 * 1000 test points will be generated to test against the training
 * points.
 *
 * @param k: the dimensionality of the dataset to create
 * @param numPoints: the number of points to use to match against
 *   (1000 test points will be used)
 * @return the triple of [Duration] objects where the first specifies
 * the time to build the tree, the second specifies the time it takes
 * to query the tree with 1,000 points, and the third is the time it
 * takes to find the nearest neighbor to these points using the brute
 * force approach.
 */
fun runExperiment(k: Int, numPoints: Int): Triple<Duration, Duration, Duration> {
    // make the list of random nodes
    val points = mutableListOf<Node>()
    for(i in 0 until numPoints){
        val coords = (0 until k).map { Random.nextInt(100) }
        val node = Node(coords, 0)
        points.add(node)
    }
    val pointList = points.toList()

    // do all the things and time them, establish variables outside of loop so I can access later
    lateinit var tree: KDtree
    lateinit var root: Node

    val buildTime = measureTime {
        tree = KDtree(pointList, k)
        root = tree.buildTree(pointList, 0)
    }

    val coords = (0 until k).map { Random.nextInt(100) }
    val query = Node(coords, 0)
    var euclidianNearest: Node
    var bruteNearest: Node

    val searchTime = measureTime {
        euclidianNearest = tree.findNearest(query, root)
    }
    println("Closest node to ${query.coordinates}: ${euclidianNearest.coordinates}, with a Euclidian distance of ${tree.euclidianDistance(euclidianNearest, query)}, found in ${searchTime.toDouble(DurationUnit.SECONDS)} seconds")

    val bruteTime = measureTime {
        bruteNearest = tree.bruteForce(query)
    }
    println("Closest node by brute force method: ${bruteNearest.coordinates}, with a Euclidian distance of ${tree.euclidianDistance(bruteNearest, query)}, found in ${bruteTime.toDouble(DurationUnit.SECONDS)} seconds")

    return Triple(buildTime, searchTime, bruteTime)
}

fun main(){
    // testing a simpler tree so I can print each thing
    val pt1 = Node(listOf(3, 7, 2, 9), 0)
    val pt2 = Node(listOf(8, 3, 6, 1), 0)
    val pt3 = Node(listOf(8, 3, 6, 6), 0)
    val pt4 = Node(listOf(3, 6, 5, 2), 0)
    val pt5 = Node(listOf(9, 5, 6, 2), 0)
    val pt6 = Node(listOf(2, 9, 3, 6), 0)
    val pt7 = Node(listOf(4, 8, 1, 4), 0)

    val pointList = listOf(pt1, pt2, pt3, pt4, pt5, pt6, pt7)
    val tree = KDtree(pointList,4)
    var root = tree.buildTree(pointList, 0)

    println("Root: ${root.coordinates} (axis ${root.axis})")
    println("Left child: ${root.left?.coordinates} (axis ${root.left?.axis})")
    println("Right child: ${root.right?.coordinates} (axis ${root.right?.axis})")
    println("Left left child: ${root.left?.left?.coordinates} (axis ${root.left?.left?.axis})")
    println("Left right child: ${root.left?.right?.coordinates} (axis ${root.left?.right?.axis})")
    println("Right left child: ${root.right?.left?.coordinates} (axis ${root.right?.left?.axis})")
    println("Right right child: ${root.right?.right?.coordinates} (axis ${root.right?.right?.axis})")
    println()

    // also testing euclidian distance function, wrote a short function to make it more efficient
    fun testingRandom(input:Node, tree: KDtree, root:Node) {
        val nearest = tree.findNearest(input, root)
        println("Closest node to ${input.coordinates}: ${nearest.coordinates}, with a Euclidian distance of ${tree.euclidianDistance(nearest, input)}")
        val bruteNearest = tree.bruteForce(nearest)
        println("Closest node by brute force method: ${bruteNearest.coordinates}, with a Euclidian distance of ${tree.euclidianDistance(bruteNearest, input)}")
        println()
    }

    val randomTest = Node(listOf(2, 3, 9, 8), 0)
    testingRandom(randomTest, tree, root)

    val anotherTest = Node(listOf(9, 3, 2, 4), 0)
    testingRandom(anotherTest, tree, root)


    // bigger tree !!
    val p1 = Node(listOf(3,2,7,4,8), 0)
    val p2 = Node(listOf(0,3,4,7,5), 0)
    val p3 = Node(listOf(1,9,2,6,5), 0)
    val p4 = Node(listOf(8,3,5,1,0), 0)
    val p5 = Node(listOf(2,8,4,0,2), 0)
    val p6 = Node(listOf(2,9,5,6,1), 0)
    val p7 = Node(listOf(4,9,6,2,7), 0)
    val p8 = Node(listOf(1,2,3,9,5), 0)
    val p9 = Node(listOf(2,0,5,8,1), 0)
    val p10 = Node(listOf(7,3,4,9,5), 0)
    val p11 = Node(listOf(2,6,4,9,2), 0)
    val p12 = Node(listOf(2,0,5,7,3), 0)
    val p13 = Node(listOf(0,3,7,1,5), 0)
    val p14 = Node(listOf(5,7,2,6,3), 0)
    val p15 = Node(listOf(6,5,8,3,1), 0)
    val p16 = Node(listOf(0,4,8,1,6), 0)
    val p17 = Node(listOf(3,0,5,8,1), 0)
    val p18 = Node(listOf(0,7,2,5,5), 0)
    val p19 = Node(listOf(6,3,0,5,3), 0)
    val p20 = Node(listOf(2,3,4,8,1), 0)

    val biggerPointList = listOf(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20)
    val biggerTree = KDtree(biggerPointList,5)
    root = biggerTree.buildTree(biggerPointList, 0)

    println("Root: ${root.coordinates} (axis ${root.axis})")
    println("Left child: ${root.left?.coordinates} (axis ${root.left?.axis})")
    println("Right child: ${root.right?.coordinates} (axis ${root.right?.axis})")
    println("Left left child: ${root.left?.left?.coordinates} (axis ${root.left?.left?.axis})")
    println("Left left left child: ${root.left?.left?.left?.coordinates} (axis ${root.left?.left?.left?.axis})")
    println("Left right child: ${root.left?.right?.coordinates} (axis ${root.left?.right?.axis})")
    println("Left right left child: ${root.left?.right?.left?.coordinates} (axis ${root.left?.right?.left?.axis})")
    println("Right left child: ${root.right?.left?.coordinates} (axis ${root.right?.left?.axis})")
    println("Right left left child: ${root.right?.left?.left?.coordinates} (axis ${root.right?.left?.left?.axis})")
    println("Right right child: ${root.right?.right?.coordinates} (axis ${root.right?.right?.axis})")
    println("Right right left child: ${root.right?.right?.left?.coordinates} (axis ${root.right?.right?.left?.axis})")
    println()

    val testAgain = Node(listOf(2,9,8,5,7), 0)
    testingRandom(testAgain, biggerTree, root)

    val whatIfLong = Node(listOf(7,3,6,4,8,5,6,1,7), 0) // was curious -- it doesn't crash or anything, it just ignores all the extra
    testingRandom(whatIfLong, biggerTree, root)

    println("Actually running the experiments here:")
    println(runExperiment(10, 1000))
    println()
    println(runExperiment(10, 1000))
    println()
    println(runExperiment(10, 1000))
    println()
    println(runExperiment(10, 1000))
    println()
    println(runExperiment(10, 1000))
    println()
}