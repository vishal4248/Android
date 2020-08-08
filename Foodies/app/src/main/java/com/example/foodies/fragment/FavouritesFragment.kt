package com.example.foodies.fragment

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodies.R
import com.example.foodies.adapter.DashboardRecyclerAdapter
import com.example.foodies.database.RestaurantDatabase
import com.example.foodies.database.RestaurantEntity
import com.example.foodies.model.Restaurant

class FavouritesFragment : Fragment() {

    private lateinit var recyclerFavourites: RecyclerView
    private lateinit var favouritesRestaurantAdapter: DashboardRecyclerAdapter
    private lateinit var rlFavourites: RelativeLayout
    private lateinit var rlNoFavourites: RelativeLayout
    lateinit var progressLayout: RelativeLayout
    private var restaurantList = arrayListOf<Restaurant>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_favourites, container, false)
        rlFavourites = view.findViewById(R.id.rlFavorites)
        rlNoFavourites = view.findViewById(R.id.rlNoFavorites)
        recyclerFavourites = view.findViewById(R.id.recyclerViewFavourites)
        progressLayout = view.findViewById(R.id.progressLayout)
        progressLayout.visibility = View.VISIBLE

        val backgroundList= RetrieveFavourites(activity as Context).execute().get()
        if (backgroundList.isEmpty()) {
            progressLayout.visibility = View.GONE
            rlFavourites.visibility = View.GONE
            rlNoFavourites.visibility = View.VISIBLE
        } else {
            progressLayout.visibility = View.GONE
            rlFavourites.visibility = View.VISIBLE
            rlNoFavourites.visibility = View.GONE

            for (i in backgroundList) {
                restaurantList.add(
                    Restaurant(
                        i.id,
                        i.name,
                        i.rating,
                        i.costForOne.toInt(),
                        i.imageUrl
                    )
                )
            }

            favouritesRestaurantAdapter= DashboardRecyclerAdapter(activity as Context, restaurantList)
            val myLayoutManager= LinearLayoutManager(activity)
            recyclerFavourites.layoutManager = myLayoutManager
            recyclerFavourites.itemAnimator= DefaultItemAnimator()
            recyclerFavourites.adapter= favouritesRestaurantAdapter
            recyclerFavourites.setHasFixedSize(true)
        }
        return view
    }

    class RetrieveFavourites(private val context: Context): AsyncTask<Void, Void, List<RestaurantEntity>>() {

        val db= Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").fallbackToDestructiveMigration().build()
        override fun doInBackground(vararg p0: Void?): List<RestaurantEntity> {
            return db.restaurantDao().getAllRestaurant()
        }
    }
}