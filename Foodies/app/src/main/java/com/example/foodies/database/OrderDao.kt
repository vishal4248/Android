package com.example.foodies.database

import androidx.room.*


@Dao
interface OrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(orderEntity: OrderEntity)

    @Delete
    fun deleteOrder(orderEntity: OrderEntity)

    @Query("SELECT * FROM orders")
    fun getAllOrders(): List<OrderEntity>

    @Query("DELETE FROM orders WHERE  resId= :resId")
    fun deleteOrders(resId: String)
}