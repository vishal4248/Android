package com.example.foodies.adapter

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.example.foodies.R
import com.example.foodies.database.RestaurantDatabase
import com.example.foodies.database.RestaurantEntity
import com.example.foodies.fragment.RestaurantMenuFragment
import com.example.foodies.model.Restaurant
import com.squareup.picasso.Picasso

class DashboardRecyclerAdapter(private val context: Context, private val restaurantList: ArrayList<Restaurant>): RecyclerView.Adapter<DashboardRecyclerAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardRecyclerAdapter.DashboardViewHolder {

        val view= LayoutInflater.from(parent.context).inflate(R.layout.recycler_dashboard_single_row,parent,false)
        return DashboardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return restaurantList.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: DashboardRecyclerAdapter.DashboardViewHolder, position: Int) {

        val resData = restaurantList[position]
        holder.restaurantName.text = resData.name
        holder.rating.text = resData.rating
        val costForPerson = "${resData.costForOne.toString()}/person"
        holder.cost.text = costForPerson
        Picasso.get().load(resData.imageUrl).error(R.drawable.ic_default_res_cover).into(holder.resThumbnail);

        val listOfFavourites = GetAllFavAsyncTask(context).execute().get()
        if (listOfFavourites.isNotEmpty() && listOfFavourites.contains(resData.id.toString())) {
            holder.favImage.setImageResource(R.drawable.ic_fav_filled)
        } else {
            holder.favImage.setImageResource(R.drawable.ic_favourites)
        }
        holder.favImage.setOnClickListener {

            val restaurantEntity = RestaurantEntity(
                resData.id,
                resData.name,
                resData.rating,
                resData.costForOne.toString(),
                resData.imageUrl
            )

            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val async = DBAsyncTask(context, restaurantEntity, 2).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(context, "Restaurant added to favourites", Toast.LENGTH_SHORT).show()
                    holder.favImage.setImageResource(R.drawable.ic_fav_filled)
                } else {
                    Toast.makeText(context, "Some error occurred!!!", Toast.LENGTH_SHORT).show()
                }
            } else {
                val async = DBAsyncTask(context, restaurantEntity, 3).execute()
                val result = async.get()
                if (result) {
                    Toast.makeText(context, "Restaurant removed from favourites", Toast.LENGTH_SHORT).show()
                    holder.favImage.setImageResource(R.drawable.ic_favourites)
                } else {
                    Toast.makeText(context, "Some error occurred!!!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        holder.cardRestaurant.setOnClickListener {
            Toast.makeText(context, "Clicked on: ${holder.restaurantName.text}", Toast.LENGTH_SHORT).show()
            val fragment= RestaurantMenuFragment()
            val args= Bundle()
            args.putInt("id",resData.id)
            args.putString("name",resData.name)
            fragment.arguments= args
            val transaction= (context as FragmentActivity).supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame, fragment)
            transaction.commit()
            (context as AppCompatActivity).supportActionBar?.title= holder.restaurantName.text.toString()
        }
    }

        class DashboardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val resThumbnail: ImageView= view.findViewById(R.id.imgRestaurantThumbnail)
            val restaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
            val rating: TextView = view.findViewById(R.id.txtRestaurantRating)
            val cost: TextView = view.findViewById(R.id.txtCostForOne)
            val favImage: ImageView = view.findViewById(R.id.imgIsFav)
            val cardRestaurant: CardView = view.findViewById(R.id.cardRestaurant)
        }

    class DBAsyncTask(private val context: Context, private val restaurantEntity: RestaurantEntity, private val mode: Int) : AsyncTask<Void, Void, Boolean>() {

        private val db= Room.databaseBuilder(context, RestaurantDatabase::class.java, "res-db").fallbackToDestructiveMigration().build()

        /*mode-1: check DB if book is favourite or not
        mode-2: save the book into DB as favourite
        mode-3: remove the favourite book*/

        override fun doInBackground(vararg p0: Void?): Boolean {
            when(mode) {
                1-> {
                    //mode-1: check DB if book is favourite or not
                    val res: RestaurantEntity? = db.restaurantDao().getRestaurantById(restaurantEntity.id.toString())
                    db.close()
                    return res!= null
                }
                2-> {
                    //mode-2: save the book into DB as favourite
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3-> {
                    //mode-3: remove the favourite book
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
            }
            return false
        }
    }

    class GetAllFavAsyncTask(context: Context): AsyncTask<Void, Void, List<String>>() {

        val db= Room.databaseBuilder(context,RestaurantDatabase::class.java,"res-db").fallbackToDestructiveMigration().build()
        override fun doInBackground(vararg p0: Void?): List<String> {
            val list= db.restaurantDao().getAllRestaurant()
            val listOfIds= arrayListOf<String>()
            for(i in list) {
                listOfIds.add(i.id.toString())
            }
            return listOfIds
        }
    }

}