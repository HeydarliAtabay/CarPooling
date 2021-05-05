package com.example.madproject

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madproject.data.Profile
import com.example.madproject.lib.FixOrientation
import com.example.madproject.ui.profile.SharedProfileViewModel
import com.example.madproject.ui.profile.ViewModelFactory
import com.google.android.material.navigation.NavigationView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private var profile = Profile()
    private lateinit var model: SharedProfileViewModel

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

        val storageDir: File? = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        model = ViewModelProvider(this, ViewModelFactory(storageDir))
            .get(SharedProfileViewModel::class.java)


        model.getUser().observe(this, {
            if (it == null) {
                Toast.makeText(this, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                profile = it
                loadNavigationHeader()
            }
        })
        loadNavigationHeader()
    }

    private fun loadNavigationHeader() {
        val header: View = navView.getHeaderView(0)
        val profilePictureHeader: ImageView = header.findViewById(R.id.imageViewHeader)
        val profileNameHeader: TextView = header.findViewById(R.id.nameHeader)

        val profileName = profile.fullName
        val currentPhotoPath = profile.currentPhotoPath ?: ""
        setPic(currentPhotoPath, profilePictureHeader)
        profileNameHeader.text = profileName
    }

    private fun setPic(currentPhotoPath: String, profilePicture: ImageView) {
        if (currentPhotoPath != "") {
            val filename = "${this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.absolutePath}/$currentPhotoPath"
            val imgFile = File(filename)
            val photoURI: Uri = FileProvider.getUriForFile(this.applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.applicationContext, photoURI)
            profilePicture.setImageBitmap(pic)
        } else profilePicture.setImageResource(R.drawable.avatar)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

}