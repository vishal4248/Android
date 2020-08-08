package com.example.foodies.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.adapter.CartItemAdapter
import com.example.foodies.adapter.RestaurantMenuAdapter
import com.example.foodies.database.OrderEntity
import com.example.foodies.database.RestaurantDatabase
import com.example.foodies.fragment.RestaurantMenuFragment
import com.example.foodies.model.FoodItem
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject


//Cart Activity which is used to kept the orderList for placing an order

class CartActivity : AppCompatActivity() {

    private lateinit var cartItemAdapter: CartItemAdapter
    private lateinit var recyclerCart: RecyclerView
    private var orderList = ArrayList<FoodItem>()
    private lateinit var txtResName: TextView
    private lateinit var toolbar: Toolbar
    private lateinit var rlLoading: RelativeLayout
    private lateinit var rlCart: RelativeLayout
    private lateinit var btnPlaceOrder: Button
    private var resId: Int = 0
    private var resName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_cart)

        rlLoading = findViewById(R.id.rlLoading)
        rlCart = findViewById(R.id.rlCart)
        txtResName = findViewById(R.id.txtCartResName)
        txtResName.text = RestaurantMenuFragment.resName

        val bundle = intent.getBundleExtra("data")
        resId = bundle?.getInt("resId", 0) as Int
        resName = bundle.getString("resName", "") as String

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "My Cart"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setTitleTextAppearance(this,R.style.AppTheme)


        setUpCartList()

        placeOrder()
    }


    private fun setUpCartList() {
        recyclerCart = findViewById(R.id.recyclerCartItems)
        val dbList = GetItemsFromDBAsync(this,resId.toString()).execute().get()
        for (element in dbList) {
            orderList.addAll(
                Gson().fromJson(element.foodItems, Array<FoodItem>::class.java).asList()
            )
        }
        if (orderList.isEmpty()) {
            rlCart.visibility = View.GONE
            rlLoading.visibility = View.VISIBLE
            Toast.makeText(this, "Some error occurred, your cart is empty", Toast.LENGTH_SHORT).show()
        } else {
            rlCart.visibility = View.VISIBLE
            rlLoading.visibility = View.GONE
        }
        cartItemAdapter = CartItemAdapter(orderList, this@CartActivity)
        val mLayoutManager = LinearLayoutManager(this@CartActivity)
        recyclerCart.layoutManager = mLayoutManager
        recyclerCart.itemAnimator = DefaultItemAnimator()
        recyclerCart.adapter = cartItemAdapter
    }

    private fun placeOrder() {

        btnPlaceOrder = findViewById(R.id.btnConfirmOrder)
        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost as Int
        }
        val total = "Place Order(Total: Rs. $sum)"
        btnPlaceOrder.text = total
        btnPlaceOrder.setOnClickListener {
            rlLoading.visibility = View.VISIBLE
            rlCart.visibility = View.INVISIBLE
            sendServerRequest()
        }
    }

    private fun sendServerRequest() {
        val queue = Volley.newRequestQueue(this@CartActivity)
        val jsonParams = JSONObject()

        jsonParams.put(
            "user_id",
            this.getSharedPreferences("login_details", Context.MODE_PRIVATE).getString("user_id", "") as String
        )

        jsonParams.put("restaurant_id", RestaurantMenuFragment.resId?.toString() as String)

        var sum = 0
        for (i in 0 until orderList.size) {
            sum += orderList[i].cost as Int
        }
        jsonParams.put("total_cost", sum.toString())

        val foodArray = JSONArray()
        for (i in 0 until orderList.size) {
            val foodId = JSONObject()
            foodId.put("food_item_id", orderList[i].id)
            foodArray.put(i, foodId)
        }
        jsonParams.put("food", foodArray)

        val placeOrderUrl= "http://13.235.250.119/v2/place_order/fetch_result/"

        val jsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST,
                placeOrderUrl,
                jsonParams,
                Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")

                    /*If order is placed, clear the DB for the recently added items
                     Once the DB is cleared, notify the user that the order has been placed*/

                    if (success) {
                            ClearDBAsync(this, resId.toString()).execute().get()
                            RestaurantMenuAdapter.isCartEmpty = true

                        val dialog = Dialog(
                            this@CartActivity,
                            android.R.style.Theme_Black_NoTitleBar_Fullscreen
                        )
                        dialog.setContentView(R.layout.order_placed_dialog)
                        dialog.show()
                        dialog.setCancelable(false)

                        val btnOk = dialog.findViewById<Button>(R.id.btnOk)
                        btnOk.setOnClickListener {
                            dialog.dismiss()

                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        rlCart.visibility = View.VISIBLE
                        Toast.makeText(this@CartActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    rlCart.visibility = View.VISIBLE
                    e.printStackTrace()
                }
            }, Response.ErrorListener {
                rlCart.visibility = View.VISIBLE
                Toast.makeText(this@CartActivity, it.message, Toast.LENGTH_SHORT).show()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "cc2a77952de236"
                    return headers
                }
            }
        queue.add(jsonObjectRequest)
    }


    class GetItemsFromDBAsync(context: Context, val resId: String) : AsyncTask<Void, Void, List<OrderEntity>>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").fallbackToDestructiveMigration().build()
        override fun doInBackground(vararg params: Void?): List<OrderEntity> {
            return db.orderDao().getAllOrders()
        }
    }

    /* AsyncTask class for clearing the recently added items from the database*/

    class ClearDBAsync(context: Context, val resId: String) : AsyncTask<Void, Void, Boolean>() {
        private val db = Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").fallbackToDestructiveMigration().build()
        override fun doInBackground(vararg params: Void?): Boolean {
            db.orderDao().deleteOrders(resId)
            db.close()
            return true
        }
    }

}
