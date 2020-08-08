package com.example.foodies.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodies.R
import com.example.foodies.model.FoodItem

class CartItemAdapter(
    private val cartList: ArrayList<FoodItem>, val context: Context
): RecyclerView.Adapter<CartItemAdapter.CartViewHolder>(){

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CartViewHolder {

        val view= LayoutInflater.from(parent.context).inflate(R.layout.layout_cart_item,parent,false)
        return  CartViewHolder(view)

    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: CartItemAdapter.CartViewHolder, position: Int) {

        val cartObject= cartList[position]
        holder.itemName.text= cartObject.name
        val cost= "${cartObject.cost?.toString()}"
        holder.itemCost.text= cost
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemName: TextView= view.findViewById(R.id.txtCartItemName)
        val itemCost: TextView= view.findViewById(R.id.txtCartPrice)

    }
}