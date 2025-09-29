interface LinkedList<T> {
    /**
     * Adds the element [data] to the front of the linked list.
     */
    fun pushFront(data: T)

    /**
     * Adds the element [data] to the back of the linked list.
     */
    fun pushBack(data: T)

    /**
     * Removes an element from the front of the list. If the list is empty, it is unchanged.
     * @return the value at the front of the list or nil if none exists
     */
    fun popFront(): T?

    /**
     * Removes an element from the back of the list. If the list is empty, it is unchanged.
     * @return the value at the back of the list or nil if none exists
     */
    fun popBack(): T?

    /**
     * @return the value at the front of the list or nil if none exists
     */
    fun peekFront(): T?

    /**
     * @return the value at the back of the list or nil if none exists
     */
    fun peekBack(): T?

    /**
     * @return true if the list is empty and false otherwise
     */
    fun isEmpty(): Boolean
}

class MyLinkedList<T> : LinkedList<T> {
    class ListNode<T>(val data: T, var next: ListNode<T>?, var prev: ListNode<T>?)
    private var front: ListNode<T>? = null
    private var back: ListNode<T>? = null

    override fun pushFront(data: T) {
        if (front == null) {
            front = ListNode(data, null, null)
            back = front
        }
        else{
            val newNode = ListNode(data, next = front, prev = null)
            front?.prev = newNode
            front = newNode
        }
    }

    override fun pushBack(data: T) {
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

    override fun popFront(): T? {
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

    override fun popBack(): T? {
        val oldBack = back ?: return null
        back = oldBack.prev
        if (back == null){
            back = null
        }
        else {
            front?.prev = null
        }
        return oldBack.data
    }

    override fun peekFront(): T? {
        return front?.data
    }

    override fun peekBack(): T? {
        return back?.data
    }

    override fun isEmpty(): Boolean {
        return front == null
    }

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

class MyStack<T> {
    private val list = MyLinkedList<T>()
    /**
     * Add [data] to the top of the stack
     */
    fun push(data: T){
        list.pushFront(data)
    }
    /**
     * Remove the element at the top of the stack.  If the stack is empty, it remains unchanged.
     * @return the value at the top of the stack or nil if none exists
     */
    fun pop(): T?{
        return list.popFront()
    }
    /**
     * @return the value on the top of the stack or nil if none exists
     */
    fun peek(): T?{
        return list.peekFront()
    }
    /**
     * @return true if the stack is empty and false otherwise
     */
    fun isEmpty(): Boolean{
        return list.isEmpty()
    }

    fun reverse(): MyStack<T> {
        val newList = MyStack<T>()
        while(!this.isEmpty()){
            newList.push(this.pop()!!)
        }
        return newList
    }

    override fun toString(): String {
        return list.toString()
    }
}

class MyQueue<T> {
    private val list = MyLinkedList<T>()
    /**
     * Add [data] to the end of the queue.
     */
    fun enqueue(data: T){
        list.pushBack(data)
    }
    /**
     * Remove the element at the front of the queue.  If the queue is empty, it remains unchanged.
     * @return the value at the front of the queue or nil if none exists
     */
    fun dequeue(): T?{
        return list.popFront()
    }
    /**
     * @return the value at the front of the queue or nil if none exists
     */
    fun peek(): T?{
        return list.peekFront()
    }
    /**
     * @return true if the queue is empty and false otherwise
     */
    fun isEmpty(): Boolean{
        return list.isEmpty()
    }

    override fun toString(): String {
        return list.toString()
    }
}

fun main(){
    val testStack = MyStack<Int>()
    println(testStack.isEmpty())
    testStack.push(1)
    testStack.push(2)
    testStack.push(3)
    println(testStack.peek())
    println(testStack)
    println(testStack.reverse())
    println(testStack.isEmpty())
    println(testStack.pop())

    val testQueue = MyQueue<Int>()
    println(testQueue.isEmpty())
    testQueue.enqueue(1)
    testQueue.enqueue(2)
    testQueue.enqueue(3)
    println(testQueue.peek())
    println(testQueue)
    println(testQueue.isEmpty())
    println(testQueue.dequeue())
}