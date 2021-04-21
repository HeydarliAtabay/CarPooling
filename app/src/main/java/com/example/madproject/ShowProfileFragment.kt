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
        if (!sharedPref.contains(ValueIds.JSON_OBJECT.value)) saveValues()
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
                    group11Lab1FULLNAME = fullName.text.toString(),
                    group11Lab1NICKNAME = nickName.text.toString(),
                    group11Lab1EMAIL = email.text.toString(),
                    group11Lab1DATEOFBIRTH = dateOfBirth.text.toString(),
                    group11Lab1LOCATION = location.text.toString(),
                    group11Lab1PHONENUMBER = phoneNumber.text.toString(),
                    group11Lab1CURRENTPHOTOPATH = currentPhotoPath!!
                )
                findNavController().navigate(action)
                true
            }
            else -> super.onOptionsItemSelected(item)
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