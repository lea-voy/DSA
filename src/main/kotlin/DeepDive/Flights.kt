package DeepDive

import kotlin.collections.set

/**
 * ``Graph`` represents a directed graph
 * @param VertexType the type that represents a vertex in the graph
 */
class Graph<VertexType> {
    // set of all vertices we have
    var vertices: MutableSet<VertexType> = mutableSetOf()
    // map where the vertices are the keys and the values are another map, that one holding all neighbors +
    // a list of times and 3 prices for each (so like weights of my edges)
    var edges: MutableMap<VertexType, MutableMap<VertexType, Array<Double?>>> = mutableMapOf()

    /**
     * @return the vertices in the graph
     */
    //it got very angry at me for trying to use this name so gonna write it wrong here
    fun getVertixes(): Set<VertexType>{
        return vertices
    }

    /**
     * Add an edge between [from] and [to] with edge weight [cost], which is a list of [time, southwest, united, spirit]
     */
    fun addEdge(from: VertexType, to: VertexType, cost: Array<Double?>){
        // make sure both points are in vertices
        vertices.add(from)
        vertices.add(to)

        // make sure edge doesn't exist already and if not, make it
        if (!edges.containsKey(from)) {
            edges[from] = mutableMapOf()
        }

        // telling matlab to trust me so it stops complaining when I try to set it without !!
        edges[from]!![to] = cost
    }

    /**
     * Get all the edges that begin at [from]
     * @return a map where each key represents a vertex connected to [from] and the value represents the edge weight.
     */
    fun getEdges(from: VertexType): Map<VertexType, Array<Double?>>{
        if(edges.containsKey(from)) {
            return edges[from]!!
        }
        else{
            return emptyMap()
        }
    }

    /**
     * Prints all edges + their various weights
     */
    fun printFullMap(){
        // Prints it all out very nice with A -> B, time = x, __ price = $ __ (x3)
        for ((from, neighbors) in edges){
            for ((to, weight) in neighbors){
                println("$from -> $to, time = ${weight[0]} hours, Southwest price = $ ${weight[1]}, United price = $ ${weight[2]}, Spirit price: $ ${weight[3]}")
            }
        }
    }

    /**
     * Remove all edges and vertices from the graph
     */
    fun clear(){
        vertices = mutableSetOf()
        edges = mutableMapOf()
    }

    /**
     * Shortest paths algorithm, inputs are start, goal, and what part we're looking at (Southwest, United, Spirit, Any, or ratio (price over time))
     */
    fun shortestPath(start: VertexType, goal: VertexType, lookingFor: String): List<VertexType>?{
        var currentDist = mutableMapOf<VertexType, Double>()
        val prev = mutableMapOf<VertexType, VertexType?>()
        val queue = MinPriorityQueue<VertexType>()

        // set what parameter to check based on input, default to any
        var param = 4
        when (lookingFor) { // it recommended making my 'if - else if - else if - else if' into this, that's cool
            "Southwest" -> {
                param = 1
            }

            "United" -> {
                param = 2
            }

            "Spirit" -> {
                param = 3
            }

            "Any" -> {
                param = 4
            }

            "Ratio" -> {
                param = 5
            }
        }

        // make distances very big so they're set but not confounding
        for (vertex in vertices) {
            currentDist[vertex] = Double.POSITIVE_INFINITY
            prev[vertex] = null
        }
        // add the start to the beginning of the queue with distance 0 and priority 0
        currentDist[start] = 0.0
        queue.addWithPriority(start, 0.0)

        while (!queue.isEmpty()) {
            // go through our map
            val u = queue.next() ?: break
//            println(" u is $u")   // used for debugging
            if (u == goal) {
                // if we reached goal, build the path now!
                val path = mutableListOf<VertexType>()
                var curr: VertexType? = goal
                while (curr != null) {
                    //println(curr)
                    path.add(0, curr)
                    curr = prev[curr]
//                    println(prev)    <- used for debugging
                }
                return path

            }

            // check all neighbors and put them in the queue with their weights, based on what we're looking for
            val neighbors = getEdges(u)
            for ((v, weight) in neighbors) {
                if (weight.getOrNull(param) != null && ((param == 1) || (param == 2) || (param == 3))) {
                    val newDist = currentDist[u]!! + weight[param]!!
                    require(weight[param]!! >= 0)
                    if (newDist < currentDist[v]!!) {
//                        println("found faster path to $v going through $u")   <- used for debugging
                        currentDist[v] = newDist
                        prev[v] = u
                        queue.addWithPriority(v, newDist)
//                        println(prev)   <- used for debugging
                    }
                }
                else if((param == 4) || (param == 5)) {
                    val prices = listOf(weight[1], weight[2], weight[3])
                    var minPrice = prices.filterNotNull().min()

                    if(param == 5){
                        //println("min price: $minPrice")
                        //println("weight: ${weight[0]}")
                        minPrice /= weight[0]!!  // bro i cannot do ANYTHING without it making me safeguard for nulls
                        //println("ratio: $minPrice")
                        //println("")
                    }

                    // yayayay this fixed infinite loop, I was dividing whole new dist by the min price each time instead of just the newly added bit
                    val newDist = currentDist[u]!! + minPrice

                    if(newDist < currentDist[v]!!) {
                        currentDist[v] = newDist
                        prev[v] = u
                        queue.addWithPriority(v, newDist)
                    }
                }
            }
        }

        return null // if no path
    }
}

