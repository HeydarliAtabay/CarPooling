package com.example.madproject.ui.yourtrips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.Transaction

class TripListViewModel: ViewModel() {


    private val userTrips: MutableLiveData<List<Trip>>
        by lazy { MutableLiveData<List<Trip>>().also { loadUserTrips() } }

    private val otherTrips: MutableLiveData<List<Trip>>
        by lazy { MutableLiveData<List<Trip>>().also { loadOtherTrips() } }

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

    private fun loadUserTrips() {
        FirestoreRepository().getTrips().addSnapshotListener(EventListener { value, e ->
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

    private fun loadOtherTrips() {
        // Get the list of other users
        FirestoreRepository().getUsersList()
            .addSnapshotListener(EventListener { value1, error1 ->
                if (error1!= null) {
                    return@EventListener
                }
                val retrievedTrips : MutableList<Trip> = mutableListOf()
                for (user in value1!!) {
                    // For each user get the created trips
                    user.reference.collection("createdTrips")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                otherTrips.value = null
                            } else {
                                val updatedList: MutableList<Trip> = mutableListOf()
                                for (trip in value!!) {
                                    val t = trip.toObject(Trip::class.java)
                                    if (t.availableSeat.toInt() == 0) {
                                        if (t == selectedDB.value) selectedDB.value = null
                                        if (retrievedTrips.contains(t)) { retrievedTrips.remove(t) }
                                    } else {
                                        if (t == selectedLocal) selectedDB.value = t
                                        // Check if this trip must be updated instead of added
                                        if (retrievedTrips.contains(t)) retrievedTrips[retrievedTrips.indexOf(
                                            t
                                        )] = t
                                        else retrievedTrips.add(t)
                                        updatedList.add(t)
                                    }
                                }
                                // Check if the current listener has been triggered by the delete of a trip
                                val toRemove = findDeleted(updatedList, retrievedTrips)
                                if (toRemove.id != "-1") {
                                    retrievedTrips.remove(toRemove)
                                    if (selectedDB.value == toRemove)
                                        selectedDB.value = null
                                }

                                otherTrips.value = retrievedTrips
                            }
                        }
                }
            })
    }

    fun getUserTrips(): LiveData<List<Trip>> {
        return userTrips
    }

    fun getOtherTrips(): LiveData<List<Trip>> {
        return otherTrips
    }

    private fun findDeleted(upd: List<Trip>, comp: List<Trip>): Trip {

        // If the size of upd and the subList of comp belonging to ownerEmail is the same it means that
        // no trip was deleted
        if (upd.isEmpty()) return Trip(id = "-1")
        val filtered = comp.filter { t -> t.ownerEmail == upd[0].ownerEmail }
        if (upd.size == filtered.size) return Trip(id = "-1")

        for (t1 in filtered) {
            // If upd does not contains t1, it means that id was deleted
            if (!upd.contains(t1))
                return t1
        }

        return Trip(id = "-1")
    }

    fun saveTrip(t: Trip): Task<Transaction> {
        return FirestoreRepository().insertTrip(t)
    }

    fun getSelectedDB(t: Trip): LiveData<Trip> {
        // Check whether the selected trip is contained in the userTrips
        if (userTrips.value != null) {
            for (trip in userTrips.value!!) {
                if (trip.id == t.id) {
                    selectedDB.value = trip
                    return selectedDB
                }
            }
        }

        // Check whether the selected trip is contained in the otherTrips
        if (otherTrips.value == null) return selectedDB
        for (trip in otherTrips.value!!) {
            if (trip.id == t.id) {
                selectedDB.value = trip
                return selectedDB
            }
        }
        return selectedDB
    }
}