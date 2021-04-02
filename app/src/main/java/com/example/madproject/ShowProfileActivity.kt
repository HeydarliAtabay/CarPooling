package com.example.madproject

import android.content.Intent
import android.net.Uri
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
    private var currentPhotoPath: String? = ""
    private lateinit var photoURI: Uri


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
        image.setImageResource(R.drawable.atabay)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.edit_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                editProfile()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun editProfile() {
        val intent = Intent(this, EditProfileActivity::class.java).also {
            it.putExtra(ValueIds.FULL_NAME.value, fullName.text.toString())
            it.putExtra(ValueIds.NICKNAME.value, nickName.text.toString())
            it.putExtra(ValueIds.DATE_OF_BIRTH.value, dateOfBirth.text.toString())
            it.putExtra(ValueIds.EMAIL.value, email.text.toString())
            it.putExtra(ValueIds.PHONE_NUMBER.value, phoneNumber.text.toString())
            it.putExtra(ValueIds.LOCATION.value, location.text.toString())
            it.putExtra(ValueIds.CURRENT_PHOTO_PATH.value, currentPhotoPath)
        }
        startActivityForResult(intent, Requests.INTENT_EDIT_ACTIVITY.value)
    }
}