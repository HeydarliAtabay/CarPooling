package com.example.madproject.ui.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class TripListViewModel: ViewModel() {

    private var trips : MutableLiveData<List<Trip>> = MutableLiveData()
    var selected = Trip()
    var currentPhotoPath = ""
    var useDBImage = false
    var comingFromOther = false

    init {
        loadTrips()
    }

    private fun loadTrips() {
        FirestoreRepository().getTrips().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                trips.value = null
                return@EventListener
            }

            val retrievedTrips : MutableList<Trip> = mutableListOf()
            for (doc in value!!) {
                retrievedTrips.add(doc.toObject(Trip::class.java))
            }
            trips.value = retrievedTrips
        })
    }

    fun getTrips(): LiveData<List<Trip>> {
        return trips
    }

    fun saveTrip(t: Trip): Task<Void> {
        return FirestoreRepository().insertTrip(t)
    }
}