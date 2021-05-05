package com.example.madproject.ui.trips

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.lib.FixOrientation
import java.io.File

class TripDetailFragment : Fragment(R.layout.fragment_trip_detail) {
    private var currentCarPath: String? = ""
    private lateinit var imageCar : ImageView
    private lateinit var photoCarURI: Uri
    private lateinit var departure : TextView
    private lateinit var arrival : TextView
    private lateinit var departureDate : TextView
    private lateinit var departureTime : TextView
    private lateinit var duration : TextView
    private lateinit var availableSeats : TextView
    private lateinit var price : TextView
    private lateinit var additionalInfo : TextView
    private lateinit var intermediateStop : TextView
    private lateinit var sharedPref: SharedPreferences
    private val sharedModel: SharedTripViewModel by activityViewModels()

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

        setHasOptionsMenu(true)

        setValuesTrip()
    }

    private fun setValuesTrip() {
        sharedModel.selected.observe(viewLifecycleOwner, { trip ->
            departure.text = trip.from
            arrival.text = trip.to
            departureDate.text = trip.departureDate
            departureTime.text = trip.departureTime
            duration.text = trip.duration
            availableSeats.text = trip.availableSeat
            price.text = trip.price
            additionalInfo.text = trip.additionalInfo
            intermediateStop.text = trip.intermediateStop
            currentCarPath = trip.imagePath
            setCarPic()
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_menu, menu)
    }

    private fun setCarPic() {
        if (currentCarPath != "") {
            val imgFile = File(currentCarPath!!)
            photoCarURI = FileProvider.getUriForFile(this.requireActivity().applicationContext, "com.example.android.fileprovider", imgFile)
            val pic = FixOrientation.handleSamplingAndRotationBitmap(this.requireActivity().applicationContext, photoCarURI)
            imageCar.setImageBitmap(pic)
        } else imageCar.setImageResource(R.drawable.car_example)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                findNavController().navigate(R.id.action_tripDetail_to_tripEdit)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}