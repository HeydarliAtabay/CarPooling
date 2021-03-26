package com.example.madproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.widget.Button

import android.widget.TextView


class ShowProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTitle("")
        setContentView(R.layout.activity_show_profile)
        val fullname="Heydarli Atabay"
        val nickname="Atash"
        val emailaddress="heydarli.atabay@gmail.com"
        val location="Italy,Torino"
        val fullnameView= findViewById<TextView>(R.id.fullNameView)
        val nicknameView=findViewById<TextView>(R.id.nicknameView)
        val emailaddressView=findViewById<TextView>(R.id.emailaddressView)
        val locationView= findViewById<TextView>(R.id.LocationView)
        fullnameView.text=fullname
        nicknameView.text=nickname
        emailaddressView.text=emailaddress
        locationView.text=location

    }
}