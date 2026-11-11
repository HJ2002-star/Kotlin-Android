package com.example.bookmarket

fun main() {
    val numbers = mutableListOf(1, 2, 3).also { list ->
        println("리스트의 크기: ${list.size}")
        list.add(4)
    }

    println(numbers)
}