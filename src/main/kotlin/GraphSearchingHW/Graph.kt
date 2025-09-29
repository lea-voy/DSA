package GraphSearchingHW

/**
 * ``Graph`` represents a directed graph
 * @param VertexType the type that represents a vertex in the graph
 */
// Question -- is it required to set these up as interfaces? or can we just do classes?
class Graph<VertexType> {
    // set of all vertices we have
    var vertices: MutableSet<VertexType> = mutableSetOf()
    // map where the vertices are the keys and the values are another map, that one holding all neighbors+weights
    var edges: MutableMap<VertexType, MutableMap<VertexType, Double>> = mutableMapOf()

    /**
     * @return the vertices in the graph
     */
    //it got very angry at me for trying to use this name so gonna write it wrong here
    fun getVertixes(): Set<VertexType>{
        return vertices
    }

    /**
     * Add an edge between [from] and [to] with edge weight [cost]
     */
    fun addEdge(from: VertexType, to: VertexType, cost: Double){
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
    fun getEdges(from: VertexType): Map<VertexType, Double>{
        // i mean.. it's just returning all the edges connected to the from
        if(edges.containsKey(from)) {
            return edges[from]!!
        }
        else{
            return emptyMap()
        }
    }

    /**
     * Prints all edges + their weights
     */
    fun printFullMap(){
        // I thought the last one would do this but then realized it was asking something else but I still wanted a way to see all so here it is
        // Prints it all out very nice with A -> B, weight = x
        for ((from, neighbors) in edges){
            for ((to, weight) in neighbors){
                println("$from -> $to, weight = $weight")
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
     * The whole thing with finding the shortest paths
     */
    fun Dijskras(start: VertexType, goal: VertexType): List<VertexType>?{
        val currentDist = mutableMapOf<VertexType, Double>()
        val prev = mutableMapOf<VertexType, VertexType?>()
        val queue = MinPriorityQueue<VertexType>()

        // make distances very big so they're set but not confounding
        for (vertix in vertices) {
            currentDist[vertix] = Double.POSITIVE_INFINITY //why is it screaming at me
            prev[vertix] = null
        }
        // add the start to the beginning of the queue with distance 0 and priority 0
        currentDist[start] = 0.0
        queue.addWithPriority(start, 0.0)

        while (!queue.isEmpty()) {
            // go through our map
            val u = queue.next() ?: break

            if (u == goal) {
                // if we reached goal, build the path now!
                val path = mutableListOf<VertexType>()
                var curr: VertexType? = goal
                while (curr != null) {
                    path.add(0, curr)
                    curr = prev[curr]
                }
                return path
            }
            // check all neighbors and put them in the queue with their weights
            val neighbors = getEdges(u)
            for ((v, weight) in neighbors) {
                val newDist = currentDist[u]!! + weight
                if (newDist < currentDist[v]!!) {
                    currentDist[v] = newDist
                    prev[v] = u
                    queue.addWithPriority(v, newDist)
                }
            }
        }

        return null // no path
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
     * Add [elem] with at level [priority]
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
     *   the lower the priority the earlier the element int
     *   the order.
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

            if (heap[i].second < heap[parent].second) {
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
    // basic testing of the graph
    val tester = Graph<String>()
    tester.addEdge("A", "B", 4.5)
    tester.addEdge("B", "C", 2.0)
    tester.addEdge("B", "D", 3.33)
    tester.addEdge("C", "E", 1.0)
    tester.addEdge("C", "F", 7.0)
    tester.addEdge("D", "F", 13.0)
    println(tester.getVertixes())
    println(tester.getEdges("B"))
    println(tester.getEdges("C"))
    tester.printFullMap()
    println("now clearing")
    tester.clear()
    tester.printFullMap()

    // examples with cities (sorta) -- I'm gonna put places across the country where I have friends+family and use it to see the shortest to get between places if I were to "crash" at friends' places along the way
    val friendList = Graph<String>()
    // i'm gonna separate driving distance and then also airports for those I have to fly to because I feel like that will be fun
    friendList.addEdge("Lea", "Kennedy", 2.6)
    friendList.addEdge("Lea", "Anna", 51.0)
    friendList.addEdge("Anna", "Alex F", 19.0)
    friendList.addEdge("Lea", "Lily", 5.7)
    // the 2 airports in Houston
    friendList.addEdge("Lea", "HOU", 32.0)
    friendList.addEdge("Lea", "IAH", 34.0)
    // ones that go to chicago midway
    friendList.addEdge("HOU", "MDW", 937.0)
    friendList.addEdge("MDW", "Nicole", 43.0)
    friendList.addEdge("MDW", "Babushka", 39.0)
    friendList.addEdge("Babushka", "Yana", 12.0)
    friendList.addEdge("Babushka", "Nicole", 4.3)
    friendList.addEdge("Nicole", "Yana", 8.2)
    // then from chicago to boston for college bc southwest never has direct lol
    friendList.addEdge("MDW", "BOS", 862.0)
    friendList.addEdge("BOS", "Olin", 20.0)
    friendList.addEdge("BOS", "Brooke", 5.1)
    friendList.addEdge("Olin", "Brooke", 15.0)
    // only 1 person there but miami can also be a stop to boston
    friendList.addEdge("IAH", "MIA", 965.0)
    friendList.addEdge("MIA", "Alex I", 22.0)
    friendList.addEdge("MIA", "BOS", 1261.0)

    //ok finally gonna use the function, i feel like answers will be very simple bc a lot of things are connected
    //yeah they were rlly boring so i deleted some connections straight from airports to have a couple stops lol
    println(friendList.Dijskras("Lea", "Alex F"))
    println(friendList.Dijskras("Lea", "Olin"))
    println(friendList.Dijskras("Lea", "Yana"))
}