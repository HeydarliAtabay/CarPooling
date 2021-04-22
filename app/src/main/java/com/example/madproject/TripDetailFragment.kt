package com.example.madproject

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

class TripDetailFragment : Fragment(R.layout.fragment_trip_detail) {
    private var currentCarPath: String? = ""
    private lateinit var imageCar : ImageView
    private lateinit var photoCarURI: Uri
    private lateinit var departure : TextView
    private lateinit var arrival : TextView
    private lateinit var departureDate : TextView
    private lateinit var departureTime : TextView
    private lateinit var duration : TextView
    private lateinit var avalaibleSeats : TextView
    private lateinit var additionalInfo : TextView
    private lateinit var intermediateStop : TextView
    private lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageCar = view.findViewById(R.id.imageCar)
        departure = view.findViewById(R.id.departure_location)
        arrival = view.findViewById(R.id.arrival_location)
        departureDate = view.findViewById(R.id.date)
        departureTime = view.findViewById(R.id.time)
        duration = view.findViewById(R.id.duration)
        avalaibleSeats = view.findViewById(R.id.seats)
        additionalInfo = view.findViewById(R.id.info)
        intermediateStop = view.findViewById(R.id.intermediate_stops)
        sharedPref = this.requireActivity().getPreferences(Context.MODE_PRIVATE)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.edit_trip, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editTrip -> {
                findNavController().navigate(R.id.action_tripDetail_to_tripEdit)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}