package com.example.madproject

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.madproject.data.Profile
import com.example.madproject.ui.profile.ProfileViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private var profile = Profile()
    private val model: ProfileViewModel by viewModels()
    private lateinit var toolbar: Toolbar
    private lateinit var header: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        model.needRegistration = intent.getBooleanExtra("INTENT_NEED_REGISTRATION_EXTRA", false)

        setNavigation()
        Log.d("test", "onCreate() finished")
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (model.changedOrientation) {
            model.changedOrientation = false
            performLogOut()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (model.logoutDialogOpened)
            model.changedOrientation = true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
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

        model.getDBUser().observe(this, {
            if (it == null && model.needRegistration) {
                loadNavigationHeader()
            } else if (it == null) {
                Toast.makeText(this, "Firebase failure!", Toast.LENGTH_LONG).show()
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

    private fun performLogOut() {
        model.logoutDialogOpened = true

        MaterialAlertDialogBuilder(this)
            .setTitle("Log out")
            .setMessage("Do you want to log out from the Car Pooling app?")
            .setPositiveButton("Yes") { _, _ ->

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                // Sign out from Google
                GoogleSignIn.getClient(this, gso).signOut()
                    .addOnCompleteListener(this) {
                        if (it.isSuccessful) {
                            // Sign out from Firebase
                            Firebase.auth.signOut()
                            Toast.makeText(this, "Succesfully logged out!", Toast.LENGTH_SHORT)
                                .show()
                            startActivity(Intent(this, AuthActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Problem in the log out!", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

            }
            .setNegativeButton("No") { _, _ ->
            }
            .setOnDismissListener {
                model.logoutDialogOpened = false
            }
            .show()
    }
}