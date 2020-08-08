package com.example.foodies.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.foodies.R
import com.example.foodies.model.FoodItem

class RestaurantMenuAdapter(
    val context: Context, private val menuList: ArrayList<FoodItem>,
    private val listener: OnItemClickListener
):
    RecyclerView.Adapter<RestaurantMenuAdapter.MenuViewHolder>() {

    companion object {
        var isCartEmpty= true
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RestaurantMenuAdapter.MenuViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycler_restaurant_menu_custom_row, parent, false)
        return MenuViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    interface OnItemClickListener {
        fun onAddItemClick(foodItem: FoodItem)
        fun onRemoveItemClick(foodItem: FoodItem)
    }

    override fun onBindViewHolder(holder: RestaurantMenuAdapter.MenuViewHolder, position: Int) {

        val menuObject = menuList[position]
        holder.foodItemName.text = menuObject.name
        val cost = "Rs. ${menuObject.cost}"
        holder.foodItemCost.text = cost
        holder.sno.text = (position + 1).toString()

        holder.addToCart.setOnClickListener {
            holder.addToCart.visibility = View.GONE
            holder.removeFromCart.visibility = View.VISIBLE
            listener.onAddItemClick(menuObject)
        }

        holder.removeFromCart.setOnClickListener {
            holder.removeFromCart.visibility = View.GONE
            holder.addToCart.visibility = View.VISIBLE
            listener.onRemoveItemClick(menuObject)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodItemName: TextView = view.findViewById(R.id.txtItemName)
        val foodItemCost: TextView = view.findViewById(R.id.txtItemCost)
        val sno: TextView = view.findViewById(R.id.txtSNo)
        val addToCart: Button = view.findViewById(R.id.btnAddToCart)
        val removeFromCart: Button = view.findViewById(R.id.btnRemoveFromCart)
    }
}