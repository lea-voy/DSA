package class5

class MutableIntList {
    var data = Array<Int?>(size=10, {null})
    var size = 0 // more effective method, keep track of what your size is instead of checking and parsing
    /**
     * Add [element] to the end of the list
     */
    fun add(element: Int) {
        var availableSpace: Int? = null
        for (i in 0..this.data.size){
            if (this.data[i] == null) {
                availableSpace = i
                break
            }
        }
        if (availableSpace != null) {
            this.data[availableSpace] = element
        }
        else{
            var newList = arrayOfNulls<Int>((this.data.size*2))
            for (i in 0..this.size()){
                newList[i] = this[i]
            }
            data = newList
            add(element)
        }
    }


//    /**
//     * Remove all elements from the list
//     */
//    fun clear(){
//        for (i in 0..this.size()){
//            // smth is up here ill figure out later
//            // this[i] = null
//            this = null
//        }
//    }

    /**
     * @return the size of the list
     */
    fun size(): Int{
        return this.size()
    }

    /**
     * @param index the index to return
     * @return the element at [index]
     */
    operator fun get(index: Int): Int{
        return this[index]
    }

    /**
     * Store [value] at position [index]
     * @param index the index to set
     * @param value to store at [index]
     */
    operator fun set(index: Int, value: Int){
        this[index] = value
    }
}

fun main(){
    var testList = MutableIntList()
    testList.add(5)
    println(testList.size())
}