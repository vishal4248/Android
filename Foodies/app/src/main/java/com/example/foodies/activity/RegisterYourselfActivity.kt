package com.example.foodies.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.util.ConnectionManager
import org.json.JSONException
import org.json.JSONObject

class RegisterYourselfActivity: AppCompatActivity() {

    private lateinit var etFullName: EditText
    lateinit var etMobileNo:EditText
    lateinit var etEmail: EditText
    lateinit var etAddress: EditText
    lateinit var etPassword: EditText
    lateinit var logIn: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_yourself)

        sharedPreferences= getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
        var isLoggedIn= sharedPreferences.getBoolean("isLoggedIn",false)

        title="Registration"

        etFullName= findViewById(R.id.etRegisterFullName)
        etMobileNo= findViewById(R.id.etRegisterMobileNo)
        etEmail= findViewById(R.id.etRegisterEmailAddress)
        etAddress= findViewById(R.id.etRegisterAddress)
        etPassword= findViewById(R.id.etRegisterPassword)
        logIn= findViewById(R.id.btnRegisterLogin)

        logIn.setOnClickListener {

            val name= etFullName.text.toString()
            val mobileNumber= etMobileNo.text.toString()
            val email= etEmail.text.toString()
            val address= etAddress.text.toString()
            val password= etPassword.text.toString()

            val jsonRegister = JSONObject()
            jsonRegister.put("name",name)
            jsonRegister.put("mobile_number", mobileNumber)
            jsonRegister.put("password", password)
            jsonRegister.put("address",address)
            jsonRegister.put("email",email)

            val registerUrl="  http://13.235.250.119/v2/register/fetch_result"
            val queue= Volley.newRequestQueue(this@RegisterYourselfActivity)

            if (ConnectionManager().isNetworkAvailable(this@RegisterYourselfActivity as Context)) {

                val jsonObjectRequest= object : JsonObjectRequest(
                    Method.POST,
                    registerUrl,
                    jsonRegister,
                    Response.Listener {

                        try {
                            Toast.makeText(this@RegisterYourselfActivity as Context,"Registration on the way",Toast.LENGTH_LONG).show()
                            val data = it.getJSONObject("data")
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
                                val intent= Intent(this@RegisterYourselfActivity,MainActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this@RegisterYourselfActivity as Context,"Sorry $it",Toast.LENGTH_LONG).show()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    },
                    Response.ErrorListener {
                        Toast.makeText(
                            this@RegisterYourselfActivity as Context,
                            "it's Volley error",
                            Toast.LENGTH_SHORT
                        ).show()
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
                    ActivityCompat.finishAffinity(this@RegisterYourselfActivity as Activity)
                }
                builder.create()
                builder.show()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}