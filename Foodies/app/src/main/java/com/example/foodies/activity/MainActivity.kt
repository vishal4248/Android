package com.example.foodies.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.adapter.RestaurantMenuAdapter
import com.example.foodies.fragment.*
import com.example.foodies.fragment.RestaurantMenuFragment.Companion.resId
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItem: MenuItem?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences= getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
        var isLoggedIn= sharedPreferences.getBoolean("isLoggedIn",true)

        drawerLayout= findViewById(R.id.navigationDrawerLayout)
        coordinatorLayout= findViewById(R.id.coordinatorLayout)
        toolbar=  findViewById(R.id.toolbar)
        frameLayout= findViewById(R.id.frame)
        navigationView= findViewById(R.id.navigationView)

        val convertView = LayoutInflater.from(this@MainActivity).inflate(R.layout.drawer_header,null)
        val txtName = convertView.findViewById(R.id.txtDrawerText) as TextView
        val txtPhoneNumber = convertView.findViewById(R.id.txtDrawerSecondText) as TextView

        txtName.text = sharedPreferences.getString("user_name", null)
        txtPhoneNumber.text = "${sharedPreferences.getString("user_mobile_number", null)}"
        navigationView.addHeaderView(convertView)

        setUpToolbar()
        openDashboard()

        val actionBarDrawerToggle= ActionBarDrawerToggle(
            this@MainActivity
            ,drawerLayout
            , R.string.open_drawer
            , R.string.close_drawer
        )

        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener {
            if( previousMenuItem != null) {
                previousMenuItem?.isChecked= false
            }
            it.isChecked=true
            it.isCheckable=true
            previousMenuItem= it
            val mPendingRun = Runnable {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
            Handler().postDelayed(mPendingRun, 100)

            when(it.itemId) {
                R.id.dashboard -> {
                    openDashboard()
                    drawerLayout.closeDrawers()
                }
                R.id.profile -> {
                    supportActionBar?.title="My Profile"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            ProfileFragment()
                        )
                        .commit()
                    drawerLayout.closeDrawers()
                }
                R.id.favourites -> {
                    supportActionBar?.title="Favourite Restaurants"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FavouritesFragment()
                        )
                        .commit()
                    drawerLayout.closeDrawers()
                }
                R.id.orderHistory -> {
                    supportActionBar?.title="My Previous Orders"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            OrderHistoryFragment()
                        )
                        .commit()
                    drawerLayout.closeDrawers()
                }

                R.id.faqs -> {
                    supportActionBar?.title="Frequently Asked Questions"
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame,
                            FaqsFragment()
                        )
                        .commit()
                    drawerLayout.closeDrawers()
                }
                R.id.logout -> {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Confirmation")
                        .setMessage("Are you sure you want Logout?")
                        .setPositiveButton("Yes") { _, _ ->
                            isLoggedIn= false
                            startActivity(Intent(this@MainActivity, LogInActivity::class.java))
                            sharedPreferences.edit().putBoolean("isLoggedIn",false).apply()
                            sharedPreferences.edit().clear().apply()
                            ActivityCompat.finishAffinity(this)
                        }
                        .setNegativeButton("No") { _, _ ->
                            openDashboard()
                        }
                        .create()
                        .show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title="Title Toolbar"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id= item.itemId
        if(id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openDashboard() {
        supportActionBar?.title="All Restaurants"
        val fragment= DashboardFragment()
        val transaction= supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frame, fragment)
        transaction.commit()
        navigationView.setCheckedItem(R.id.dashboard)
    }

        override fun onBackPressed() {
            val f = supportFragmentManager.findFragmentById(R.id.frame)
            when (f) {
                is DashboardFragment -> {
                    Volley.newRequestQueue(this).cancelAll(this::class.java.simpleName)
                    super.onBackPressed()
                }
                is RestaurantMenuFragment -> {
                    if (!RestaurantMenuAdapter.isCartEmpty) {
                        val builder = AlertDialog.Builder(this@MainActivity)
                        builder.setTitle("Confirmation")
                            .setMessage("Going back will reset cart items. Do you still want to proceed?")
                            .setPositiveButton("Yes") { _, _ ->
                                val clearCart =
                                    CartActivity.ClearDBAsync(applicationContext, resId.toString())
                                        .execute().get()
                                openDashboard()
                                RestaurantMenuAdapter.isCartEmpty = true
                            }
                            .setNegativeButton("No") { _, _ ->

                            }
                            .create()
                            .show()
                    } else {
                        openDashboard()
                    }
                }
                else -> openDashboard()
            }
        }

}
