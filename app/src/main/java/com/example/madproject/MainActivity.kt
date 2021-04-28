package com.example.madproject

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.ValueIds
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
import java.io.File
import java.lang.RuntimeException


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var sharedPref: SharedPreferences
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.fragment)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
                R.id.showProfile, R.id.tripList), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        sharedPref = getPreferences(Context.MODE_PRIVATE)

        if (!sharedPref.contains(ValueIds.JSON_OBJECT_PROFILE.value)) setDefaultNavigationHeader() else loadNavigationHeader()


    }

    private fun loadNavigationHeader() {
        val pref = sharedPref.getString(ValueIds.JSON_OBJECT_PROFILE.value, null)
        val header: View = navView.getHeaderView(0)
        val profilePictureHeader: ImageView = header.findViewById(R.id.imageViewHeader)
        val profileNameHeader: TextView = header.findViewById(R.id.nameHeader)

        if (pref != null) {
            val dataObj = JSONObject(pref)
            val profileName = dataObj.getString(ValueIds.FULL_NAME.value)
            val currentPhotoPath = dataObj.getString(ValueIds.CURRENT_PHOTO_PATH.value)
            setPic(currentPhotoPath, profilePictureHeader)
            profileNameHeader.text = profileName
        }
    }

    private fun setPic(currentPhotoPath: String, profilePicture: ImageView) {
        if (currentPhotoPath != "") {
            val imgFile = File(currentPhotoPath)
            val photoURI: Uri = FileProvider.getUriForFile(this.applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.applicationContext, photoURI)
            profilePicture.setImageBitmap(pic)
        } else profilePicture.setImageResource(R.drawable.avatar)
    }

    private fun setDefaultNavigationHeader() {
        val header : View = navView.getHeaderView(0)
        val profilePicture:ImageView  = header.findViewById(R.id.imageViewHeader)
        val profileName: TextView = header.findViewById(R.id.nameHeader)
        profilePicture.setImageResource(R.drawable.avatar)
        profileName.text = "Guest profile"
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}