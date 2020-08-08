package com.example.foodies.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import com.example.foodies.R
import com.example.foodies.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.HashMap

class LogInActivity: AppCompatActivity() {

    private lateinit var userName: EditText
    lateinit var password: EditText
    lateinit var logIn: Button
    lateinit var signUp: TextView
    lateinit var forgotPassword: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        sharedPreferences= getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
        var isLoggedIn= sharedPreferences.getBoolean("isLoggedIn",false)
        setContentView(R.layout.activity_log_in)
        title= "Log In"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userName= findViewById(R.id.etUserName)
        password= findViewById(R.id.etPassword)
        logIn= findViewById(R.id.btnLogin)
        forgotPassword= findViewById(R.id.txtForgotPassword)
        signUp= findViewById(R.id.txtSignUp)

        if(isLoggedIn) {
            val intent= Intent(this@LogInActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        logIn.setOnClickListener {
            val mobileNumber=userName.text.toString()
            val password=password.text.toString()

            val queue= Volley.newRequestQueue(this@LogInActivity)

            val jsonParams = JSONObject()
            jsonParams.put("mobile_number", mobileNumber)
            jsonParams.put("password", password)

            val logInUrl=" http://13.235.250.119/v2/login/fetch_result"

            if (ConnectionManager().isNetworkAvailable(this@LogInActivity as Context)){
                val jsonObjectRequest= object : JsonObjectRequest(
                    Method.POST,
                    logInUrl,
                    jsonParams,
                Response.Listener<JSONObject> {

                    try {
                        val data = it.getJSONObject("data")
                        Toast.makeText(this,"Welcome to Foodies!!!",Toast.LENGTH_LONG).show()
                        val success = data.getBoolean("success")
                        if(success) {
                            val response= data.getJSONObject("data")
                            sharedPreferences.edit().putString("user_id",response.getString("user_id")).apply()
                            sharedPreferences.edit().putString("user_name",response.getString("name")).apply()
                            sharedPreferences.edit().putString("user_email",response.getString("email")).apply()
                            sharedPreferences.edit().putString("user_mobile_number",response.getString("mobile_number")).apply()
                            sharedPreferences.edit().putString("user_address",response.getString("address")).apply()

                            isLoggedIn=true
                            sharedPreferences.edit().putBoolean("isLoggedIn",true).apply()
                            val intent= Intent(this@LogInActivity,MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@LogInActivity as Context,"Sorry LogIn details not found",Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                },
                Response.ErrorListener {
                    Toast.makeText(this@LogInActivity as Context, "Some error occurred", Toast.LENGTH_SHORT).show()
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
                val builder = android.app.AlertDialog.Builder(applicationContext)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@LogInActivity as Activity)
                }
                builder.create()
                builder.show()
            }
        }

        forgotPassword.setOnClickListener {
            Toast.makeText(this@LogInActivity as Context,"forgot password",Toast.LENGTH_LONG).show()
            val intent= Intent(this@LogInActivity,ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        signUp.setOnClickListener{
            Toast.makeText(this@LogInActivity as Context,"sign up",Toast.LENGTH_LONG).show()
            val intent= Intent(this@LogInActivity,RegisterYourselfActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onPause() {
        super.onPause()
        finish()
    }

}