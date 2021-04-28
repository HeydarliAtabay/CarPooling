package com.example.madproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.Requests
import com.example.madproject.lib.ValueIds
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {
    private lateinit var fullName: EditText
    private lateinit var nickName: EditText
    private lateinit var email: EditText
    private lateinit var location: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var dateOfBirth: EditText
    private lateinit var image:ImageView
    private var currentPhotoPath: String? = ""
    private lateinit var photoURI: Uri
    private lateinit var sharedPref: SharedPreferences
    private var picker: MaterialDatePicker<Long>? = null

    private val args: EditProfileFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val resPath = savedInstanceState?.getString("currentPhotoPath")
        currentPhotoPath = if (resPath === null) "" else resPath
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        email = view.findViewById(R.id.email)
        location = view.findViewById(R.id.location)
        image = view.findViewById(R.id.imageView)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        dateOfBirth = view.findViewById(R.id.dateOfBirth)
        sharedPref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)

        fixEditText()

        setHasOptionsMenu(true)

        val editPhoto = view.findViewById<ImageButton>(R.id.imageButton)
        registerForContextMenu(editPhoto)

        setValues()
    }

    override fun onPause() {
        super.onPause()
        if (picker?.isVisible == true) picker?.dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentPhotoPath", currentPhotoPath)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                Toast.makeText(context, "Profile information saved!", Toast.LENGTH_LONG).show()
                /*
                val snack = Snackbar.make(this.requireActivity().findViewById(R.id.cLayout), R.string.profile_save, Snackbar.LENGTH_SHORT)

                val tv = snack.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    tv.textAlignment = View.TEXT_ALIGNMENT_CENTER;
                } else {
                    tv.gravity = Gravity.CENTER_HORIZONTAL;
                }
                snack.show()*/
                saveValues()
                findNavController().navigate(R.id.action_editProfile_to_showProfile)
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
                Requests.INTENT_CAPTURE_PHOTO.value -> setPic()

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
                    photoURI = FileProvider.getUriForFile(
                            this.requireActivity(),
                            "com.example.android.fileprovider",
                            outputFile
                    )
                    setPic()
                }
            }
        }
    }

    private fun fixEditText() {
        fullName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                fullName.setSelection(0, 0)
                fullName.hint = ""
            }  else {
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
                nickName.hint = "Enter your nickname"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nickName, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        email.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                email.setSelection(0, 0)
                email.hint = ""
            } else {
                email.hint = "email@email.com"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(email, InputMethodManager.SHOW_IMPLICIT)
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
        var datePicker = MaterialDatePicker.Builder.datePicker().setCalendarConstraints(
                constraintsBuilder.build()
        )

        if (dateOfBirth.text.toString() != "") {
            val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.ROOT)
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
            //val selectedDate = DateFormat.getDateInstance(DateFormat.SHORT).format(Date(it))
            //dateOfBirth.setText(selectedDate)

            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ROOT)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ROOT)
            dateOfBirth.setText(outputFormat.format(inputFormat.parse(picker?.headerText!!)!!))
            email.requestFocus()
        }

        picker?.show(this.requireActivity().supportFragmentManager, picker.toString())
    }

    private fun setValues() {
        if (args.group11Lab2FULLNAME == "Guest profile") fullName.setText("") else fullName.setText(args.group11Lab2FULLNAME)
        nickName.setText(args.group11Lab2NICKNAME)
        email.setText(args.group11Lab2EMAIL)
        dateOfBirth.setText(args.group11Lab2DATEOFBIRTH)
        phoneNumber.setText(args.group11Lab2PHONENUMBER)
        location.setText(args.group11Lab2LOCATION)
        if (currentPhotoPath == "") currentPhotoPath = args.group11Lab2CURRENTPHOTOPATH
        setPic()
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
            putString(ValueIds.JSON_OBJECT_PROFILE.value, dataObj.toString())
            apply()
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
            currentPhotoPath = absolutePath
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