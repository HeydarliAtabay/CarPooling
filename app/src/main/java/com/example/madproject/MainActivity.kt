package com.example.madproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
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
import com.example.madproject.lib.performLogout
import com.example.madproject.ui.comments.RatingsViewModel
import com.example.madproject.ui.map.ShowMapFragment
import com.example.madproject.ui.profile.ProfileViewModel
import com.example.madproject.ui.yourtrips.TripListViewModel
import com.example.madproject.ui.yourtrips.interestedusers.UserListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.squareup.picasso.Picasso
import org.osmdroid.config.Configuration
import org.osmdroid.config.IConfigurationProvider
import org.osmdroid.tileprovider.util.StorageUtils

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navView: NavigationView
    private var profile = Profile()
    private val profileModel: ProfileViewModel by viewModels()
    private val ratingsModel: RatingsViewModel by viewModels()
    private val userListModel: UserListViewModel by viewModels()
    private val tripListModel: TripListViewModel by viewModels()
    private lateinit var toolbar: Toolbar
    private lateinit var header: View

    private var showMapFragment: ShowMapFragment? = null

    private val networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                showMapFragment?.invalidateMapView()
            } catch (e: NullPointerException) {
                Toast.makeText(this@MainActivity, "Cannot initialize the Map View!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the flag from the intent extra in order to know if the first registration is needed
        profileModel.needRegistration = intent.getBooleanExtra("INTENT_NEED_REGISTRATION_EXTRA", false)

        val provider: IConfigurationProvider = Configuration.getInstance()
        provider.userAgentValue = BuildConfig.APPLICATION_ID
        provider.osmdroidBasePath = StorageUtils.getStorage()
        provider.osmdroidTileCache = StorageUtils.getStorage()
        setContentView(R.layout.activity_main)

        registerReceiver(networkReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        // Setup the Material Toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        setNavigation()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (profileModel.changedOrientation) {
            profileModel.changedOrientation = false
            // if the orientation is changed with the logout dialog opened, reopen it
            logOut()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
        if (profileModel.logoutDialogOpened)
            profileModel.changedOrientation = true
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

        profileModel.getDBUser().observe(this, {
            if (it == null && profileModel.needRegistration) {
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
            logOut()
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

    /*
    Function to perform the logout and to return to the Auth Activity
     */
    private fun logOut() {
        profileModel.logoutDialogOpened = true

        MaterialAlertDialogBuilder(this)
            .setTitle("Log out")
            .setMessage("Do you want to log out from the Car Pooling app?")
            .setPositiveButton("Yes") { _, _ ->

                // Remove all the Snapshot Listeners on Firebase before logging out
                profileModel.clearListeners()
                ratingsModel.clearListeners()
                userListModel.clearListeners()
                tripListModel.clearListeners()

                performLogout(getString(R.string.default_web_client_id), this, this)

            }
            .setNegativeButton("No") { _, _ ->
            }
            .setOnDismissListener {
                profileModel.logoutDialogOpened = false
            }
            .show()
    }
}