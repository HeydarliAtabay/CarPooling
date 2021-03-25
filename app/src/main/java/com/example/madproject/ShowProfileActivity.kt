package com.example.madproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class ShowProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.setTitle("")

        val actButton = findViewById<Button>(R.id.button)
        actButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }
}