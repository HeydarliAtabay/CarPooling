package com.example.madproject.ui.trips

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestoreException
import com.squareup.picasso.Picasso

class TripDetailFragment : Fragment(R.layout.fragment_trip_detail) {
    private lateinit var imageCar : ImageView
    private lateinit var departure : TextView
    private lateinit var arrival : TextView
    private lateinit var departureDate : TextView
    private lateinit var departureTime : TextView
    private lateinit var duration : TextView
    private lateinit var availableSeats : TextView
    private lateinit var trip: Trip
    private lateinit var price : TextView
    private lateinit var additionalInfo : TextView
    private lateinit var intermediateStop : TextView
    private val sharedModel: TripListViewModel by activityViewModels()

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

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        /*if(!sharedModel.comingFromOther) {
            fab.hide()
        }else {
            fab.show()*/
            fab.setOnClickListener{
                FirestoreRepository().controlBooking(trip)
                    .addOnSuccessListener {
                        Log.d("test" , it.documents.size.toString())
                        if(it.documents.size != 0){
                            Toast.makeText(this.requireActivity(), "Trip already booked", Toast.LENGTH_LONG ).show()
                        }else{
                            try {
                                FirestoreRepository().bookingTransaction(trip)
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            this.requireActivity(),
                                            "Trip booked successfully",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(
                                            this.requireActivity(),
                                            it.message,
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            } catch (e: FirebaseFirestoreException){
                                Toast.makeText(
                                    this.requireActivity(),
                                    e.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(this.requireActivity(), "DB access failure", Toast.LENGTH_SHORT ).show()
                    }
            }
        //}



        trip = sharedModel.selected

        setHasOptionsMenu(true)

        setValuesTrip()
    }

    private fun setValuesTrip() {
        departure.text = trip.from
        arrival.text = trip.to
        departureDate.text = trip.departureDate
        departureTime.text = trip.departureTime
        duration.text = trip.duration
        availableSeats.text = trip.availableSeat
        price.text = trip.price
        additionalInfo.text = trip.additionalInfo
        intermediateStop.text = trip.intermediateStop
        if (trip.imageUrl != "") {
            Picasso.get().load(trip.imageUrl).into(imageCar)
        } else imageCar.setImageResource(R.drawable.car_example)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        Log.d("test", "onCreateOptions")
        if(!sharedModel.comingFromOther) {
            inflater.inflate(R.menu.show_profiles, menu)
            inflater.inflate(R.menu.edit_menu, menu)
        } else sharedModel.comingFromOther = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                sharedModel.useDBImage = true
                findNavController().navigate(R.id.action_tripDetail_to_tripEdit)
                true
            }
            R.id.profilesButton -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}