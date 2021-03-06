package com.example.foodies.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.util.ConnectionManager
import com.example.foodies.util.Validations
import org.json.JSONObject

class ForgotPasswordActivity: AppCompatActivity() {

    lateinit var etMobileNo: EditText
    lateinit var etEmail: EditText
    lateinit var btnNext: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        title = "Forgot Password"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etMobileNo = findViewById(R.id.etFgtMobileNo)
        etEmail = findViewById(R.id.etFgtEmailAdd)
        btnNext = findViewById(R.id.btnFgtNext)
        btnNext.setOnClickListener {

            val mobileNumber = etMobileNo.text.toString()
            val email = etEmail.text.toString()
            if (Validations.validateMobile(mobileNumber)) {
                etMobileNo.error = null
                if (Validations.validateEmail(email)) {
                    if (ConnectionManager().isNetworkAvailable(this@ForgotPasswordActivity)) {
                        sendOTP(mobileNumber, email)
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "No Internet Connection!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    etEmail.error = "Invalid Email"
                }
            } else {
                etMobileNo.error = "Invalid Mobile Number"
            }
        }
    }

    private fun sendOTP(mobileNumber: String, email: String) {

        val forgotPasswordUrl="  http://13.235.250.119/v2/forgot_password/fetch_result"
        val queue = Volley.newRequestQueue(this@ForgotPasswordActivity)
        val jsonParams = JSONObject()

        jsonParams.put("mobile_number", mobileNumber)
        jsonParams.put("email", email)

        val jsonObjectRequest =
            object : JsonObjectRequest(
                Method.POST,
                forgotPasswordUrl,
                jsonParams,
                Response.Listener {

                try {
                    val data = it.getJSONObject("data")
                    val success = data.getBoolean("success")
                    if (success) {
                        val firstTry = data.getBoolean("first_try")
                        if (firstTry) {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please check your registered Email for the OTP.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent= Intent(this@ForgotPasswordActivity,ResetPasswordActivity::class.java)
                                startActivity(intent)
                            }
                            builder.create().show()
                        } else {
                            val builder = AlertDialog.Builder(this@ForgotPasswordActivity)
                            builder.setTitle("Information")
                            builder.setMessage("Please refer to the previous email for the OTP.")
                            builder.setCancelable(false)
                            builder.setPositiveButton("Ok") { _, _ ->
                                val intent = Intent(
                                    this@ForgotPasswordActivity,
                                    ResetPasswordActivity::class.java
                                )
                                intent.putExtra("user_mobile", mobileNumber)
                                startActivity(intent)
                            }
                            builder.create().show()
                        }
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, "Mobile number not registered!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@ForgotPasswordActivity, "Incorrect response error!!", Toast.LENGTH_SHORT).show()
                }
            }, Response.ErrorListener {
                VolleyLog.e("Error::::", "/post request fail! Error: ${it.message}")
                Toast.makeText(this@ForgotPasswordActivity, it.message, Toast.LENGTH_SHORT).show()
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

}