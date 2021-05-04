package com.example.madproject.ui.trips

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.InputType
import android.view.*
import androidx.fragment.app.Fragment
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.Requests
import com.example.madproject.lib.ValueIds
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

class TripEditFragment : Fragment(R.layout.fragment_trip_edit) {
    private var currentCarPath: String? = ""
    private var newCarPath: String? = ""
    private lateinit var imageCar : ImageView
    private lateinit var photoCarURI: Uri
    private lateinit var departure : EditText
    private lateinit var arrival : EditText
    private lateinit var departureDate : EditText
    private lateinit var departureTime : EditText
    private lateinit var duration : EditText
    private lateinit var availableSeats : EditText
    private lateinit var price : EditText
    private lateinit var additionalInfo : EditText
    private lateinit var intermediateStop : EditText
    private lateinit var sharedPref: SharedPreferences
    private var datepicker: MaterialDatePicker<Long>? = null
    private var timepicker: MaterialTimePicker? = null
    private val sharedModel: SharedTripViewModel by activityViewModels()
    private lateinit var trip: Trip


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
        sharedPref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)

        fixEditText()

        setHasOptionsMenu(true)

        val editPhoto = view.findViewById<ImageButton>(R.id.imageButton2)
        registerForContextMenu(editPhoto)

        setValues()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.save_profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.saveButton -> {
                saveTripValues()
                Toast.makeText(context, "Trip information saved!", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_tripEdit_to_tripList)
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
        sharedModel.select(trip)
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
                    val oldImgFile = File(currentCarPath!!)
                    oldImgFile.delete()
                    currentCarPath = newCarPath
                    setCarPic()
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
                    photoCarURI = FileProvider.getUriForFile(
                            this.requireActivity(),
                            "com.example.android.fileprovider",
                            outputFile
                    )
                    currentCarPath = newCarPath
                    setCarPic()
                }
            }
        }
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
                arrival.hint = "Arrival location"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(arrival, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        departureDate.inputType = InputType.TYPE_NULL

        departureDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                setDatePicker()
            }
        }

        departureTime.inputType = InputType.TYPE_NULL

        departureTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
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
                availableSeats.hint = "Available seats"
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(availableSeats, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        price.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                price.setSelection(0, 0)
                price.hint = ""
            } else {
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
                additionalInfo.hint = "Additional informations"
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
        trip = Trip(
                id = trip.id,
                imagePath = currentCarPath!!,
                from = departure.text.toString(),
                to = arrival.text.toString(),
                departureDate = departureDate.text.toString(),
                departureTime = departureTime.text.toString(),
                duration = duration.text.toString(),
                availableSeat = availableSeats.text.toString(),
                additionalInfo = additionalInfo.text.toString(),
                intermediateStop = intermediateStop.text.toString(),
                price = BigDecimal(if(price.text.toString()=="") "0" else price.text.toString())
        )
    }

    private fun setValues() {
        sharedModel.selected.observe(viewLifecycleOwner, { t ->
            trip = t
            departure.setText(t.from)
            arrival.setText(t.to)
            departureDate.setText(t.departureDate)
            departureTime.setText(t.departureTime)
            duration.setText(t.duration)
            availableSeats.setText(t.availableSeat)
            price.setText(t.price?.toEngineeringString())
            additionalInfo.setText(t.additionalInfo)
            intermediateStop.setText(t.intermediateStop)
            currentCarPath = t.imagePath
            setCarPic()
        })
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
            newCarPath = absolutePath
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
                    photoCarURI = FileProvider.getUriForFile(
                            this.requireActivity(),
                            "com.example.android.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoCarURI)
                    startActivityForResult(
                            takePictureIntent,
                            Requests.INTENT_CAPTURE_PHOTO.value
                    )
                }
            }
        }
    }

    private fun saveTripValues() {
        updateTrip()

        var tripList:MutableList<Trip> = mutableListOf()
        val gson = Gson()
        if (sharedPref.contains(ValueIds.JSON_OBJECT_TRIPS.value)) {
            val pref = sharedPref.getString(ValueIds.JSON_OBJECT_TRIPS.value, null)
            val type = object : TypeToken<MutableList<Trip>>() {}.type
            if (pref != null) {
                tripList = gson.fromJson(pref, type)
            }
            if (trip.id == -1) {
                trip.id = tripList.size
                tripList.add(trip)
            } else {
                tripList[trip.id] = trip
            }
        }
        else{
            trip.id = 0
            tripList.add(trip)
        }

        val stringToSave:String = gson.toJson(tripList)
        sharedPref.edit {
            putString(ValueIds.JSON_OBJECT_TRIPS.value, stringToSave)
            apply()
        }


    }

    private fun setCarPic() {
        if (currentCarPath != "") {
            val imgFile = File(currentCarPath!!)
            photoCarURI = FileProvider.getUriForFile(
                    this.requireActivity().applicationContext,
                    "com.example.android.fileprovider",
                    imgFile
            )
            val pic = FixOrientation.handleSamplingAndRotationBitmap(
                    this.requireActivity().applicationContext,
                    photoCarURI
            )
            imageCar.setImageBitmap(pic)
        } else imageCar.setImageResource(R.drawable.car_example)
    }
}