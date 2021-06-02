package com.example.madproject.ui.yourtrips

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
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
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.example.madproject.lib.*
import com.example.madproject.ui.map.MapViewModel
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

@Suppress("DEPRECATION")
class TripEditFragment : Fragment(R.layout.fragment_trip_edit) {
    private var currentPhotoPath: String? = ""
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
    private lateinit var intermediateStops : EditText
    private lateinit var departureButton: ImageButton
    private lateinit var arrivalButton: ImageButton
    private lateinit var intStopsButton: ImageButton
    private var datePicker: MaterialDatePicker<Long>? = null
    private var timePicker: MaterialTimePicker? = null
    private val sharedModel: TripListViewModel by activityViewModels()
    private val mapModel: MapViewModel by activityViewModels()
    private lateinit var trip: Trip
    private var storageDir: File? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Removing the back navigation to show Map
        findNavController().popBackStack(
            R.id.showMap, true)

        imageCar = view.findViewById(R.id.imageCar)
        departure = view.findViewById(R.id.departure_location)
        arrival = view.findViewById(R.id.arrival_location)
        departureDate = view.findViewById(R.id.date)
        departureTime = view.findViewById(R.id.time)
        duration = view.findViewById(R.id.duration)
        availableSeats = view.findViewById(R.id.seats)
        price = view.findViewById(R.id.price)
        additionalInfo = view.findViewById(R.id.info)
        intermediateStops = view.findViewById(R.id.intermediate_stops)
        departureButton = view.findViewById(R.id.addDeparture)
        arrivalButton = view.findViewById(R.id.addArrival)
        intStopsButton = view.findViewById(R.id.addIntermediateStops)
        storageDir = this.requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        trip = sharedModel.selectedLocal
        currentPhotoPath = sharedModel.currentPhotoPath

        sharedModel.orientation = this.requireActivity().requestedOrientation


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

