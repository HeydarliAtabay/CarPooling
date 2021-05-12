package com.example.madproject


import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.lib.Requests
import com.example.madproject.ui.profile.ProfileViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private var profile = Profile()
    private lateinit var model: ProfileViewModel
    private lateinit var toolbar: Toolbar
    private lateinit var header: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        setNavigation()

    }

    private fun setNavigation(){

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.fragment)
        header = navView.getHeaderView(0)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.showProfile, R.id.tripList, R.id.othersTripList), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        model = ViewModelProviders.of(this)
            .get(ProfileViewModel::class.java)

        model.getDBUser().observe(this, {
            if (it == null) {
                Toast.makeText(this, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                profile = it
                loadNavigationHeader()
            }
        })
        loadNavigationHeader()

        val logoutButton = header.findViewById<ImageButton>(R.id.log_out_button)

        logoutButton.setOnClickListener {
            performLogOut()
        }

    }

    private fun loadNavigationHeader() {
        val profilePictureHeader: ImageView = header.findViewById(R.id.imageUser)
        val profileNameHeader: TextView = header.findViewById(R.id.nameHeader)
        profileNameHeader.text = profile.fullName
        if (profile.imageUrl != "") {
            Picasso.get().load(profile.imageUrl).into(profilePictureHeader)
        } else profilePictureHeader.setImageResource(R.drawable.avatar)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun performLogOut() {
        Firebase.auth.signOut()
        Toast.makeText(this, "Succesfully logged out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this,AuthActivity::class.java))
        finish()
    }
}