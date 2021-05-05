package com.example.madproject.ui.profile

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.example.madproject.lib.*
import com.google.android.material.navigation.NavigationView
import java.io.File

class ShowProfileFragment : Fragment(R.layout.fragment_show_profile) {

    private lateinit var fullName : TextView
    private lateinit var nickName : TextView
    private lateinit var dateOfBirth : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var location : TextView
    private lateinit var image : ImageView
    private var currentPhotoPath: String? = ""
    private lateinit var photoURI: Uri
    private var storageDir: File? = null
    private var profile: Profile = Profile()
    private lateinit var model: SharedProfileViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        dateOfBirth = view.findViewById(R.id.dateOfBirth)
        email = view.findViewById(R.id.email)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        location = view.findViewById(R.id.location)
        image = view.findViewById(R.id.imageView3)
        storageDir = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        model = ViewModelProvider(this, ViewModelFactory(storageDir))
            .get(SharedProfileViewModel::class.java)
        /*model = ViewModelProviders.of(this)
            .get(SharedProfileViewModel::class.java)*/

        model.getUser().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                profile = it
                setProfile()
            }
        })
        setProfile()

        //aggiorna il navigation header
        setNavigationHeader()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                findNavController().navigate(R.id.action_showProfile_to_editProfile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setProfile(){
        fullName.text = profile.fullName
        nickName.text = profile.nickName
        dateOfBirth.text = profile.dateOfBirth
        email.text = profile.email
        phoneNumber.text = profile.phoneNumber
        location.text = profile.location
        currentPhotoPath = profile.currentPhotoPath
        setPic()
    }

    private fun setPic() {
        if (currentPhotoPath != "") {
            val filename = "${storageDir?.absolutePath}/${currentPhotoPath}"
            val imgFile = File(filename)
            photoURI = FileProvider.getUriForFile(this.requireActivity().applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.requireActivity().applicationContext, photoURI)
            image.setImageBitmap(pic)
        } else image.setImageResource(R.drawable.avatar)
    }

    private fun setNavigationHeader(){
        val navView = this.requireActivity().findViewById<NavigationView>(R.id.nav_view)
        val header: View = navView.getHeaderView(0)
        val profilePictureHeader: ImageView = header.findViewById(R.id.imageViewHeader)
        val profileNameHeader: TextView = header.findViewById(R.id.nameHeader)

        profileNameHeader.text = fullName.text

        if (currentPhotoPath != "") {
            val filename = "${storageDir?.absolutePath}/${currentPhotoPath}"
            val imgFile = File(filename)
            photoURI = FileProvider.getUriForFile(this.requireActivity().applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.requireActivity().applicationContext, photoURI)
            profilePictureHeader.setImageBitmap(pic)
        } else profilePictureHeader.setImageResource(R.drawable.avatar)


    }
}