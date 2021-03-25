package com.example.madproject

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        this.setTitle("Edit your profile...")

        fullName = findViewById(R.id.fullName)
        nickName = findViewById(R.id.nickName)
        email = findViewById(R.id.email)
        location = findViewById(R.id.location)


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