class MinPriorityQueue<T> {
    private var heap: MutableList<Pair<T, Double>> = mutableListOf()
    private var indexMap: MutableMap<T, Int> = mutableMapOf()

    /**
     * @return true if the queue is empty, false otherwise
     */
    fun isEmpty(): Boolean{
        return heap.isEmpty()
    }

    /**
     * Add [elem] at level [priority]
     */
    fun addWithPriority(elem: T, priority: Double){
        heap.add(Pair<T, Double>(elem, priority))
        indexMap[elem] = heap.size - 1
        bubbleUp(heap.size-1)
    }

    /**
     * Get the next (highest priority) element and remove this element from the queue.
     * @return the next element in terms of priority.  If empty, return null.
     */
    fun next(): T?{
        if (heap.isEmpty()) return null

        val min = heap[0].first
        if (heap.size == 1) {
            heap.removeAt(0)
            return min
        }

        val last = heap.removeAt(heap.size - 1)
        heap[0] = last
        bubbleDown(0)
        return min
    }

    /**
     * Adjust the priority of the given element
     * @param elem whose priority should change
     * @param newPriority the priority to use for the element
     *   the lower the priority the earlier the element in
     *   the order
     */
    fun adjustPriority(elem: T, newPriority: Double){
        val index = indexMap[elem]?: return
        heap[index] = Pair(elem,newPriority)
    }

    /**
     * Bubble up for organizing, goes through each parent above until it fits right
     */
    fun bubbleUp(index:Int) {
        var i = index
        while (i>0) {
            val parent = (i - 1)/2

            if (heap[i].second < heap[parent].second) {  // second is priority, which is already based on param; I was confused and trying to do a bunch of indexing and stuff
                val temp = heap[i]
                heap[i] = heap[parent]
                heap[parent] = temp
                i = parent
            } else {
                break
            }
        }
    }

    /**
     * Bubble down for organizing, goes through each child below until it fits right
     */
    fun bubbleDown(index:Int) {
        var i = index
        while (true) {
            val left = 2 * i + 1
            val right = 2 * i + 2
            var smallest = i

            if (left < heap.size && heap[left].second < heap[smallest].second) {
                smallest = left
            }
            if (right < heap.size && heap[right].second < heap[smallest].second) {
                smallest = right
            }

            if (smallest == i) break

            val temp = heap[i]
            heap[i] = heap[smallest]
            heap[smallest] = temp

            i = smallest
        }
    }
}

