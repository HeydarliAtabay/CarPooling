package com.example.madproject

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.method.KeyListener
import android.view.*
import android.widget.*

var t: Toast? = null

fun setToast(message: String?, context: Context) {
    t?.cancel()
    t = Toast.makeText(context, message, Toast.LENGTH_LONG)
    t?.show()
}

class EditProfileActivity : AppCompatActivity() {
    private lateinit var fullName:EditText
    private lateinit var nickName:EditText
    private lateinit var email:EditText
    private lateinit var location:EditText
    private lateinit var imageView:ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        this.setTitle("Edit your profile...")

        fullName = findViewById(R.id.fullName)
        nickName = findViewById(R.id.nickName)
        email = findViewById(R.id.email)
        location = findViewById(R.id.location)
        imageView = findViewById(R.id.imageView)


        fullName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                fullName.setSelection(0,0)
            } }

        nickName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                nickName.setSelection(0,0)
            } }

        email.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                email.setSelection(0,0)
            } }

        location.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                location.setSelection(0,0)
            } }

        val button = findViewById<ImageButton>(R.id.imageButton)
        button.setOnClickListener{
            dispatchTakePictureIntent()
        }



    }


    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, 1)
        } catch (e: ActivityNotFoundException) {
            // display error state to the user
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.save_profile, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.saveButton -> {
                val text = findViewById<TextView>(R.id.fullName)
                setToast("Saving...", text.context)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}