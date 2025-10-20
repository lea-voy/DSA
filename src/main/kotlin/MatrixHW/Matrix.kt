package MatrixHW
import kotlin.random.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

class Matrix(val n: Int) {
    // each matrix has the double array of its data
    val data: Array<DoubleArray> = Array(n) { DoubleArray(n) }

    /**
     * Set the value of a given position in the matrix (pass in row, col, and value)
     */
    fun set(row: Int, col: Int, value: Double) {
        data[row][col] = value
    }

    /**
     * Return the value of a given position in the matrix (pass in row, col)
     */
    fun get(row:Int, col: Int): Double {
        return data[row][col]
    }

    /**
     * Break the matrix up into 4 n/2 matrices, then return them as a list
     */
    fun split(): List<Matrix> {
        val A11 = Matrix(n/2)
        val A12 = Matrix(n/2)
        val A21 = Matrix(n/2)
        val A22 = Matrix(n/2)

        for (i in 0 until n/2) {
            for (j in 0 until n / 2) {
                A11.data[i][j] = data[i][j]
                A12.data[i][j] = data[i][j + (n / 2)]
                A21.data[i][j] = data[i + (n / 2)][j]
                A22.data[i][j] = data[i + (n / 2)][j + (n / 2)]
            }
        }
        return listOf(A11, A12, A21, A22)
    }

    /**
     * Put the 4 matrix fragments back together, pretty much just reverse of split
     */
    fun recombine(C11: Matrix, C12: Matrix, C21: Matrix, C22: Matrix): Matrix {
        val halfLength = C11.n
        val bigMatrix = Matrix(halfLength*2)
        for (i in 0 until halfLength){
            for (j in 0 until halfLength){
                bigMatrix.data[i][j] = C11.data[i][j]
                bigMatrix.data[i][j+halfLength] = C12.data[i][j]
                bigMatrix.data[i+halfLength][j] = C21.data[i][j]
                bigMatrix.data[i+halfLength][j+halfLength] = C22.data[i][j]
            }
        }
        return bigMatrix
    }

    /**
     * Add other matrix to current one (will be called on current and pass in the second)
     */
    fun add(other: Matrix): Matrix {
        val sum = Matrix(n)
        for (i in 0 until n){
            for (j in 0 until n){
                sum.data[i][j] = data[i][j] + other.data[i][j]
            }
        }
        return sum
    }

    /**
     * Subtract other matrix from current one (will be called on current and pass in the second)
     */
    fun subtract(other: Matrix): Matrix {
        val difference = Matrix(n)
        for (i in 0 until n){
            for (j in 0 until n){
                difference.data[i][j] = data[i][j] - other.data[i][j]
            }
        }
        return difference
    }

    /**
     * Multiply current matrix by another one (again, will be called on current and pass in the second)
     */
    fun multiply(other: Matrix): Matrix? {
        if (n != other.n) {
            println("Matrices are not the same size, cannot multiply")
            return null
        }

        //time for recursion.... base case first
        if (n == 1){
            val product = Matrix(n)
            product.data[0][0] = data[0][0] * other.data[0][0]
            return product
        }

        //now split and recursively multiply and add  y   a  y
        val (A11, A12, A21, A22) = split()
        val (B11, B12, B21, B22) = other.split()
        //see below for what checkMultiply is, I hate it here why are these null checkers so mean
        val C11 = (A11.multiply(B11))?.add(checkMultiply(A12,B21)?: return null)
        val C12 = (A11.multiply(B12))?.add(checkMultiply(A12,B22)?: return null)
        val C21 = (A21.multiply(B11))?.add(checkMultiply(A22,B21)?: return null)
        val C22 = (A22.multiply(B12))?.add(checkMultiply(A22,B22)?: return null)
        //i am going to lose my mind there are sO MANY CHECKS
        return recombine(C11!!, C12!!, C21!!, C22!!)
    }

    //bc I have some things returning Matrix? instead of just Matrix, kotlin yells at me for trying to multiply/add/wtv them, so here's one that automatically checks to appease it
    fun checkMultiply(a: Matrix?, b: Matrix?): Matrix? {
        if (a == null || b == null) {return null}
        return a.multiply(b)
    }

