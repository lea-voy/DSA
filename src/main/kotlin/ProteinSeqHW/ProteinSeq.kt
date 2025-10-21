package ProteinSeqHW

import kotlin.Array

fun NeedWun(string1: String, string2: String){
    //setting up score and direction matrices
    val rows = string1.length
    val cols = string2.length
    val scores = Array(rows+1) { IntArray(cols+1) }
    val directions = Array(rows+1) { Array(cols+1) {""} }

    //setting up that first row and column
    // for direction, U = Up, L = Left, and D = Diagonal
    for (i in 0 until rows+1){
        scores[i][0] = -1 * i
        directions[i][0] = "U"
    }

    for (j in 0 until cols+1){
        scores[0][j] = -1 * j
        directions[0][j] = "L"
    }

    directions[0][0] = "0"

    //now the whole filling out the table
    for (i in 1..rows){
        for (j in 1..cols){
            //check if the positions in the strings are matched, get a +1 or -1 depending
            var diagMatch = 0
            if (string1[i-1] == string2[j-1]){diagMatch = 1}
            else{diagMatch = -1}

            //find the scores of the 3 adjacent (left, up, and diagonal)
            val diagScore = scores[i-1][j-1] + diagMatch
            val upScore = scores[i-1][j] - 1
            val leftScore = scores[i][j-1] - 1

            //check which one is biggest
            val maxScore = maxOf(diagScore, upScore, leftScore)
            scores[i][j] = maxScore

            //update which direction that was
            if (maxScore == diagScore){directions[i][j] = "D"}
            else if (maxScore == upScore){directions[i][j] = "U"}
            else {directions[i][j] = "L"}
        }
    }
    //print the final score now
    println("Alignment score: ${scores[rows][cols]}")

    //now backtracking through directions to get the alignment
    val alignedString1 = StringBuilder()
    val alignedString2 = StringBuilder()

    var i = rows
    var j = cols
    while(i > 0 || j > 0){
        if((directions[i][j] == "D") && i > 0 && j > 0){ //the extra checks are in case one runs out before the other and it gets confused
            alignedString1.append(string1[i-1])
            alignedString2.append(string2[j-1])
            i--
            j--
        }
        else if((directions[i][j] == "U" || j == 0) && i > 0){ //again, checks if one runs out, makes the rest go to -
            alignedString1.append(string1[i-1])
            alignedString2.append("-")
            i--
        }
        else if((directions[i][j] == "L" || i == 0) && j > 0){
            alignedString1.append("-")
            alignedString2.append(string2[j-1])
            j--
        }
        else{
            break
        }
    }

    //now print those alignments (with reverse function waow)
    println("Alignment 1: ${alignedString1.reverse()}")
    println("Alignment 2: ${alignedString2.reverse()}")
}

fun main(){
    val genomeSnippet = "TGGCGACAACCGTAGCGGAATATTTTCGCGACCAGGGAAAACGGGTCGTGCTTTTTATCGATTCCATGACCCGTTATGCGCGTGCTTTGCGAGACGTGGCACTGGCGTCGGGAGAGCGTCCGGCTCGTCGAGGTTATCCCGCCTCCGTATTCGATAATTTGCCCCGCTTGCTGGAACGCCCAGGGGCGACCAGCGAGGGAAGCATTACTGCCTTTTATACGGTACTGCTGGAAAGCGAGGAAGAGGCGGACCCGATGGCGGATGAAATTCGCTCTATCCTTGACGGTCACCTGTATCTGAGCAGAAAGCTGGCCGGGCAGGGACATTACCCGGCAATCGATGTACTGAAAAGCGTAAGCCGCGTTTTT"
    val testAgainst = "TGGCCACCACGATAGCAGAATTTTTTCGCGATAATGGAAAGCGAGTCGTCTTGCTTGCCGACTCACTGACGCGTTATGCCAGGGCCGCACGGGAAATCGCTCTGGCCGCCGGAGAGACCGCGGTTTCTGGAGAATATCCGCCAGGCGTATTTAGTGCATTGCCACGACTTTTAGAACGTACGGGAATGGGAGAAAAAGGCAGTATTACCGCATTTTATACGGTACTGGTGGAAGGCGATGATATGAATGAGCCGTTGGCGGATGAAGTCCGTTCACTGCTTGATGGACATATTGTACTATCCCGACGGCTTGCAGAGAGGGGGCATTATCCTGCCATTGACGTGTTGGCAACGCTCAGCCGCGTTTTT"

    NeedWun(genomeSnippet, testAgainst)
}