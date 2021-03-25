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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        this.setTitle("Edit your profile...")

        val editText = findViewById<EditText>(R.id.edit)

        editText.setOnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                editText.setSelection(0, 0)
            }
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
                val text = findViewById<TextView>(R.id.tv)
                setToast("Saving...", text.context)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


}