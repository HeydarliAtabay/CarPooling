package com.example.madproject

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.ContextMenu.ContextMenuInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity


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
                fullName.setSelection(0, 0)
            } }

        nickName.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                nickName.setSelection(0, 0)
            } }

        email.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                email.setSelection(0, 0)
            } }

        location.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {  // lost focus
                location.setSelection(0, 0)
            } }

        val editPhoto = findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(editPhoto)

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
                setToast("Saving...", applicationContext)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.imageButton -> {
                menuInflater.inflate(R.menu.menu_change_photo, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_gallery -> {
                setToast("Opening the gallery...", applicationContext)
                true
            }
            R.id.action_camera -> {
                setToast("Opening the camera...", applicationContext)
                true
            }
            else -> false
        }
    }


}