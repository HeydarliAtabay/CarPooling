package com.example.madproject.ui.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.example.madproject.lib.*
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.*
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
    private lateinit var photoURI: Uri
    private var profile: Profile = Profile()
    private var storageDir: File? = null
    private var picker: MaterialDatePicker<Long>? = null
    private val model: ProfileViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        location = view.findViewById(R.id.location)
        image = view.findViewById(R.id.imageView)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        dateOfBirth = view.findViewById(R.id.dateOfBirth)
        storageDir = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        profile = model.localProfile
        currentPhotoPath = model.currentPhotoPath


        getProfileFromShowProfile()

        model.useDBImage = false

        fixEditText()

        setHasOptionsMenu(true)

        val editPhoto = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(editPhoto)
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard()
        if (picker?.isVisible == true) picker?.dismiss()
        updateProfile()
        model.currentPhotoPath = currentPhotoPath ?: ""

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
                    if (currentPhotoPath == "") saveValues() else saveValuesImage()
                } else {
                    Toast.makeText(
                        context,
                        "Insert the required fields to save the profile information!",
                        Toast.LENGTH_LONG
                    ).show()
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
                    currentPhotoPath = MyFunctions.resizeSetImage(this.requireActivity(), model.bigPhotoPath, storageDir?.absolutePath)
                    image.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath!!))
                    model.bigPhotoPath = ""
                }

                Requests.INTENT_PHOTO_FROM_GALLERY.value -> {
                    val inputStream: InputStream? = data?.data?.let {
                        this.requireActivity().contentResolver.openInputStream(
                            it
                        )
                    }
                    val outputFile = MyFunctions.createImageFile(storageDir?.absolutePath).apply {
                        // Save a file: path for use with ACTION_VIEW intents
                        model.bigPhotoPath = absolutePath
                    }
                    val fileOutputStream = FileOutputStream(outputFile)
                    inputStream?.copyTo(fileOutputStream)
                    fileOutputStream.close()
                    inputStream?.close()
                    currentPhotoPath = MyFunctions.resizeSetImage(this.requireActivity(), model.bigPhotoPath, storageDir?.absolutePath)
                    image.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath!!))
                    model.bigPhotoPath = ""
                }
            }
        } else {
            if (requestCode == Requests.INTENT_CAPTURE_PHOTO.value) {
                File(model.bigPhotoPath).delete()
                model.bigPhotoPath = ""
            }
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
            } else {
                view?.findViewById<TextInputLayout>(R.id.tilFullName)?.error = null
                fullName.hint = "Enter your full name"
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nickName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        location.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                location.setSelection(0, 0)
                location.hint = ""
            } else {
                location.hint = "Enter your location"
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
                val imm =
                    context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(phoneNumber, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
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

    private fun getProfileFromShowProfile() {
        fullName.setText(profile.fullName)
        nickName.setText(profile.nickName)
        dateOfBirth.setText(profile.dateOfBirth)
        phoneNumber.setText(profile.phoneNumber)
        location.setText(profile.location)

        if ((profile.imageUrl == "") && (currentPhotoPath == "")) image.setImageResource(R.drawable.avatar)
        else if ((profile.imageUrl == "") && (currentPhotoPath != "")) image.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        else if ((profile.imageUrl != "") && (currentPhotoPath == "")) Picasso.get().load(profile.imageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(image)
        else {
            if (model.useDBImage) Picasso.get().load(profile.imageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(image)
            else image.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        }
    }

    private fun updateProfile() {
        model.localProfile = Profile(
            fullName = fullName.text.toString(),
            nickName = nickName.text.toString(),
            dateOfBirth = dateOfBirth.text.toString(),
            email = profile.email,
            phoneNumber = phoneNumber.text.toString(),
            location = location.text.toString(),
            imageUrl = profile.imageUrl
        )
        profile = model.localProfile
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
        model.setDBUser(profile)
            .addOnCompleteListener{
                if (it.isSuccessful)  {
                    Toast.makeText(context, "Profile information saved!", Toast.LENGTH_SHORT).show()
                    if (model.needRegistration) {
                        model.needRegistration = false
                        findNavController().navigate(R.id.action_registerProfile_to_othersTripList)
                    } else {
                        findNavController().navigate(R.id.action_editProfile_to_showProfile)
                    }
                }
                else {
                    Toast.makeText(context, "Failed saving profile!", Toast.LENGTH_SHORT).show()
                    if (!model.needRegistration)
                        findNavController().navigate(R.id.action_editProfile_to_showProfile)
                }
            }
    }

    private fun saveValuesImage() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val localPhoto = File(currentPhotoPath!!)
        val file = Uri.fromFile(localPhoto)
        val imageRef = storageRef.child("${profile.email}/profileImage.jpg")
        imageRef.putFile(file)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        profile.imageUrl = uri.toString()
                        saveValues()
                    }
                } else {
                    Toast.makeText(context, "Failed saving profile photo!", Toast.LENGTH_SHORT)
                        .show()
                    saveValues()
                }
                currentPhotoPath = ""
                localPhoto.delete()
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
                    MyFunctions.createImageFile(storageDir?.absolutePath).apply {
                        // Save a file: path for use with ACTION_VIEW intents
                        model.bigPhotoPath = absolutePath
                    }
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
}