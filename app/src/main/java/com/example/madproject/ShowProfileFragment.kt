package com.example.madproject

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.navigation.fragment.findNavController
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.ValueIds
import com.google.android.material.navigation.NavigationView
import org.json.JSONObject
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
    private lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        dateOfBirth = view.findViewById(R.id.dateOfBirth)
        email = view.findViewById(R.id.email)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        location = view.findViewById(R.id.location)
        image = view.findViewById(R.id.imageView3)
        sharedPref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)

        // If the sharedPref does not exist (first run) it is created with the default values
        if (!sharedPref.contains(ValueIds.JSON_OBJECT_PROFILE.value)) saveValues()
        loadValues()
        //aggiorna il navigation header
        setNavigationHeader()

        setHasOptionsMenu(true)
    }

    fun getCurrentPhoto():String?{
        return currentPhotoPath
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                val action = ShowProfileFragmentDirections.actionShowProfileToEditProfile(
                    group11Lab2FULLNAME = fullName.text.toString(),
                    group11Lab2NICKNAME = nickName.text.toString(),
                    group11Lab2EMAIL = email.text.toString(),
                    group11Lab2DATEOFBIRTH = dateOfBirth.text.toString(),
                    group11Lab2LOCATION = location.text.toString(),
                    group11Lab2PHONENUMBER = phoneNumber.text.toString(),
                    group11Lab2CURRENTPHOTOPATH = currentPhotoPath!!
                )
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveValues() {
        val dataObj = JSONObject()

        if (fullName.text.toString() == "Guest profile") dataObj.put(ValueIds.FULL_NAME.value, "")
        else dataObj.put(ValueIds.FULL_NAME.value, fullName.text.toString())

        dataObj.put(ValueIds.NICKNAME.value, nickName.text.toString())
        dataObj.put(ValueIds.DATE_OF_BIRTH.value, dateOfBirth.text.toString())
        dataObj.put(ValueIds.EMAIL.value, email.text.toString())
        dataObj.put(ValueIds.PHONE_NUMBER.value, phoneNumber.text.toString())
        dataObj.put(ValueIds.LOCATION.value, location.text.toString())
        dataObj.put(ValueIds.CURRENT_PHOTO_PATH.value, currentPhotoPath)

        sharedPref.edit {
            putString(ValueIds.JSON_OBJECT_PROFILE.value, dataObj.toString())
            apply()
        }
    }

    private fun loadValues() {
        val pref = sharedPref.getString(ValueIds.JSON_OBJECT_PROFILE.value, null)

        if (pref != null) {
            val dataObj = JSONObject(pref)
            if (dataObj.getString(ValueIds.FULL_NAME.value) == "") fullName.text = "Guest profile"
            else fullName.text = dataObj.getString(ValueIds.FULL_NAME.value)
            nickName.text = dataObj.getString(ValueIds.NICKNAME.value)
            dateOfBirth.text = dataObj.getString(ValueIds.DATE_OF_BIRTH.value)
            email.text = dataObj.getString(ValueIds.EMAIL.value)
            phoneNumber.text = dataObj.getString(ValueIds.PHONE_NUMBER.value)
            location.text = dataObj.getString(ValueIds.LOCATION.value)
            currentPhotoPath = dataObj.getString(ValueIds.CURRENT_PHOTO_PATH.value)

            setPic()
        }
    }

    private fun setPic() {
        if (currentPhotoPath != "") {
            val imgFile = File(currentPhotoPath!!)
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
            val imgFile = File(currentPhotoPath!!)
            photoURI = FileProvider.getUriForFile(this.requireActivity().applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.requireActivity().applicationContext, photoURI)
            profilePictureHeader.setImageBitmap(pic)
        } else profilePictureHeader.setImageResource(R.drawable.avatar)


    }
}