package com.example.madproject.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Booking
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.example.madproject.lib.isFuture
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class ProfileViewModel: ViewModel() {


    private val yourProfile: MutableLiveData<Profile>
        by lazy { MutableLiveData(Profile()).also { loadProfile() } }


    private var listOfBookings : MutableLiveData<List<Booking>?> = MutableLiveData<List<Booking>?>()
        //by lazy { MutableLiveData<List<Booking>>().also {loadBookings()}}

    var localProfile = Profile()

    // Variables to manage the photo inside trip edit
    var currentPhotoPath = ""
    var bigPhotoPath = ""
    var useDBImage = false

    // Flags to know from which path the various fragments are called
    var comingFromPrivacy = false
    var needRegistration = false

    // This flag is used to maintain the state of the logout dialog
    var logoutDialogOpened = false
    var changedOrientation = false

    // Variable to manage the orientation of the screen in the async tasks
    var orientation = -1

    init {
        loadBookings()
    }

    private fun loadProfile() {
        FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                yourProfile.value = null
                return@EventListener
            }

            yourProfile.value = value?.toObject(Profile::class.java)
        })
    }

    //Function that loads all the expired confirmed booking for the user
    private fun loadBookings() {
        val retrievedBookings : MutableList<Booking> = mutableListOf()
        FirestoreRepository().getAllTrips().addSnapshotListener(EventListener {trips, e ->
            if (e != null) {
                listOfBookings.value = null
                return@EventListener
            }

            //List of all trips, except the ones owned by the user
            val trips = trips?.map { t -> t.toObject(Trip::class.java) }

            //filter only past trips
            val pastTrips = trips?.filter { t-> !isFuture(t.departureDate, t.departureTime, t.duration)}


            if (pastTrips != null) {

                //Load all user bookings for each trip
                for (trip in pastTrips) {
                    FirestoreRepository().getBooking(trip).addSnapshotListener (EventListener{ booking, e2 ->
                        if (e2 != null) {
                            listOfBookings.value = null
                        }
                        if (booking != null) {
                            for (booking in booking) {
                                val b = booking.toObject(Booking::class.java)
                                //add the relative booking in the temporary list
                                retrievedBookings.add(b)
                                Log.d("ciao", "lista dei bookings ${retrievedBookings.toString()}")
                            }


                        }
                    })
                }
                listOfBookings.value = retrievedBookings
                Log.d("ciao", "lista dei bookings ${retrievedBookings.toString()}")
            }
        })
    }

    fun getBookingByTripId(tripId: String): Booking? {
        val booking = listOfBookings.value?.filter { b-> b.tripId == tripId}?.get(0)
        Log.d("Ciao", "Bokking trovato e' ${booking.toString()}")
        return booking
    }

    fun getDBUser() : LiveData<Profile>{
        return yourProfile
    }

    fun setDBUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }
}