    override fun onResume() {
        super.onResume()
        setValues()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                updateTrip()
                if ((trip.duration.isNotEmpty()) && (trip.duration.length != 5)) {
                    view?.findViewById<TextInputLayout>(R.id.tilDuration)?.error = " "
                    Toast.makeText(context, "Insert the duration in the required format!", Toast.LENGTH_LONG).show()
                } else {
                    if (formCheck()) {
                        // Disable the orientation because the saving part is an Async task
                        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LOCKED
                        val f = currentPhotoPath != ""
                        saveTrip(f)
                    } else {
                        Toast.makeText(
                            context,
                            "Insert the required fields to save the trip!",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        closeKeyboard(requireActivity())
        if (datePicker?.isVisible == true) datePicker?.dismiss()
        if (timePicker?.isVisible == true) timePicker?.dismiss()
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
                    currentPhotoPath = resizeImage(this.requireActivity(), sharedModel.bigPhotoPath, storageDir?.absolutePath)
                    imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath!!))
                    sharedModel.bigPhotoPath = ""
                }

                Requests.INTENT_PHOTO_FROM_GALLERY.value -> {
                    Log.d("test", "returned intent")
                    val inputStream: InputStream? = data?.data?.let {
                        this.requireActivity().contentResolver.openInputStream(
                            it
                        )
                    }
                    val outputFile = createImageFile(storageDir?.absolutePath).apply {
                        sharedModel.bigPhotoPath = absolutePath
                    }
                    val fileOutputStream = FileOutputStream(outputFile)
                    inputStream?.copyTo(fileOutputStream)
                    fileOutputStream.close()
                    inputStream?.close()
                    currentPhotoPath = resizeImage(this.requireActivity(), sharedModel.bigPhotoPath, storageDir?.absolutePath)
                    imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath!!))
                    sharedModel.bigPhotoPath = ""
                }
            }
        } else {
            if (requestCode == Requests.INTENT_CAPTURE_PHOTO.value) {
                File(sharedModel.bigPhotoPath).delete()
                sharedModel.bigPhotoPath = ""
            }
        }
    }

    /*
    Function to manage the listeners on the edit texts
     */
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

        durationTextListener(duration, context, view)

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

        intermediateStops.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                intermediateStops.setSelection(0, 0)
                intermediateStops.hint = ""
            } else {
                intermediateStops.hint = "Intermediate stops"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(intermediateStops, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

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
            val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            currentDate.timeZone = TimeZone.getTimeZone("UTC")
            val p = currentDate.parse(departureDate.text.toString())
            dPicker = dPicker.setSelection(p?.time)
        }
        datePicker = dPicker.build()

        datePicker?.addOnCancelListener {
            departureDate.clearFocus()
        }

        datePicker?.addOnNegativeButtonClickListener {
            departureDate.clearFocus()
        }

        datePicker?.addOnPositiveButtonClickListener {

            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
            departureDate.setText(outputFormat.format(inputFormat.parse(datePicker?.headerText!!)!!))
            departureTime.requestFocus()
        }

        datePicker?.show(this.requireActivity().supportFragmentManager, datePicker.toString())
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
        timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select trip departure time")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(h)
            .setMinute(m)
            .build()

        timePicker?.addOnCancelListener {
            departureTime.clearFocus()
        }

        timePicker?.addOnNegativeButtonClickListener {
            departureTime.clearFocus()
        }

        timePicker?.addOnPositiveButtonClickListener {
            departureTime.setText(parseTime(timePicker?.hour, timePicker?.minute))
            duration.requestFocus()
        }

        timePicker?.show(this.requireActivity().supportFragmentManager, timePicker.toString())
    }

    private fun updateTrip() {
        sharedModel.selectedLocal = Trip(
            id = trip.id,
            imageUrl = trip.imageUrl,
            from = departure.text.toString(),
            to = arrival.text.toString(),
            departureDate = departureDate.text.toString(),
            departureTime = departureTime.text.toString(),
            duration = parseDuration(duration.text.toString()),
            availableSeat = availableSeats.text.toString(),
            additionalInfo = additionalInfo.text.toString(),
            intermediateStops = intermediateStops.text.toString(),
            price = parsePrice(price.text.toString()),
            ownerEmail = FirestoreRepository.currentUser.email!!,
            departureCoo = trip.departureCoordinates,
            arrivalCoo = trip.arrivalCoordinates,
            intermediateCoo = trip.intermediateCoordinates
        )
        trip = sharedModel.selectedLocal
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
        intermediateStops.setText(trip.intermediateStops)

        departureButton.setOnClickListener {
            mapModel.pathManagement = "selectDeparture"
            findNavController().navigate(R.id.action_tripEdit_to_showMap)
        }

        arrivalButton.setOnClickListener {
            mapModel.pathManagement = "selectArrival"
            findNavController().navigate(R.id.action_tripEdit_to_showMap)
        }

        intStopsButton.setOnClickListener {
            mapModel.pathManagement = "selectIntStops"
            findNavController().navigate(R.id.action_tripEdit_to_showMap)
        }

        if ((trip.imageUrl == "") && (currentPhotoPath == "")) imageCar.setImageResource(R.drawable.car_example)
        else if ((trip.imageUrl == "") && (currentPhotoPath != "")) imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        else if ((trip.imageUrl != "") && (currentPhotoPath == "")) Picasso.get().load(trip.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(imageCar)
        else {
            if (sharedModel.useDBImage) Picasso.get().load(trip.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(imageCar)
            else imageCar.setImageBitmap(BitmapFactory.decodeFile(currentPhotoPath))
        }
    }

    /*
    Start the intent to choose a new picture from the gallery
    */
    private fun dispatchChoosePictureIntent() {
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        requireActivity().intent.type = "image/*"
        startActivityForResult(pickIntent, Requests.INTENT_PHOTO_FROM_GALLERY.value)
    }

    /*
    Start the intent to capture a new picture from the default camera app
     */
    @SuppressLint("QueryPermissionsNeeded")
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(this.requireActivity().packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile(storageDir?.absolutePath).apply {
                        // Save a file: path for use with ACTION_VIEW intents
                        sharedModel.bigPhotoPath = absolutePath
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

    /*
    Check if the required fields are provided when the user clicks the save button
     */
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

    /*
    Save the new values inside Firebase
     */
    private fun saveValues() {
        sharedModel.saveTrip(trip)
            .addOnCompleteListener{
                if (it.isSuccessful) Toast.makeText(context, "Trip information saved!", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Failed saving trip!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_tripEdit_to_tripList)
                // Restore the old orientation (disabled because of the Async task)
                requireActivity().requestedOrientation = sharedModel.orientation
            }
    }

    /*
    If there is a new image, save the image inside Firebase storage and then the new profile information
     */
    private fun saveTrip(image: Boolean) {
        if (trip.id == "") {
            trip.id = FirebaseFirestore.getInstance()
                .collection("trips").document().id
        }

        if (!image) saveValues()
        else {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            val localPhoto = File(currentPhotoPath!!)
            val file = Uri.fromFile(localPhoto)
            val imageRef = storageRef.child("${FirestoreRepository.currentUser.email}/${trip.id}.jpg")
            imageRef.putFile(file)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            trip.imageUrl = uri.toString()
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
    }
}