    /**
     * Multiply current matrix by another one but strassen edition (again, will be called on current and pass in the second)
     */
    fun strassenMultiply(other: Matrix): Matrix? {
        //same initial setup
        if (n != other.n) {
            println("Matrices are not the same size, cannot multiply")
            return null
        }

        if (n == 1){
            val product = Matrix(n)
            product.data[0][0] = data[0][0] * other.data[0][0]
            return product
        }

        val (A11, A12, A21, A22) = split()
        val (B11, B12, B21, B22) = other.split()

        //now for the very strange strassen equations
        val M1 = (A11.add(A22)).strassenMultiply(B11.add(B22))
        val M2 = (A21.add(A22)).strassenMultiply(B11)
        val M3 = A11.strassenMultiply(B12.subtract(B22))
        val M4 = A22.strassenMultiply(B21.subtract(B11))
        val M5 = (A11.add(A12)).strassenMultiply(B22)
        val M6 = (A21.subtract(A11)).strassenMultiply(B11.add(B12))
        val M7 = (A12.subtract(A22)).strassenMultiply(B21.add(B22))
        //just using a bunch of exclamation points this time
        val C11 = ((M1!!.add(M4!!)).subtract(M5!!)).add(M7!!)
        val C12 = M3!!.add(M5)
        val C21 = M2!!.add(M4)
        val C22 = ((M1.subtract(M2)).add(M3)).add(M6!!)

        return recombine(C11, C12, C21, C22)
    }
}

fun main(){
    //making a bunch of random matrices of various sizes
    val vSmallTestMatrix = Matrix(4)
    val vSmallTestMatrix2 = Matrix(4)
    for (i in 0 until 4){
        for (j in 0 until 4){
            vSmallTestMatrix.set(i, j, Random.nextDouble(100.0))
            vSmallTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    val smallTestMatrix = Matrix(10)
    val smallTestMatrix2 = Matrix(10)
    for (i in 0 until 10){
        for (j in 0 until 10){
            smallTestMatrix.set(i, j, Random.nextDouble(100.0))
            smallTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    val mediumTestMatrix = Matrix(50)
    val mediumTestMatrix2 = Matrix(50)
    for (i in 0 until 50){
        for (j in 0 until 50){
            mediumTestMatrix.set(i, j, Random.nextDouble(100.0))
            mediumTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    val largeTestMatrix = Matrix(100)
    val largeTestMatrix2 = Matrix(100)
    for (i in 0 until 100){
        for (j in 0 until 100){
            largeTestMatrix.set(i, j, Random.nextDouble(100.0))
            largeTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    val vLargeTestMatrix = Matrix(1000)
    val vLargeTestMatrix2 = Matrix(1000)
    for (i in 0 until 1000){
        for (j in 0 until 1000){
            vLargeTestMatrix.set(i, j, Random.nextDouble(100.0))
            vLargeTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    val vvLargeTestMatrix = Matrix(3000)
    val vvLargeTestMatrix2 = Matrix(3000)
    for (i in 0 until 3000){
        for (j in 0 until 3000){
            vvLargeTestMatrix.set(i, j, Random.nextDouble(100.0))
            vvLargeTestMatrix2.set(i, j, Random.nextDouble(100.0))
        }
    }

    // going through and timing the diff sizes, probably a more efficient way to write but this is good enough for me
    val multiplicationRunTimes = mutableListOf<Double>()
    var runTime = measureTime{
        vSmallTestMatrix.multiply(vSmallTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        smallTestMatrix.multiply(smallTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        mediumTestMatrix.multiply(mediumTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        largeTestMatrix.multiply(largeTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        vLargeTestMatrix.multiply(vLargeTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        vvLargeTestMatrix.multiply(vvLargeTestMatrix2)
    }
    multiplicationRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    println("Runtimes for regular matrix multiplication are $multiplicationRunTimes")


    //and now again with strassen multiplication
    val strassenRunTimes = mutableListOf<Double>()
    runTime = measureTime{
        vSmallTestMatrix.strassenMultiply(vSmallTestMatrix2)
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        smallTestMatrix.strassenMultiply(smallTestMatrix2)
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        mediumTestMatrix.strassenMultiply(mediumTestMatrix2)
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        largeTestMatrix.strassenMultiply(largeTestMatrix2)
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        vLargeTestMatrix.strassenMultiply(vLargeTestMatrix2)
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    runTime = measureTime{
        vvLargeTestMatrix.strassenMultiply((vvLargeTestMatrix2))
    }
    strassenRunTimes.add(runTime.toDouble(DurationUnit.SECONDS))

    println("Runtimes for Strassen matrix multiplication are $strassenRunTimes")

    //calling the get functions to confirm they work just so I can have them all tested
    println("first vals of 2 test matrices summed:")
    println(vSmallTestMatrix.get(1, 1))
    println(vSmallTestMatrix2.get(1, 1))
    val testSum = vSmallTestMatrix.add(vSmallTestMatrix2)
    println(testSum.get(1,1))
}