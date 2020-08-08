package com.example.foodies.model

import org.json.JSONArray

data class OrderDetails (
    val  orderId: Int,
    val resName: String,
    val totalCost: String,
    val orderDate: String,
    val foodItem: JSONArray
)