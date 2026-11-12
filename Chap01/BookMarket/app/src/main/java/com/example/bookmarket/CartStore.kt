package com.example.bookmarket

data class CartItem(
    val id: String,
    val title: String,
    val price: Int,
    val cover: Int,
    var quantity: Int = 1
) {
    val subtotal: Long
        get() = price.toLong() * quantity
}

object CartStore {
    private val cartItems = mutableListOf<CartItem>()

    val items: List<CartItem>
        get() = cartItems.map { it.copy() }

    val total: Long
        get() = cartItems.sumOf { it.subtotal }

    fun add(
        id: String,
        title: String,
        price: Int,
        cover: Int
    ) {
        val existing = cartItems.find { it.id == id }

        if (existing == null) {
            cartItems.add(
                CartItem(id, title, price, cover)
            )
        } else {
            existing.quantity += 1
        }
    }

    fun remove(id: String) {
        cartItems.removeAll { it.id == id }
    }

    fun clear() {
        cartItems.clear()
    }
}