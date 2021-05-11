package com.example.madproject.ui.yourtrips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class TripListViewModel: ViewModel() {

    private var userTrips: MutableLiveData<List<Trip>> = MutableLiveData()
    private var otherTrips: MutableLiveData<List<Trip>> = MutableLiveData()
    private var selectedDB: MutableLiveData<Trip> = MutableLiveData(Trip())

    var selectedLocal = Trip()
    var currentPhotoPath = ""
    var useDBImage = false

    // Flags to manage the Dialog when the orientation changes
    var dialogOpened = false
    var changedOrientation = false

    // Flags used to manage the trip booking
    var comingFromOther = false

    fun getUserTrips(): LiveData<List<Trip>> {
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
        return userTrips
    }

    fun getOtherTrips(): LiveData<List<Trip>> {
        FirestoreRepository().getUsersList().get()
            .addOnSuccessListener {
                val retrievedTrips : MutableList<Trip> = mutableListOf()
                for (user in it!!) {
                    user.reference.collection("createdTrips")
                        .addSnapshotListener(EventListener { value, error ->
                            if (error != null) {
                                otherTrips.value = null
                                return@EventListener
                            }

                            for (trip in value!!) {
                                val t = trip.toObject(Trip::class.java)
                                if (t.id == selectedLocal.id) selectedDB.value = t
                                val toUpdate = findUpdate(t,retrievedTrips)
                                if (toUpdate.id != "-1")
                                    retrievedTrips[retrievedTrips.indexOf(toUpdate)] = t
                                else
                                    retrievedTrips.add(t)
                            }
                            otherTrips.value = retrievedTrips
                        })
                }
            }
        return otherTrips
    }

    private fun findUpdate(t: Trip, trips: List<Trip>): Trip {
        for (trip in trips) {
            if (t.id == trip.id) return trip
        }
        return Trip(id = "-1")
    }

    fun saveTrip(t: Trip): Task<Void> {
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