package com.example.foodies.fragment

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import com.example.foodies.R


class ProfileFragment : Fragment() {

    private lateinit var txtUserName: TextView
    private lateinit var txtPhone: TextView
    private lateinit var txtAddress: TextView
    private lateinit var txtEmail: TextView
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_profile, container, false)

        sharedPreferences= (activity as FragmentActivity).getSharedPreferences("login_details",Context.MODE_PRIVATE)
        txtUserName= view.findViewById(R.id.txtProfileName)
        txtPhone= view.findViewById(R.id.txtProfileMobile)
        txtAddress= view.findViewById(R.id.txtProfileDelivery)
        txtEmail= view.findViewById(R.id.txtProfileEmail)

        txtUserName.text = sharedPreferences.getString("user_name", null)

        val phoneText = "+91-${sharedPreferences.getString("user_mobile_number", null)}"
        txtPhone.text = phoneText

        txtEmail.text = sharedPreferences.getString("user_email", null)

        val address = sharedPreferences.getString("user_address", null)
        txtAddress.text = address

        return view
    }
}