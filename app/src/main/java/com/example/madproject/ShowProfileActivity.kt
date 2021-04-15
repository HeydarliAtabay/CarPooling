package com.example.madproject

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import com.example.madproject.lib.*
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.content.edit
import org.json.JSONObject
import java.io.File


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
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.title = "Your profile"
        setContentView(R.layout.activity_show_profile)

        fullName = findViewById(R.id.fullName)
        nickName = findViewById(R.id.nickName)
        dateOfBirth = findViewById(R.id.dateOfBirth)
        email = findViewById(R.id.email)
        phoneNumber = findViewById(R.id.phoneNumber)
        location = findViewById(R.id.location)
        image = findViewById(R.id.imageView3)
        sharedPref = this.getPreferences(Context.MODE_PRIVATE)

        // If the sharedPref does not exist (first run) it is created with the default values
        if (!sharedPref.contains(ValueIds.JSON_OBJECT.value)) saveValues()
        loadValues()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                Requests.INTENT_EDIT_ACTIVITY.value -> {
                    fullName.text = data?.getStringExtra(ValueIds.FULL_NAME.value)
                    nickName.text = data?.getStringExtra(ValueIds.NICKNAME.value)
                    email.text = data?.getStringExtra(ValueIds.EMAIL.value)
                    phoneNumber.text = data?.getStringExtra(ValueIds.PHONE_NUMBER.value)
                    dateOfBirth.text = data?.getStringExtra(ValueIds.DATE_OF_BIRTH.value)
                    location.text = data?.getStringExtra(ValueIds.LOCATION.value)
                    currentPhotoPath = data?.getStringExtra(ValueIds.CURRENT_PHOTO_PATH.value)
                    setPic()
                    saveValues()
                }

            }
        }
    }

    private fun saveValues() {
        val dataObj = JSONObject()

        dataObj.put(ValueIds.FULL_NAME.value, fullName.text.toString())
        dataObj.put(ValueIds.NICKNAME.value, nickName.text.toString())
        dataObj.put(ValueIds.DATE_OF_BIRTH.value, dateOfBirth.text.toString())
        dataObj.put(ValueIds.EMAIL.value, email.text.toString())
        dataObj.put(ValueIds.PHONE_NUMBER.value, phoneNumber.text.toString())
        dataObj.put(ValueIds.LOCATION.value, location.text.toString())
        dataObj.put(ValueIds.CURRENT_PHOTO_PATH.value, currentPhotoPath)

        sharedPref.edit {
            putString(ValueIds.JSON_OBJECT.value, dataObj.toString())
            apply()
        }
    }

    private fun loadValues() {
        val pref = sharedPref.getString(ValueIds.JSON_OBJECT.value, null)

        if (pref != null) {
            val dataObj = JSONObject(pref)
            fullName.text = dataObj.getString(ValueIds.FULL_NAME.value)
            nickName.text = dataObj.getString(ValueIds.NICKNAME.value)
            dateOfBirth.text = dataObj.getString(ValueIds.DATE_OF_BIRTH.value)
            email.text = dataObj.getString(ValueIds.EMAIL.value)
            phoneNumber.text = dataObj.getString(ValueIds.PHONE_NUMBER.value)
            location.text = dataObj.getString(ValueIds.LOCATION.value)
            currentPhotoPath = dataObj.getString(ValueIds.CURRENT_PHOTO_PATH.value)

            setPic()
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

    private fun setPic() {
        if (currentPhotoPath != "") {
            val imgFile = File(currentPhotoPath!!)
            photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this, photoURI)
            image.setImageBitmap(pic)
        } else image.setImageResource(R.drawable.atabay)
    }
}