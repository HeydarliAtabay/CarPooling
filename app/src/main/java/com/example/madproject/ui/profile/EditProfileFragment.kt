package com.example.madproject.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import androidx.lifecycle.ViewModelProviders
import com.example.madproject.data.Profile
import com.example.madproject.lib.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private lateinit var fullName: EditText
    private lateinit var nickName: EditText
    private lateinit var location: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var dateOfBirth: EditText
    private lateinit var image: ImageView
    private var currentPhotoPath: String? = ""
    private var newPhotoPath: String? = ""
    private lateinit var photoURI: Uri
    private var profile: Profile = Profile()
    private var picker: MaterialDatePicker<Long>? = null
    private lateinit var model: SharedProfileViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        location = view.findViewById(R.id.location)
        image = view.findViewById(R.id.imageView)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        dateOfBirth = view.findViewById(R.id.dateOfBirth)

        model = ViewModelProviders.of(this)
            .get(SharedProfileViewModel::class.java)

        val storageDir: File? = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        model.getUser(storageDir).observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                profile = it
                getProfileFromShowProfile()

            }
        })

        getProfileFromShowProfile()

        fixEditText()

        setHasOptionsMenu(true)

        val editPhoto = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(editPhoto)

    }

    private fun getProfileFromShowProfile() {
        fullName.setText(profile.fullName)
        nickName.setText(profile.nickName)
        dateOfBirth.setText(profile.dateOfBirth)
        phoneNumber.setText(profile.phoneNumber)
        location.setText(profile.location)
        currentPhotoPath = profile.currentPhotoPath
        setPic()
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard()
        if (picker?.isVisible == true) picker?.dismiss()
        updateProfile()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                updateProfile()

                if (formCheck()) {
                    saveValues()
                    findNavController().navigate(R.id.action_editProfile_to_showProfile)
                } else {
                    Toast.makeText(context, "Insert the required fields to save the profile information!", Toast.LENGTH_LONG).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.imageButton -> {
                this.requireActivity().menuInflater.inflate(R.menu.menu_change_photo, menu)
            }
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_gallery -> {
                dispatchChoosePictureIntent()
                true
            }
            R.id.action_camera -> {
                dispatchTakePictureIntent()
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                Requests.INTENT_CAPTURE_PHOTO.value -> {
                    currentPhotoPath = newPhotoPath
                    setPic()
                }

                Requests.INTENT_PHOTO_FROM_GALLERY.value -> {
                    val inputStream: InputStream? = data?.data?.let {
                        this.requireActivity().contentResolver.openInputStream(
                                it
                        )
                    }
                    val outputFile = createImageFile()
                    val fileOutputStream = FileOutputStream(outputFile)
                    inputStream?.copyTo(fileOutputStream)
                    fileOutputStream.close()
                    inputStream?.close()
                    currentPhotoPath = newPhotoPath
                    setPic()
                }
            }
        }
        else {
            newPhotoPath = ""
        }
    }

    private fun closeKeyboard() {
        val v = this.requireActivity().currentFocus
        if (v != null) {
            val imm = this.requireActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun fixEditText() {
        fullName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                fullName.setSelection(0, 0)
                fullName.hint = ""
            }  else {
                view?.findViewById<TextInputLayout>(R.id.tilFullName)?.error = null
                fullName.hint = "Enter your full name"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(fullName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        nickName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                nickName.setSelection(0, 0)
                nickName.hint = ""
            } else {
                view?.findViewById<TextInputLayout>(R.id.tilNickName)?.error = null
                nickName.hint = "Enter your nickname"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nickName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        location.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                location.setSelection(0, 0)
                location.hint = ""
            } else {
                location.hint = "Enter your location"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(location, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        dateOfBirth.inputType = InputType.TYPE_NULL

        dateOfBirth.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view?.findViewById<TextInputLayout>(R.id.tilBirth)?.error = null
                setDatePicker()
            }
        }

        phoneNumber.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                phoneNumber.setSelection(0, 0)
                phoneNumber.hint = ""
            } else {
                phoneNumber.hint = "Enter your phone number"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(phoneNumber, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun setDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder().setValidator(
                DateValidatorPointBackward.now()
        )
        var datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select your date of birth")
            .setCalendarConstraints(
                constraintsBuilder.build()
            )

        if (dateOfBirth.text.toString() != "") {
            val currentDate = SimpleDateFormat("MMM dd, yyyy")
            currentDate.timeZone = TimeZone.getTimeZone("UTC")
            val p = currentDate.parse(dateOfBirth.text.toString())
            datePicker = datePicker.setSelection(p?.time)
        }
        picker = datePicker.build()

        picker?.addOnCancelListener {
            dateOfBirth.clearFocus()
        }

        picker?.addOnNegativeButtonClickListener {
            dateOfBirth.clearFocus()
        }

        picker?.addOnPositiveButtonClickListener {
            val inputFormat = SimpleDateFormat("dd MMM yyyy")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy")
            dateOfBirth.setText(outputFormat.format(inputFormat.parse(picker?.headerText!!)!!))
            phoneNumber.requestFocus()
        }

        picker?.show(this.requireActivity().supportFragmentManager, picker.toString())
    }

    private fun updateProfile() {
        profile = Profile(
            fullName = fullName.text.toString(),
            nickName = nickName.text.toString(),
            dateOfBirth = dateOfBirth.text.toString(),
            email = profile.email,
            phoneNumber = phoneNumber.text.toString(),
            location = location.text.toString(),
            currentPhotoPath = currentPhotoPath
        )
    }

    private fun formCheck(): Boolean {
        var flag = true
        if (profile.fullName == "") {
            view?.findViewById<TextInputLayout>(R.id.tilFullName)?.error = " "
            flag = false
        }
        if (profile.nickName == "") {
            view?.findViewById<TextInputLayout>(R.id.tilNickName)?.error = " "
            flag = false
        }
        if (profile.dateOfBirth == "") {
            view?.findViewById<TextInputLayout>(R.id.tilBirth)?.error = " "
            flag = false
        }
        return flag
    }

    private fun saveValues() {
        model.setUser(profile)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile information saved!", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed saving profile!", Toast.LENGTH_LONG).show()
            }
        if(profile.currentPhotoPath!=null && profile.currentPhotoPath!="" && newPhotoPath != "") {
            model.setUserImage(profile)
                .addOnFailureListener {
                    Toast.makeText(context, "Failed saving profile picture!", Toast.LENGTH_SHORT).show()
                }

        }

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            newPhotoPath = absolutePath
        }
    }

    private fun dispatchChoosePictureIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        this.requireActivity().intent.type = "image/*"
        startActivityForResult(pickIntent, Requests.INTENT_PHOTO_FROM_GALLERY.value)
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(this.requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    photoURI = FileProvider.getUriForFile(
                        this.requireActivity(),
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(
                        takePictureIntent,
                        Requests.INTENT_CAPTURE_PHOTO.value
                    )
                }
            }
        }
    }

    private fun setPic() {
        if (currentPhotoPath != "") {
            val imgFile = File(currentPhotoPath!!)
            photoURI = FileProvider.getUriForFile(
                this.requireActivity().applicationContext,
                "com.example.android.fileprovider",
                imgFile
            )
            val pic = FixOrientation.handleSamplingAndRotationBitmap(
                this.requireActivity().applicationContext,
                photoURI
            )
            image.setImageBitmap(pic)
        } else image.setImageResource(R.drawable.avatar)
    }
}