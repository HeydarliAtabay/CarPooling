package com.example.madproject.ui.yourtrips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Booking
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class TripListViewModel: ViewModel() {

    private val userTrips: MutableLiveData<List<Trip>>
        by lazy { MutableLiveData<List<Trip>>().also { loadUserTrips() } }

    private val otherTrips: MutableLiveData<List<Trip>>
        by lazy { MutableLiveData<List<Trip>>().also {
            loadOtherTrips()
        } }

    private val confirmedTrips: MutableLiveData<List<Trip>> = MutableLiveData()
    private val interestedTrips: MutableLiveData<List<Trip>> = MutableLiveData()

    private var selectedDB: MutableLiveData<Trip> = MutableLiveData()

    var selectedLocal = Trip()

    // Vars to manage the photos
    var currentPhotoPath = ""
    var bigPhotoPath = ""
    var useDBImage = false

    // Flags to manage the Dialog when the orientation changes
    var bookingDialogOpened = false
    var changedOrientationBooking = false
    var deleteDialogOpened = false
    var changedOrientationDelete = false

    // Flags used to manage the trip booking
    var comingFromOther = false

    // Flag to manage the landscape selection of the tab
    var tabCompletedTrips = false

    // Data used to manage the booking dialog restore state from OtherTripsFragment
    var tripIdInDialog = ""

    // Variable to manage the orientation of the screen in the async tasks
    var orientation = -1

    /*
    Function used to load the trips created by the current user from Firebase, it is used a
    Snapshot listener in order to keep the list updated
     */
    private fun loadUserTrips() {
        FirestoreRepository().getUserTrips().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                userTrips.value = null
                return@EventListener
            }

            val retrievedTrips: MutableList<Trip> = mutableListOf()
            for (doc in value!!) {
                val t = doc.toObject(Trip::class.java)
                retrievedTrips.add(t)
                if (t.id == selectedLocal.id) selectedDB.value = t
            }
            userTrips.value = retrievedTrips
        })
    }

    /*
    Function used to populate the lists which contains the trips created by other users.
    Implementation:
        - Get the entire collection of trips created by other users
        - For each trip find if that trip received a proposal by the current user (1), the user has got
          a booking confirmation on that trip (2), or it is still a bookable trip (3):
            (1). Add the trip to "interestedTrips", and delete it from the other two lists (if present)
            (2). Add the trip to "confirmedTrips", and delete it from the other two lists (if present)
            (3). If there are no more available seats -> remove from "otherTrips", else add it to "otherTrips"
     */
    private fun loadOtherTrips() {
        FirestoreRepository().getAllTrips()
            .addSnapshotListener(EventListener { value, error ->
                if (error != null) {
                    otherTrips.value = null
                    confirmedTrips.value = null
                    interestedTrips.value = null
                    return@EventListener
                }
                val retrievedAvTrips: MutableList<Trip> =
                    mutableListOf() // retrieved Available trips (no proposed or booked)
                val retrievedIntTrips: MutableList<Trip> =
                    mutableListOf() // retrieved Interested trips (with a booking proposal)
                val retrievedConfTrips: MutableList<Trip> =
                    mutableListOf() // retrieved Confirmed trips (whit a confirmed booking)

                for (t in value!!) {
                    val trip = t.toObject(Trip::class.java)
                    // Update the trip which is in "tripDetail"
                    if (trip == selectedLocal) selectedDB.value = trip

                    // For each trip query Firebase in order to get the related Bookings
                    FirestoreRepository().getProposals(trip)
                        .addSnapshotListener(EventListener { prop, error1 ->
                            Log.d("test", "Listener Proposals")
                            if (error1 != null) {
                                otherTrips.value = null
                                confirmedTrips.value = null
                                interestedTrips.value = null
                                return@EventListener
                            }

                            val myProposals = prop?.map { p -> p.toObject(Booking::class.java) }
                                ?.filter { p -> p.clientEmail == FirestoreRepository.currentUser.email }

                            if (myProposals?.isEmpty() == true) {
                                // If it does not find any proposal, look for a confirmed booking
                                FirestoreRepository().getConfirmed(trip)
                                    .addSnapshotListener(EventListener { conf, error2 ->

                                        if (error2 != null) {
                                            otherTrips.value = null
                                            confirmedTrips.value = null
                                            interestedTrips.value = null
                                            return@EventListener
                                        }

                                        val myConfirmed = conf?.map { p -> p.toObject(Booking::class.java) }
                                            ?.filter { p -> p.clientEmail == FirestoreRepository.currentUser.email }

                                        if (myConfirmed?.isEmpty() == true) {
                                            // If it does not also find any confirmed booking, add the trip to otherTrips
                                            if (trip.availableSeat.toInt() == 0) {
                                                if (trip == selectedDB.value) selectedDB.value = null
                                                if (retrievedAvTrips.contains(trip)) { retrievedAvTrips.remove(trip) }

                                            } else retrievedAvTrips.add(trip)

                                            otherTrips.value = retrievedAvTrips
                                        } else {
                                            // If it finds a confirmed booking, add the trip to confirmedTrips

                                            // Check if this trip is in another list and remove it
                                            if (retrievedAvTrips.contains(trip)) {
                                                retrievedAvTrips.remove(trip)
                                                otherTrips.value = retrievedAvTrips
                                            }
                                            if (retrievedIntTrips.contains(trip)) {
                                                retrievedIntTrips.remove(trip)
                                                confirmedTrips.value = retrievedIntTrips
                                            }

                                            retrievedConfTrips.add(trip)
                                            //Log.d("test", "Confirmed Trips triggered -> $retrievedConfTrips")
                                            confirmedTrips.value = retrievedConfTrips
                                        }
                                    })
                            } else {
                                // If it finds a proposal, add the trip to interestedTrips

                                // Check if this trip is in another list and remove it
                                if (retrievedAvTrips.contains(trip)) {
                                    retrievedAvTrips.remove(trip)
                                    otherTrips.value = retrievedAvTrips
                                }
                                if (retrievedConfTrips.contains(trip)) {
                                    retrievedConfTrips.remove(trip)
                                    confirmedTrips.value = retrievedConfTrips
                                }

                                retrievedIntTrips.add(trip)
                                interestedTrips.value = retrievedIntTrips
                            }
                        })
                }
            })
    }

    /*
   Function used for the procedure of loading dynamically (looking for DB changes), the trip on
   trip detail fragment:
       - "t" is the trip selected, it is needed to get live updates for this trip
       - The function looks for this trip in the populated lists and updates the MutableLiveData
         "selectedDB"
       - "selectedDB" will remain update inside the snapshot listeners in the above functions
    */
    fun getSelectedDB(t: Trip): LiveData<Trip> {
        // Check whether the selected trip is contained in the "userTrips"
        if (userTrips.value != null) {
            if (userTrips.value!!.contains(t))
                selectedDB.value = userTrips.value!![userTrips.value!!.indexOf(t)]
        }

        // Check whether the selected trip is contained in the "otherTrips"
        if (otherTrips.value != null) {
            if (otherTrips.value!!.contains(t))
                selectedDB.value = otherTrips.value!![otherTrips.value!!.indexOf(t)]
        }

        // Check whether the selected trip is contained in the "interestedTrips"
        if (interestedTrips.value != null) {
            if (interestedTrips.value!!.contains(t))
                selectedDB.value = interestedTrips.value!![interestedTrips.value!!.indexOf(t)]
        }

        // Check whether the selected trip is contained in the "confirmedTrips"
        if (confirmedTrips.value == null) return selectedDB

        if (confirmedTrips.value!!.contains(t))
            selectedDB.value = confirmedTrips.value!![confirmedTrips.value!!.indexOf(t)]

        return selectedDB
    }

    fun getUserTrips(): LiveData<List<Trip>> {
        return userTrips
    }

    fun getOtherTrips(): LiveData<List<Trip>> {
        return otherTrips
    }

    fun getConfirmedTrips(): LiveData<List<Trip>> {
        return confirmedTrips
    }

    fun getInterestedTrips(): LiveData<List<Trip>> {
        return interestedTrips
    }

    fun saveTrip(t: Trip): Task<Void> {
        return FirestoreRepository().insertTrip(t)
    }
}