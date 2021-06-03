package com.example.madproject.ui.profile

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
import com.google.firebase.firestore.ListenerRegistration

class ProfileViewModel: ViewModel() {


    private val yourProfile: MutableLiveData<Profile>
        by lazy { MutableLiveData(Profile()).also { loadProfile() } }


    private var listOfBookings : List<Booking> = listOf()

    var localProfile = Profile()

    // Variables to manage the SnapshotListeners registrations
    private var listener1: ListenerRegistration? = null
    private var listener2: ListenerRegistration? = null
    private var listenersMap1 = mutableMapOf<String, ListenerRegistration>()


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
        listener1 = FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                yourProfile.value = null
                return@EventListener
            }

            yourProfile.value = value?.toObject(Profile::class.java)
        })
    }

    //Function that loads all the expired confirmed booking for the user
    private fun loadBookings() {
        listener2 = FirestoreRepository().getAllTrips().addSnapshotListener(EventListener {docs, e ->
            if (e != null || docs == null) {
                listOfBookings = listOf()
                return@EventListener
            }

            //List of all trips, except the ones owned by the user
            val trips = docs.map { t -> t.toObject(Trip::class.java) }

            //filter only past trips
            val pastTrips = trips.filter { t-> !isFuture(t.departureDate, t.departureTime, t.duration)}

            val retrievedBookings : MutableList<Booking> = mutableListOf()
            //Load all user bookings for each trip
            for (trip in pastTrips) {
                listenersMap1[trip.id] = FirestoreRepository().getBooking(trip).addSnapshotListener { bookDocs, e2 ->
                    if (e2 != null || bookDocs == null) {
                        listOfBookings = listOf()
                    } else {
                        for (booking in bookDocs) {
                            val b = booking.toObject(Booking::class.java)
                            //add the relative booking in the temporary list
                            if (retrievedBookings.contains(b))
                                retrievedBookings[retrievedBookings.indexOf(b)] = b
                            else
                                retrievedBookings.add(b)
                        }
                    }
                }
            }
            listOfBookings = retrievedBookings
        })
    }

    fun getBookingByTripId(tripId: String): Booking {
        val booking = listOfBookings.filter { b-> b.tripId == tripId}
        return if (booking.isNotEmpty()) booking[0] else Booking()
    }

    fun getDBUser() : LiveData<Profile>{
        return yourProfile
    }

    fun setDBUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }

    fun clearListeners() {
        listener1?.remove()
        listener2?.remove()
        for (l in listenersMap1.values) {
            l.remove()
        }
    }
}