fun main(){
    // a whooooole lot of flights, saved with length, southwest, united, spirit
    val flightList = Graph<String>()

    // from New York
    flightList.addEdge("New York", "Los Angeles", arrayOf(6.0, null, 317.0, 197.0))
    flightList.addEdge("New York", "San Diego", arrayOf(6.0, null, 357.0, null))
    flightList.addEdge("New York", "San Francisco", arrayOf(6.5, null, 461.0, null))
    flightList.addEdge("New York", "Chicago", arrayOf(2.5, 174.0, 276.0, 55.0))
    flightList.addEdge("New York", "Houston", arrayOf(4.0, 179.0, 336.0, 127.0))
    flightList.addEdge("New York", "San Antonio", arrayOf(4.2, null, 279.0, 194.0))
    flightList.addEdge("New York", "Dallas", arrayOf(3.7, 172.0, 378.0, 132.0))
    flightList.addEdge("New York", "Austin", arrayOf(4.2, null, 296.0, 132.0))
    flightList.addEdge("New York", "Phoenix", arrayOf(5.5, null, 307.0, 210.0))
    flightList.addEdge("New York", "Miami", arrayOf(3.2, null, 167.0, 87.0))
    flightList.addEdge("New York", "Seattle", arrayOf(6.2, null, 463.0, null))
    flightList.addEdge("New York", "Denver", arrayOf(4.3, 209.0, 296.0, null))
    flightList.addEdge("New York", "Boston", arrayOf(1.2, null, 173.0, 222.0))

    // from Los Angeles
    flightList.addEdge("Los Angeles", "New York", arrayOf(5.3, null, 317.0, 197.0))
    flightList.addEdge("Los Angeles", "San Diego", arrayOf(1.0, null, 179.0, null))
    flightList.addEdge("Los Angeles", "San Francisco", arrayOf(1.5, 119.0, 137.0, null))
    flightList.addEdge("Los Angeles", "Chicago", arrayOf(4.0, 214.0, 397.0, 119.0))
    flightList.addEdge("Los Angeles", "Houston", arrayOf(3.2, 250.0, 419.0, 70.0))
    flightList.addEdge("Los Angeles", "San Antonio", arrayOf(2.8, 224.0, null, 73.0))
    flightList.addEdge("Los Angeles", "Dallas", arrayOf(3.0, 224.0, 467.0, 73.0))
    flightList.addEdge("Los Angeles", "Austin", arrayOf(3.0, 179.0, 357.0, null))
    flightList.addEdge("Los Angeles", "Phoenix", arrayOf(1.5, 115.0, 229.0, null))
    flightList.addEdge("Los Angeles", "Miami", arrayOf(4.8, null, null, 113.0))
    flightList.addEdge("Los Angeles", "Seattle", arrayOf(2.8, 199.0, 257.0, null))
    flightList.addEdge("Los Angeles", "Denver", arrayOf(2.3, 165.0, 329.0, null))
    flightList.addEdge("Los Angeles", "Boston", arrayOf(5.5, null, 380.0, 182.0))

    // from San Diego
    flightList.addEdge("San Diego", "New York", arrayOf(5.3, 229.0, 357.0, null))
    flightList.addEdge("San Diego", "Los Angeles", arrayOf(1.0, null, 179.0, null))
    flightList.addEdge("San Diego", "San Francisco", arrayOf(1.7, 114.0, 157.0, null))
    flightList.addEdge("San Diego", "Chicago", arrayOf(4.0, 189.0, 377.0, null))
    flightList.addEdge("San Diego", "Houston", arrayOf(3.2, 235.0, 469.0, null))
    flightList.addEdge("San Diego", "San Antonio", arrayOf(2.7, 234.0, null, null))
    flightList.addEdge("San Diego", "Dallas", arrayOf(2.8, 149.0, null, null))
    flightList.addEdge("San Diego", "Austin", arrayOf(2.8, 180.0, null, null))
    flightList.addEdge("San Diego", "Phoenix", arrayOf(1.3, 129.0, null, null))
    flightList.addEdge("San Diego", "Seattle", arrayOf(3.0, 159.0, null, null))
    flightList.addEdge("San Diego", "Denver", arrayOf(2.3, 159.0, 277.0, null))

    // from San Francisco
    flightList.addEdge("San Francisco", "New York", arrayOf(5.5, null, 441.0, null))
    flightList.addEdge("San Francisco", "Los Angeles", arrayOf(1.5, null, 137.0, null))
    flightList.addEdge("San Francisco", "San Diego", arrayOf(1.5, 115.0, 157.0, null))
    flightList.addEdge("San Francisco", "Chicago", arrayOf(4.2, 204.0, 387.0, null))
    flightList.addEdge("San Francisco", "Houston", arrayOf(3.7, null, 295.0, null))
    flightList.addEdge("San Francisco", "San Antonio", arrayOf(3.3, null, 263.0, null))
    flightList.addEdge("San Francisco", "Dallas", arrayOf(3.3, 181.0, 317.0, null))
    flightList.addEdge("San Francisco", "Austin", arrayOf(3.5, 184.0, 367.0, null))
    flightList.addEdge("San Francisco", "Phoenix", arrayOf(2.0, 134.0, 368.0, null))
    flightList.addEdge("San Francisco", "Philadelphia", arrayOf(5.2, null, 360.0, null))
    flightList.addEdge("San Francisco", "Miami", arrayOf(5.3, null, 259.0, null))
    flightList.addEdge("San Francisco", "Seattle", arrayOf(2.2, null, 197.0, null))
    flightList.addEdge("San Francisco", "Denver", arrayOf(2.5, 179.0, 297.0, null))
    flightList.addEdge("San Francisco", "Boston", arrayOf(5.5, null, 273.0, null))

    // from Chicago
    flightList.addEdge("Chicago", "New York", arrayOf(2.2, 144.0, 276.0, 55.0))
    flightList.addEdge("Chicago", "Los Angeles", arrayOf(4.3, 199.0, 397.0, 119.0))
    flightList.addEdge("Chicago", "San Diego", arrayOf(4.3, 189.0, 377.0, null))
    flightList.addEdge("Chicago", "San Francisco", arrayOf(4.7, 194.0, 387.0, null))
    flightList.addEdge("Chicago", "Houston", arrayOf(2.8, 181.0, 317.0, 60.0))
    flightList.addEdge("Chicago", "San Antonio", arrayOf(2.8, 224.0, 447.0, 114.0))
    flightList.addEdge("Chicago", "Dallas", arrayOf(2.2, 194.0, 235.0, 93.0))
    flightList.addEdge("Chicago", "Austin", arrayOf(2.7, 182.0, 363.0, 184.0))
    flightList.addEdge("Chicago", "Phoenix", arrayOf(3.7, 149.0, 297.0, null))
    flightList.addEdge("Chicago", "Philadelphia", arrayOf(2.0, 198.0, 395.0, 100.0))
    flightList.addEdge("Chicago", "Miami", arrayOf(3.0, 197.0, 353.0, 54.0))
    flightList.addEdge("Chicago", "Seattle", arrayOf(4.5, 239.0, 355.0, null))
    flightList.addEdge("Chicago", "Denver", arrayOf(2.7, 143.0, 225.0, null))
    flightList.addEdge("Chicago", "Boston", arrayOf(2.3, 179.0, 220.0, 331.0))

    // from Houston
    flightList.addEdge("Houston", "New York", arrayOf(3.3, 179.0, 336.0, 127.0))
    flightList.addEdge("Houston", "Los Angeles", arrayOf(3.5, 250.0, 419.0, 70.0))
    flightList.addEdge("Houston", "San Diego", arrayOf(3.3, 235.0, 469.0, null))
    flightList.addEdge("Houston", "San Francisco", arrayOf(4.2, null, 295.0, null))
    flightList.addEdge("Houston", "Chicago", arrayOf(2.7, 181.0, 317.0, 60.0))
    flightList.addEdge("Houston", "San Antonio", arrayOf(1.0, 145.0, 289.0, 138.0))
    flightList.addEdge("Houston", "Dallas", arrayOf(1.0, 145.0, 289.0, 97.0))
    flightList.addEdge("Houston", "Austin", arrayOf(1.0, 175.0, 349.0, null))
    flightList.addEdge("Houston", "Phoenix", arrayOf(2.8, 224.0, 447.0, null))
    flightList.addEdge("Houston", "Philadelphia", arrayOf(3.2, null, 287.0, null))
    flightList.addEdge("Houston", "Miami", arrayOf(2.5, 249.0, 417.0, 54.0))
    flightList.addEdge("Houston", "Seattle", arrayOf(4.7, null, 407.0, null))
    flightList.addEdge("Houston", "Denver", arrayOf(2.5, 154.0, 307.0, null))
    flightList.addEdge("Houston", "Boston", arrayOf(3.7, null, 297.0, 331.0))

    // from San Antonio
    flightList.addEdge("San Antonio", "New York", arrayOf(3.8, null, 279.0, 194.0))
    flightList.addEdge("San Antonio", "Los Angeles", arrayOf(3.2, 224.0, null, 73.0))
    flightList.addEdge("San Antonio", "San Diego", arrayOf(2.8, 234.0, null, null))
    flightList.addEdge("San Antonio", "San Francisco", arrayOf(3.8, null, 263.0, null))
    flightList.addEdge("San Antonio", "Chicago", arrayOf(2.7, 224.0, 447.0, 114.0))
    flightList.addEdge("San Antonio", "Houston", arrayOf(1.0, 145.0, 289.0, 138.0))
    flightList.addEdge("San Antonio", "Dallas", arrayOf(1.0, 150.0, null, 65.0))
    flightList.addEdge("San Antonio", "Phoenix", arrayOf(2.3, 209.0, null, null))
    flightList.addEdge("San Antonio", "Philadelphia", arrayOf(3.7, null, null, 138.0))
    flightList.addEdge("San Antonio", "Denver", arrayOf(2.2, 209.0, 417.0, null))

    // from Dallas
    flightList.addEdge("Dallas", "New York", arrayOf(3.2, 172.0, 378.0, 132.0))
    flightList.addEdge("Dallas", "Los Angeles", arrayOf(3.2, 224.0, 467.0, 73.0))
    flightList.addEdge("Dallas", "San Diego", arrayOf(3.0, 149.0, null, null))
    flightList.addEdge("Dallas", "San Francisco", arrayOf(3.0, 181.0, 317.0, null))
    flightList.addEdge("Dallas", "Chicago", arrayOf(2.0, 194.0, 235.0, 93.0))
    flightList.addEdge("Dallas", "Houston", arrayOf(1.0, 145.0, 289.0, 97.0))
    flightList.addEdge("Dallas", "San Antonio", arrayOf(1.0, 150.0, null, 65.0))
    flightList.addEdge("Dallas", "Austin", arrayOf(0.8, 155.0, 417.0, null))
    flightList.addEdge("Dallas", "Phoenix", arrayOf(2.5, 203.0, null, null))
    flightList.addEdge("Dallas", "Philadelphia", arrayOf(3.0, 188.0, null, 113.0))
    flightList.addEdge("Dallas", "Miami", arrayOf(2.5, 170.0, null, 49.0))
    flightList.addEdge("Dallas", "Seattle", arrayOf(4.2, 289.0, null, null))
    flightList.addEdge("Dallas", "Denver", arrayOf(2.0, 134.0, 267.0, null))
    flightList.addEdge("Dallas", "Boston", arrayOf(3.7, 194.0, null, 263.0))

    // from Austin
    flightList.addEdge("Austin", "New York", arrayOf(3.7, null, 296.0, 132.0))
    flightList.addEdge("Austin", "Los Angeles", arrayOf(3.2, 179.0, 357.0, null))
    flightList.addEdge("Austin", "San Diego", arrayOf(3.0, 180.0, null, null))
    flightList.addEdge("Austin", "San Francisco", arrayOf(3.8, 184.0, 367.0, null))
    flightList.addEdge("Austin", "Chicago", arrayOf(2.7, 182.0, 363.0, 184.0))
    flightList.addEdge("Austin", "Houston", arrayOf(1.0, 175.0, 349.0, null))
    flightList.addEdge("Austin", "Dallas", arrayOf(1.0, 155.0, 417.0, null))
    flightList.addEdge("Austin", "Phoenix", arrayOf(2.5, 209.0, null, null))
    flightList.addEdge("Austin", "Miami", arrayOf(2.7, 154.0, null, null))
    flightList.addEdge("Austin", "Denver", arrayOf(2.2, 154.0, 287.0, null))
    flightList.addEdge("Austin", "Boston", arrayOf(3.8, null, null, 395.0))

    // from Phoenix
    flightList.addEdge("Phoenix", "New York", arrayOf(4.7, null, 307.0, 210.0))
    flightList.addEdge("Phoenix", "Los Angeles", arrayOf(1.3, 115.0, 229.0, null))
    flightList.addEdge("Phoenix", "San Diego", arrayOf(1.2, 129.0, null, null))
    flightList.addEdge("Phoenix", "San Francisco", arrayOf(2.0, 134.0, 367.0, null))
    flightList.addEdge("Phoenix", "Chicago", arrayOf(3.3, 149.0, 297.0, null))
    flightList.addEdge("Phoenix", "Houston", arrayOf(2.5, 224.0, 447.0, null))
    flightList.addEdge("Phoenix", "San Antonio", arrayOf(2.0, 209.0, null, null))
    flightList.addEdge("Phoenix", "Dallas", arrayOf(2.2, 203.0, null, null))
    flightList.addEdge("Phoenix", "Austin", arrayOf(2.2, 209.0, null, null))
    flightList.addEdge("Phoenix", "Miami", arrayOf(4.2, null, null, 324.0))
    flightList.addEdge("Phoenix", "Seattle", arrayOf(3.2, 165.0, null, null))
    flightList.addEdge("Phoenix", "Denver", arrayOf(1.7, 154.0, 287.0, null))

    // from Philadelphia
    flightList.addEdge("Philadelphia", "San Francisco", arrayOf(6.2, null, 360.0, null))
    flightList.addEdge("Philadelphia", "Chicago", arrayOf(2.3, 198.0, 395.0, 100.0))
    flightList.addEdge("Philadelphia", "Houston", arrayOf(3.7, null, 287.0, null))
    flightList.addEdge("Philadelphia", "San Antonio", arrayOf(3.7, null, null, 138.0))
    flightList.addEdge("Philadelphia", "Dallas", arrayOf(3.2, 188.0, null, 113.0))
    flightList.addEdge("Philadelphia", "Miami", arrayOf(2.8, null, null, 39.0))
    flightList.addEdge("Philadelphia", "Denver", arrayOf(4.0, 250.0, 287.0, null))
    flightList.addEdge("Philadelphia", "Boston", arrayOf(1.3, null, null, 351.0))

    // from Miami
    flightList.addEdge("Miami", "New York", arrayOf(2.8, null, 167.0, 87.0))
    flightList.addEdge("Miami", "Los Angeles", arrayOf(5.7, null, null, 113.0))
    flightList.addEdge("Miami", "San Francisco", arrayOf(6.2, null, 259.0, null))
    flightList.addEdge("Miami", "Chicago", arrayOf(3.3, 197.0, 353.0, 54.0))
    flightList.addEdge("Miami", "Houston", arrayOf(2.8, 249.0, 417.0, 48.0))
    flightList.addEdge("Miami", "Dallas", arrayOf(3.0, 170.0, null, 49.0))
    flightList.addEdge("Miami", "Austin", arrayOf(3.0, 154.0, null, null))
    flightList.addEdge("Miami", "Phoenix", arrayOf(5.0, null, null, 324.0))
    flightList.addEdge("Miami", "Philadelphia", arrayOf(2.7, null, null, 39.0))
    flightList.addEdge("Miami", "Denver", arrayOf(4.5, 250.0, 499.0, null))

    // from Seattle
    flightList.addEdge("Seattle", "New York", arrayOf(5.2, null, 463.0, null))
    flightList.addEdge("Seattle", "Los Angeles", arrayOf(2.7, 199.0, 257.0, null))
    flightList.addEdge("Seattle", "San Diego", arrayOf(2.7, 159.0, null, null))
    flightList.addEdge("Seattle", "San Francisco", arrayOf(2.2, null, 197.0, null))
    flightList.addEdge("Seattle", "Chicago", arrayOf(4.0, 239.0, 355.0, null))
    flightList.addEdge("Seattle", "Houston", arrayOf(4.2, null, 407.0, null))
    flightList.addEdge("Seattle", "Dallas", arrayOf(3.8, 289.0, null, null))
    flightList.addEdge("Seattle", "Phoenix", arrayOf(2.8, 165.0, null, null))
    flightList.addEdge("Seattle", "Denver", arrayOf(2.7, 189.0, 237.0, null))

    // from Denver
    flightList.addEdge("Denver", "New York", arrayOf(3.7, 209.0, 296.0, null))
    flightList.addEdge("Denver", "Los Angeles", arrayOf(2.5, 165.0, 329.0, null))
    flightList.addEdge("Denver", "San Diego", arrayOf(2.3, 159.0, 277.0, null))
    flightList.addEdge("Denver", "San Francisco", arrayOf(2.7, 179.0, 297.0, null))
    flightList.addEdge("Denver", "Chicago", arrayOf(2.3, 143.0, 225.0, null))
    flightList.addEdge("Denver", "Houston", arrayOf(2.3, 154.0, 307.0, null))
    flightList.addEdge("Denver", "San Antonio", arrayOf(2.0, 209.0, 417.0, null))
    flightList.addEdge("Denver", "Dallas", arrayOf(1.8, 134.0, 267.0, null))
    flightList.addEdge("Denver", "Austin", arrayOf(2.0, 154.0, 287.0, null))
    flightList.addEdge("Denver", "Phoenix", arrayOf(1.8, 154.0, 287.0, null))
    flightList.addEdge("Denver", "Philadelphia", arrayOf(3.5, 250.0, 287.0, null))
    flightList.addEdge("Denver", "Miami", arrayOf(3.8, 250.0, 499.0, null))
    flightList.addEdge("Denver", "Seattle", arrayOf(2.8, 189.0, 237.0, null))
    flightList.addEdge("Denver", "Boston", arrayOf(3.8, 240.0, 282.0, null))

    // from Boston
    flightList.addEdge("Boston", "New York", arrayOf(1.3, null, 173.0, 222.0))
    flightList.addEdge("Boston", "Los Angeles", arrayOf(6.2, null, 380.0, 182.0))
    flightList.addEdge("Boston", "San Francisco", arrayOf(6.5, null, 273.0, null))
    flightList.addEdge("Boston", "Chicago", arrayOf(2.8, 179.0, 220.0, 331.0))
    flightList.addEdge("Boston", "Houston", arrayOf(4.2, null, 297.0, 338.0))
    flightList.addEdge("Boston", "Dallas", arrayOf(3.8, 194.0, null, 263.0))
    flightList.addEdge("Boston", "Austin", arrayOf(4.5, null, null, 395.0))
    flightList.addEdge("Boston", "Philadelphia", arrayOf(1.5, null, null, 351.0))
    flightList.addEdge("Boston", "Denver", arrayOf(4.5, 240.0, 282.0, null))



    // using functions
//    flightList.printFullMap()
    //random lil tests
    println(flightList.shortestPath("Houston", "Boston", "Southwest")) //looking for shortest path (well ig cheapest) btwn places that don't have direct
    println(flightList.shortestPath("Boston", "Houston", "Southwest"))
    println(flightList.shortestPath("Boston", "Seattle", "United"))
    println(flightList.shortestPath("New York", "Philadelphia", "Spirit"))
    println(flightList.shortestPath("Miami", "Seattle", "Ratio"))
    println(flightList.shortestPath("Boston", "Houston", "Ratio"))
    println(flightList.shortestPath("Houston", "Boston", "Ratio")) //this is the one that originally tweaked out, fixed w/ the ratio fix part
    println()

    //across whole country, a path none of them have direct
    println("Various tests of Los Angeles -> Philadelphia")
    println("Southwest:")
    println(flightList.shortestPath("Los Angeles", "Philadelphia", "Southwest"))
    println("United:")
    println(flightList.shortestPath("Los Angeles", "Philadelphia", "United"))
    println("Spirit:")
    println(flightList.shortestPath("Los Angeles", "Philadelphia", "Spirit"))
    println("Any:")
    println(flightList.shortestPath("Los Angeles", "Philadelphia", "Any"))
    println("Ratio:")
    println(flightList.shortestPath("Los Angeles", "Philadelphia", "Ratio")) //infinite looped before, fixed now!
    println()

    // user input part now, making it a loop that repeats until cancelled
    println("Welcome! This is my program for finding the most cost-efficient path between two cities based on your preferred parameters. ")
    println("Follow the prompts and type 'Cancel' into the first prompt whenever you're done!")
    println()
    while (true) {
        println("Where are you departing from? (pls capitalize first letter) ")
        val userStart = readln()
        if (userStart == "Cancel"){
            break
        }
        println("Where are you headed? ")
        val userGoal = readln()
        println("What airline do you prefer? ")
        println("[Southwest, United, Spirit, Any, or Ratio (price / distance)] ")
        val userPref = readln()
        println("Recommended path:")
        println(flightList.shortestPath(userStart, userGoal, userPref))
        println()
    }
    println("Thank you! Have a nice day :)")
}