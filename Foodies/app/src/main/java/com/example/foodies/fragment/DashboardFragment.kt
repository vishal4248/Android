package com.example.foodies.fragment

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.adapter.DashboardRecyclerAdapter
import com.example.foodies.model.Restaurant
import com.example.foodies.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class DashboardFragment : Fragment() {

    private lateinit var recyclerDashboard: RecyclerView
    private lateinit var allRestaurantsAdapter: DashboardRecyclerAdapter
    private var restaurantList = arrayListOf<Restaurant>()
    private lateinit var progressLayout: RelativeLayout

    private var ratingComparator= Comparator<Restaurant>{
            res1, res2 ->
        if(res1.rating.compareTo(res2.rating,true) == 0) {
            res1.name.compareTo(res2.name,true)
        } else {
            res1.rating.compareTo(res2.rating,true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        progressLayout = view.findViewById(R.id.progressLayout) as RelativeLayout
        progressLayout.visibility = View.VISIBLE

        setUpRecycler(view)

        return view
    }

    private fun setUpRecycler(view: View) {
        recyclerDashboard = view.findViewById(R.id.recyclerviewDashboard) as RecyclerView

        val queue = Volley.newRequestQueue(activity as Context)
        val url= "http://13.235.250.119/v2/restaurants/fetch_result/"

        if (ConnectionManager().isNetworkAvailable(activity as Context)) {
            val jsonObjectRequest = object : JsonObjectRequest(
                Method.GET,
                url,
                null,
                Response.Listener<JSONObject> {

                    progressLayout.visibility = View.GONE
                    try {
                        val data = it.getJSONObject("data")
                        val success = data.getBoolean("success")
                        if (success) {
                            val resArray = data.getJSONArray("data")
                            for (i in 0 until resArray.length()) {
                                val resObject = resArray.getJSONObject(i)
                                val restaurant = Restaurant(
                                    resObject.getString("id").toInt(),
                                    resObject.getString("name"),
                                    resObject.getString("rating"),
                                    resObject.getString("cost_for_one").toInt(),
                                    resObject.getString("image_url")
                                )
                                restaurantList.add(restaurant)
                                if (activity != null) {
                                    allRestaurantsAdapter= DashboardRecyclerAdapter(activity as Context,restaurantList)
                                    val myLayoutManager = LinearLayoutManager(activity)
                                    recyclerDashboard.layoutManager = myLayoutManager
                                    recyclerDashboard.itemAnimator = DefaultItemAnimator()
                                    recyclerDashboard.adapter = allRestaurantsAdapter
                                    recyclerDashboard.setHasFixedSize(true)
                                }
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener { error: VolleyError? ->
                    Toast.makeText(activity as Context, error?.message, Toast.LENGTH_SHORT).show()
                }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] = "cc2a77952de236"
                    return headers
                }
            }
            queue.add(jsonObjectRequest)
        } else {
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
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sort, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id= item.itemId
        if(id == R.id.action_sort) {
            Collections.sort(restaurantList,ratingComparator)
            restaurantList.reverse()
        }
        allRestaurantsAdapter.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }
}