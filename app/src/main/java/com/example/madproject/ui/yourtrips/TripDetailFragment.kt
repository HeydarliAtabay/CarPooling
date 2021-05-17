package com.example.madproject.ui.yourtrips

import android.content.DialogInterface
import android.os.Bundle
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
import com.example.madproject.ui.profile.ProfileViewModel
import com.example.madproject.ui.yourtrips.interestedusers.UserListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class TripDetailFragment : Fragment(R.layout.fragment_trip_detail) {
    private lateinit var imageCar: ImageView
    private lateinit var departure: TextView
    private lateinit var arrival: TextView
    private lateinit var departureDate: TextView
    private lateinit var departureTime: TextView
    private lateinit var duration: TextView
    private lateinit var availableSeats: TextView
    private lateinit var trip: Trip
    private lateinit var price: TextView
    private lateinit var additionalInfo: TextView
    private lateinit var intermediateStops: TextView
    private lateinit var fabBooking: FloatingActionButton
    private lateinit var fabDelete: FloatingActionButton
    private val sharedModel: TripListViewModel by activityViewModels()
    private val userListModel: UserListViewModel by activityViewModels()
    private val profileModel: ProfileViewModel by activityViewModels()

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
        intermediateStops = view.findViewById(R.id.intermediate_stops)

        // reset the flag to "false", since this fragment will set it to "true" if the required navigation is selected
        profileModel.comingFromPrivacy = false

        if (!sharedModel.comingFromOther) {
            userListModel.resetFilteredUsers()
        }

        fabBooking = view.findViewById(R.id.fabBooking)
        fabDelete = view.findViewById(R.id.fabDelete)

        if (!sharedModel.comingFromOther) {
            fabBooking.hide()
            fabDelete.show()
            fabDelete.setOnClickListener {
                deleteTripDialog()
            }
        } else {
            fabDelete.hide()
            fabBooking.show()
            fabBooking.setOnClickListener {
                createBookingDialog()
            }
        }

        trip = sharedModel.selectedLocal

        sharedModel.getSelectedDB(trip).observe(viewLifecycleOwner, {
            if (it == null) {
                if (sharedModel.comingFromOther) {
                    Toast.makeText(context, "This trip does not exist anymore!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_tripDetail_to_othersTripList)
                } else
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_SHORT).show()
            } else {
                trip = it
                setValuesTrip()
            }
        })

        setHasOptionsMenu(true)

        setValuesTrip()

        if (sharedModel.changedOrientationBooking) {
            createBookingDialog()
            sharedModel.changedOrientationBooking = false
        }

        if (sharedModel.changedOrientationDelete) {
            deleteTripDialog()
            sharedModel.changedOrientationDelete = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (sharedModel.bookingDialogOpened)
            sharedModel.changedOrientationBooking = true
        if (sharedModel.deleteDialogOpened)
            sharedModel.changedOrientationDelete = true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (!sharedModel.comingFromOther) {
            inflater.inflate(R.menu.show_profiles, menu)
            inflater.inflate(R.menu.edit_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                sharedModel.useDBImage = true
                sharedModel.selectedLocal = trip
                findNavController().navigate(R.id.action_tripDetail_to_tripEdit)
                true
            }
            R.id.profilesButton -> {
                userListModel.selectedLocalTrip = trip
                userListModel.tabBookings = false
                profileModel.comingFromPrivacy = true
                findNavController().navigate(R.id.action_tripDetail_to_userList)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun createBookingDialog() {
        sharedModel.bookingDialogOpened = true
        MaterialAlertDialogBuilder(this.requireActivity())
            .setTitle("New Booking")
            .setMessage("Are you sure to book this trip?")
            .setPositiveButton("Yes") { _, _ ->
                bookTheTrip()
            }
            .setNegativeButton("No") { _, _ -> }
            .setOnDismissListener {
                sharedModel.bookingDialogOpened = false
            }
            .show()
    }

    private fun deleteTripDialog() {
        sharedModel.deleteDialogOpened = true
        MaterialAlertDialogBuilder(this.requireActivity())
            .setTitle("Delete trip")
            .setMessage("Are you sure to delete this trip?")
            .setPositiveButton("Yes") { _, _ ->
                FirestoreRepository().deleteTrip(trip)
                    .addOnSuccessListener {

                        FirebaseStorage.getInstance().reference
                            .child("${FirestoreRepository.auth.email}/${trip.id}.jpg")
                            .delete()

                        Toast.makeText(
                            this.requireActivity(),
                            "Trip successfully deleted!",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigate(R.id.action_tripDetail_to_tripList)
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            this.requireActivity(),
                            "Failure in removing the trip!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("No") { _, _ ->
            }
            .setOnDismissListener {
                sharedModel.deleteDialogOpened = false
            }
            .show()
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
        intermediateStops.text = trip.intermediateStops
        if (trip.imageUrl != "") {
            Picasso.get().load(trip.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(imageCar)
        } else imageCar.setImageResource(R.drawable.car_example)
    }

    private fun bookTheTrip() {
        FirestoreRepository().controlBooking(trip)
            .addOnSuccessListener {
                if (it.documents.size != 0) {
                    Toast.makeText(this.requireActivity(), "Trip already booked", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    FirestoreRepository().proposeBooking(trip)
                        .addOnSuccessListener {
                            Toast.makeText(this.requireActivity(), "Booking request successfully sent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this.requireActivity(), "The booking request had a trouble, please retry!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this.requireActivity(), "DB access failure", Toast.LENGTH_SHORT).show()
            }
    }

    /*
    private fun bookTheTrip() {
        FirestoreRepository().controlBooking(trip)
            .addOnSuccessListener {
                if (it.documents.size != 0) {
                    Toast.makeText(this.requireActivity(), "Trip already booked", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    try {
                        FirestoreRepository().bookingTransaction(trip)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this.requireActivity(),
                                    "Trip booked successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { mes ->
                                Toast.makeText(
                                    this.requireActivity(), mes.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                    } catch (e: FirebaseFirestoreException) {
                        Toast.makeText(this.requireActivity(), e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this.requireActivity(), "DB access failure", Toast.LENGTH_SHORT)
                    .show()
            }
    }
     */
}