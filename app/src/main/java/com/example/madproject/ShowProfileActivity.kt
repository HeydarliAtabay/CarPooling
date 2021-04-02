package com.example.madproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

import android.widget.ImageView

import android.widget.TextView
import android.widget.Toast


class ShowProfileActivity : AppCompatActivity() {
    private lateinit var fullName : TextView
    private lateinit var nickName : TextView
    private lateinit var dateOfBirth : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var location : TextView
    private lateinit var image : ImageView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTitle("")
        setContentView(R.layout.activity_show_profile)

        fullName = findViewById<TextView>(R.id.fullName)
        nickName = findViewById<TextView>(R.id.nickName)
        dateOfBirth = findViewById<TextView>(R.id.dateOfBirth)
        email = findViewById<TextView>(R.id.email)
        phoneNumber = findViewById<TextView>(R.id.phoneNumber)
        location = findViewById<TextView>(R.id.location)
        image = findViewById<ImageView>(R.id.imageView3)


        fullName.text = "Heydarli Atabay"
        nickName.text = "Atash"
        dateOfBirth.text = "23/09/95"
        email.text = "heydarli.atabay@gmail.com"
        phoneNumber.text = "345678909"
        location.text = "Turin, Italy"

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                val intent = Intent(this, EditProfileActivity::class.java)
                startActivityForResult(intent, 1)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }






}