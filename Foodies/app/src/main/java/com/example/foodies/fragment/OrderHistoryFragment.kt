package com.example.foodies.fragment

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.adapter.OrderHistoryAdapter
import com.example.foodies.model.OrderDetails
import com.example.foodies.util.ConnectionManager
import org.json.JSONException


class OrderHistoryFragment : Fragment() {

    lateinit var recyclerOrderHistory : RecyclerView
    lateinit var orderHistoryAdapter : OrderHistoryAdapter
    lateinit var sharedPreferences : SharedPreferences
    lateinit var progressLayout : RelativeLayout
    var orderHistoryList = arrayListOf<OrderDetails>()
    lateinit var layoutManager : RecyclerView.LayoutManager
    lateinit var llHasOrders: LinearLayout
    lateinit var rlNoOrders: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_order_history, container, false)
        sharedPreferences= activity!!.getSharedPreferences(getString(R.string.preferences_file_name),Context.MODE_PRIVATE)
        recyclerOrderHistory=view.findViewById(R.id.recyclerOrderHistory)
        llHasOrders= view.findViewById(R.id.llHasOrders)
        rlNoOrders= view.findViewById(R.id.rlNoOrders)
        layoutManager = LinearLayoutManager(activity as Context)

        progressLayout =view.findViewById(R.id.rlLoading)
        progressLayout.visibility = View.VISIBLE

        if(ConnectionManager().isNetworkAvailable(activity as Context)){

            val userId =sharedPreferences.getString("user_id",null)
            val usId= userId.toString()
            val url = "http://13.235.250.119/v2/orders/fetch_result/"
            val orderHistoryUrl= url+ usId

            val queue = Volley.newRequestQueue(activity as Context)

            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                orderHistoryUrl,
                null,
                Response.Listener {

                    progressLayout.visibility = View.GONE
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            if (resArray.length() == 0) {
                                llHasOrders.visibility = View.GONE
                                rlNoOrders.visibility = View.VISIBLE
                            }
                            else {
                                for (i in 0 until resArray.length()) {
                                    val orderJsonObject = resArray.getJSONObject(i)
                                    val foodItems = orderJsonObject.getJSONArray("food_items")
                                    val orderDetails = OrderDetails(
                                        orderJsonObject.getString("order_id").toInt(),
                                        orderJsonObject.getString("restaurant_name"),
                                        orderJsonObject.getString("total_cost"),
                                        orderJsonObject.getString("order_placed_at"),
                                        foodItems
                                    )
                                    orderHistoryList.add(orderDetails)
                                    if (orderHistoryList.isEmpty()) {
                                        llHasOrders.visibility = View.GONE
                                        rlNoOrders.visibility = View.VISIBLE
                                    } else {
                                        llHasOrders.visibility = View.VISIBLE
                                        rlNoOrders.visibility = View.GONE

                                        if (activity != null) {
                                            orderHistoryAdapter = OrderHistoryAdapter(activity as Context, orderHistoryList)
                                            val mLayoutManager = LinearLayoutManager(activity as Context)
                                            recyclerOrderHistory.layoutManager = mLayoutManager
                                            recyclerOrderHistory.itemAnimator = DefaultItemAnimator()
                                            recyclerOrderHistory.adapter = orderHistoryAdapter

                                        } else {
                                            queue.cancelAll(this::class.java.simpleName)
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: JSONException){
                            e.printStackTrace()
                    }
                }, Response.ErrorListener {
                    if(activity !=null){
                        Toast.makeText(activity as Context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "cc2a77952de236"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        }
        else{
            val builder = android.app.AlertDialog.Builder(activity as Context)
            builder.setTitle("Error")
            builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
            builder.setCancelable(false)
            builder.setPositiveButton("Ok") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            builder.create()
            builder.show()
        }
        return view
    }
}