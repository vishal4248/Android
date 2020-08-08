package com.example.foodies.activity

import android.app.Activity
import android.app.AlertDialog
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
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.foodies.R
import com.example.foodies.util.ConnectionManager
import com.example.foodies.util.Validations
import org.json.JSONObject

class ResetPasswordActivity: AppCompatActivity() {

    lateinit var etMobileNo: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmNewPassword: EditText
    lateinit var etOtp: EditText
    lateinit var btnLogIn: Button
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        sharedPreferences = getSharedPreferences(getString(R.string.preferences_file_name), Context.MODE_PRIVATE)
        var isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        title = "Reset Password"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        etMobileNo = findViewById(R.id.etResetMobileNo)
        etNewPassword = findViewById(R.id.etResetNewPassword)
        etConfirmNewPassword = findViewById(R.id.etResetConfirmNewPassword)
        etOtp = findViewById(R.id.etResetOtp)
        btnLogIn = findViewById(R.id.btnResetLogIn)

        btnLogIn.setOnClickListener {
            val mobileNumber = etMobileNo.text.toString()
            val newPassword = etNewPassword.text.toString()
            val confirmPassword = etConfirmNewPassword.text.toString()
            val oneTimePassword = etOtp.text.toString()

            if (ConnectionManager().isNetworkAvailable(this@ResetPasswordActivity as Context)) {
                if (oneTimePassword.length == 4) {
                    if (Validations.validatePasswordLength(newPassword)) {
                        if (Validations.matchPassword(newPassword, confirmPassword)) {

                            val resetPasswordUrl = "  http://13.235.250.119/v2/reset_password/fetch_result"
                            val queue = Volley.newRequestQueue(this@ResetPasswordActivity)

                            val jsonParams = JSONObject()
                            jsonParams.put("mobile_number", mobileNumber)
                            jsonParams.put("password", newPassword)
                            jsonParams.put("otp", oneTimePassword)

                            val jsonObjectRequest =
                                object : JsonObjectRequest(
                                    Method.POST,
                                    resetPasswordUrl,
                                    jsonParams,
                                    Response.Listener {

                                        try {
                                            val data = it.getJSONObject("data")
                                            val success = data.getBoolean("success")
                                            if (success) {
                                                val builder = AlertDialog.Builder(this@ResetPasswordActivity)
                                                builder.setTitle("Confirmation")
                                                builder.setMessage("Your password has been successfully changed")
                                                builder.setIcon(R.drawable.ic_default_res_cover)
                                                builder.setCancelable(false)
                                                builder.setPositiveButton("Ok") { _, _ ->
                                                    isLoggedIn = true
                                                    sharedPreferences.edit().putBoolean("isLoggedIn",true).apply()
                                                    startActivity(Intent(this@ResetPasswordActivity, MainActivity::class.java))
                                                }
                                                builder.create().show()
                                            } else {
                                                Toast.makeText(this@ResetPasswordActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                                            }
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(this@ResetPasswordActivity, "Some error occurred", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    Response.ErrorListener {
                                        VolleyLog.e(
                                            "Error::::",
                                            "/post request fail! Error: ${it.message}"
                                        )
                                        Toast.makeText(this@ResetPasswordActivity, it.message, Toast.LENGTH_SHORT).show()
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
                            Toast.makeText(this@ResetPasswordActivity, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, "Invalid Password", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Incorrect OTP", Toast.LENGTH_SHORT).show()
                }
            } else {
                val builder = android.app.AlertDialog.Builder(applicationContext)
                builder.setTitle("Error")
                builder.setMessage("No Internet Connection found. Please connect to the internet and re-open the app.")
                builder.setCancelable(false)
                builder.setPositiveButton("Ok") { _, _ ->
                    ActivityCompat.finishAffinity(this@ResetPasswordActivity as Activity)
                }
                builder.create()
                builder.show()            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}
