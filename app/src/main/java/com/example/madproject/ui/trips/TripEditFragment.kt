package com.example.madproject.ui.trips

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.Requests
import com.example.madproject.ui.profile.ProfileViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class TripEditFragment : Fragment(R.layout.fragment_trip_edit) {
    private var currentPhotoPath: String? = ""
    private var bigPhotoPath: String? = ""
    private lateinit var imageCar : ImageView
    private lateinit var photoURI: Uri
    private lateinit var departure : EditText
    private lateinit var arrival : EditText
    private lateinit var departureDate : EditText
    private lateinit var departureTime : EditText
    private lateinit var duration : EditText
    private lateinit var availableSeats : EditText
    private lateinit var price : EditText
    private lateinit var additionalInfo : EditText
    private lateinit var intermediateStop : EditText
    private var datepicker: MaterialDatePicker<Long>? = null
    private var timepicker: MaterialTimePicker? = null
    private val sharedModel: TripListViewModel by activityViewModels()
    private val profileModel: ProfileViewModel by activityViewModels()
    private lateinit var trip: Trip
    private var profile = Profile()
    private var storageDir: File? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageCar = view.findViewById(R.id.imageCar)
        departure = view.findViewById(R.id.departure_location)
        arrival = view.findViewById(R.id.arrival_location)
        departureDate = view.findViewById(R.id.date)
        departureTime = view.findViewById(R.id.time)
        duration = view.findViewById(R.id.duration)
        availableSeats = view.findViewById(R.id.seats)
        price = view.findViewById(R.id.price)
        additionalInfo = view.findViewById(R.id.info)
        intermediateStop = view.findViewById(R.id.intermediate_stops)
        storageDir = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        profileModel.getDBUser().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(this.requireActivity(), "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                profile = it
            }
        })

        trip = sharedModel.selected
        currentPhotoPath = sharedModel.currentPhotoPath

        setValues()

        fixEditText()

        if (sharedModel.useDBImage && (currentPhotoPath != "")) {
            File(currentPhotoPath!!).delete()
            currentPhotoPath = ""
            sharedModel.currentPhotoPath = ""
        }

        sharedModel.useDBImage = false

        setHasOptionsMenu(true)

        val editPhoto = view.findViewById<ImageButton>(R.id.imageButton2)
        registerForContextMenu(editPhoto)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                updateTrip()
                if (formCheck()) {
                    if (currentPhotoPath == "") saveValues() else saveValuesImage()
                } else {
                    Toast.makeText(context, "Insert the required fields to save the trip!", Toast.LENGTH_LONG).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard()
        if (datepicker?.isVisible == true) datepicker?.dismiss()
        if (timepicker?.isVisible == true) timepicker?.dismiss()
        updateTrip()
        sharedModel.currentPhotoPath = currentPhotoPath ?: ""
    }

    override fun onCreateContextMenu(
            menu: ContextMenu,
            v: View,
            menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        when (v.id) {
            R.id.imageButton2 -> {
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
                    resizeSetImage()
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
                    resizeSetImage()
                }
            }
        } else {
            if (requestCode == Requests.INTENT_CAPTURE_PHOTO.value) {
                File(bigPhotoPath!!).delete()
                bigPhotoPath = ""
            }
        }
    }

    private fun resizeSetImage() {
        currentPhotoPath = "${storageDir?.absolutePath}/profileImage.jpg"
        val smallImageFile = File(currentPhotoPath!!)
        val fout: OutputStream = FileOutputStream(smallImageFile)

        val bigImageFile = File(bigPhotoPath!!)
        photoURI = FileProvider.getUriForFile(
            this.requireActivity().applicationContext,
            "com.example.android.fileprovider",
            bigImageFile
        )
        val pic = FixOrientation.handleSamplingAndRotationBitmap(
            this.requireActivity().applicationContext,
            photoURI
        )
        pic?.compress(Bitmap.CompressFormat.JPEG, 30, fout)
        fout.flush()
        fout.close()

        imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath!!))

        bigImageFile.delete()
        bigPhotoPath = ""
    }

    private fun closeKeyboard() {
        val v = this.requireActivity().currentFocus
        if (v != null) {
            val imm = this.requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }

    private fun fixEditText() {
        departure.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                departure.setSelection(0, 0)
                departure.hint = ""
            }  else {
                view?.findViewById<TextInputLayout>(R.id.tilDeparture)?.error = null
                departure.hint = "Departure location"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(departure, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        arrival.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                arrival.setSelection(0, 0)
                arrival.hint = ""
            } else {
                view?.findViewById<TextInputLayout>(R.id.tilArrival)?.error = null
                arrival.hint = "Arrival location"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(arrival, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        departureDate.inputType = InputType.TYPE_NULL

        departureDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view?.findViewById<TextInputLayout>(R.id.tilDate)?.error = null
                setDatePicker()
            }
        }

        departureTime.inputType = InputType.TYPE_NULL

        departureTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view?.findViewById<TextInputLayout>(R.id.tilTime)?.error = null
                setTimePicker()
            }
        }

        duration.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                duration.setSelection(0, 0)
                duration.hint = ""
            } else {
                duration.hint = "Estimated duration in hours"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(duration, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        availableSeats.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                availableSeats.setSelection(0, 0)
                availableSeats.hint = ""
            } else {
                view?.findViewById<TextInputLayout>(R.id.tilSeats)?.error = null
                availableSeats.hint = "Available seats"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(availableSeats, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        price.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                price.setSelection(0, 0)
                price.hint = ""
                price.setText(parsePrice(price.text.toString()))
            } else {
                view?.findViewById<TextInputLayout>(R.id.tilPrice)?.error = null
                price.hint = "Price"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(price, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        additionalInfo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                additionalInfo.setSelection(0, 0)
                additionalInfo.hint = ""
            } else {
                additionalInfo.hint = "Additional information"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(additionalInfo, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        intermediateStop.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                intermediateStop.setSelection(0, 0)
                intermediateStop.hint = ""
            } else {
                intermediateStop.hint = "Intermediate stops"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(intermediateStop, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun parsePrice(s: String): String {
        return if (s.contains(".")) {
            val p = s.split(".")
            val integer = if (p[0] == "") "0" else p[0]
            val dec = p[1]
            when (dec.length) {
                0 -> "$integer.00"
                1 -> "$integer.${dec}0"
                else -> "$integer.${dec[0]}${dec[1]}"
            }

        } else {
            if (s != "") {
                "$s.00"
            } else ""
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder().setValidator(
                DateValidatorPointForward.now()
        )

        var dPicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select trip departure date")
            .setCalendarConstraints(
                constraintsBuilder.build()
            )

        if (departureDate.text.toString() != "") {
            val currentDate = SimpleDateFormat("MMM dd, yyyy")
            currentDate.timeZone = TimeZone.getTimeZone("UTC")
            val p = currentDate.parse(departureDate.text.toString())
            dPicker = dPicker.setSelection(p?.time)
        }
        datepicker = dPicker.build()

        datepicker?.addOnCancelListener {
            departureDate.clearFocus()
        }

        datepicker?.addOnNegativeButtonClickListener {
            departureDate.clearFocus()
        }

        datepicker?.addOnPositiveButtonClickListener {

            val inputFormat = SimpleDateFormat("dd MMM yyyy")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy")
            departureDate.setText(outputFormat.format(inputFormat.parse(datepicker?.headerText!!)!!))
            departureTime.requestFocus()
        }

        datepicker?.show(this.requireActivity().supportFragmentManager, datepicker.toString())
    }

    private fun parseTime(hour: Int?, minute: Int?): String {
        if ((hour == null) || (minute == null)) return ""

        val h = if (hour < 10) "0$hour" else hour.toString()
        val m = if (minute < 10) "0$minute" else minute.toString()

        return "$h:$m"
    }

    private fun unParseTime(time: String): Int {
        val first = time[0]
        val second = time[1]
        if (first.toInt() == 0) return second.toInt()

        return time.toInt()
    }

    private fun setTimePicker() {
        var h = 0
        var m = 0

        if (departureTime.text.toString() != "") {
            val s = departureTime.text.toString().split(":")
            if (s.size == 2) {
                h = unParseTime(s[0])
                m = unParseTime(s[1])
            }
        }
        timepicker = MaterialTimePicker.Builder()
            .setTitleText("Select trip departure time")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(h)
            .setMinute(m)
            .build()

        timepicker?.addOnCancelListener {
            departureTime.clearFocus()
        }

        timepicker?.addOnNegativeButtonClickListener {
            departureTime.clearFocus()
        }

        timepicker?.addOnPositiveButtonClickListener {
            departureTime.setText(parseTime(timepicker?.hour, timepicker?.minute))
            duration.requestFocus()
        }

        timepicker?.show(this.requireActivity().supportFragmentManager, timepicker.toString())
    }

    private fun updateTrip() {
        sharedModel.selected = Trip(
            id = trip.id,
            imageUrl = trip.imageUrl,
            from = departure.text.toString(),
            to = arrival.text.toString(),
            departureDate = departureDate.text.toString(),
            departureTime = departureTime.text.toString(),
            duration = duration.text.toString(),
            availableSeat = availableSeats.text.toString(),
            additionalInfo = additionalInfo.text.toString(),
            intermediateStop = intermediateStop.text.toString(),
            price = price.text.toString(),
            ownerEmail = profile.email
        )
        trip = sharedModel.selected
    }

    private fun setValues() {
        departure.setText(trip.from)
        arrival.setText(trip.to)
        departureDate.setText(trip.departureDate)
        departureTime.setText(trip.departureTime)
        duration.setText(trip.duration)
        availableSeats.setText(trip.availableSeat)
        price.setText(trip.price)
        additionalInfo.setText(trip.additionalInfo)
        intermediateStop.setText(trip.intermediateStop)

        if ((trip.imageUrl == "") && (currentPhotoPath == "")) imageCar.setImageResource(R.drawable.car_example)
        else if ((trip.imageUrl == "") && (currentPhotoPath != "")) imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        else if ((trip.imageUrl != "") && (currentPhotoPath == "")) Picasso.get().load(trip.imageUrl).into(imageCar)
        else {
            if (sharedModel.useDBImage) Picasso.get().load(trip.imageUrl).into(imageCar)
            else imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val filename = "${storageDir?.absolutePath}/$timeStamp.jpg"

        return File(filename).apply {
            // Save a file: path for use with ACTION_VIEW intents
            bigPhotoPath = absolutePath
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

    private fun formCheck(): Boolean {
        var flag = true
        if (trip.from == "") {
            view?.findViewById<TextInputLayout>(R.id.tilDeparture)?.error = " "
            flag = false
        }
        if (trip.to == "") {
            view?.findViewById<TextInputLayout>(R.id.tilArrival)?.error = " "
            flag = false
        }
        if (trip.departureDate == "") {
            view?.findViewById<TextInputLayout>(R.id.tilDate)?.error = " "
            flag = false
        }
        if (trip.departureTime == "") {
            view?.findViewById<TextInputLayout>(R.id.tilTime)?.error = " "
            flag = false
        }
        if (trip.availableSeat == "") {
            view?.findViewById<TextInputLayout>(R.id.tilSeats)?.error = " "
            flag = false
        }
        if (trip.price == "") {
            view?.findViewById<TextInputLayout>(R.id.tilPrice)?.error = " "
            flag = false
        }

        return flag
    }

    private fun saveValues() {
        if (trip.id == "") {
            trip.id = FirebaseFirestore
                .getInstance()
                .collection("users/${profile.email}/createdTrips").document().id
        }

        sharedModel.saveTrip(trip)
            .addOnCompleteListener{
                if (it.isSuccessful) Toast.makeText(context, "Trip information saved!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Failed saving trip!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_tripEdit_to_tripList)
            }
    }

    private fun saveValuesImage() {
        if (trip.id == "") {
            trip.id = FirebaseFirestore
                .getInstance()
                .collection("users/${profile.email}/createdTrips").document().id
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val localPhoto = File(currentPhotoPath!!)
        val file = Uri.fromFile(localPhoto)
        val imageRef = storageRef.child("${profile.email}/${trip.id}.jpg")
        imageRef.putFile(file)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        trip.imageUrl = uri.toString()
                        sharedModel.saveTrip(trip)
                            .addOnCompleteListener{ task ->
                                if (task.isSuccessful) Toast.makeText(context, "Trip information saved!", Toast.LENGTH_SHORT).show()
                                else Toast.makeText(context, "Failed saving trip!", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_tripEdit_to_tripList)
                            }
                    }
                } else {
                    Toast.makeText(context, "Failed saving profile photo!", Toast.LENGTH_SHORT).show()
                    sharedModel.saveTrip(trip)
                        .addOnCompleteListener{ task ->
                            if (!task.isSuccessful) Toast.makeText(context, "Failed saving trip!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_tripEdit_to_tripList)
                        }
                }
                currentPhotoPath = ""
                localPhoto.delete()
            }
    }
}