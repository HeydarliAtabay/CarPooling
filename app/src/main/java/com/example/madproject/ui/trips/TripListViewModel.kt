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
    private var selectedDB: MutableLiveData<Trip> = MutableLiveData(Trip())
    var selectedLocal = Trip()
    var currentPhotoPath = ""
    var useDBImage = false

    // Flags used to manage the trip booking
    var comingFromOther = false
    private var bookTheTrip: MutableLiveData<Boolean> = MutableLiveData(false)

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
                val t = doc.toObject(Trip::class.java)
                retrievedTrips.add(t)
                if (t.id == selectedLocal.id) selectedDB.value = t
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

    fun getSelectedDB(t: Trip): LiveData<Trip> {
        if (trips.value == null) return selectedDB
        for (trip in trips.value!!) {
            if (trip.id == t.id) {
                selectedDB.value = trip
                break
            }
        }
        return selectedDB
    }

    fun getBookTheTrip(): LiveData<Boolean> {
        return bookTheTrip
    }

    fun setBookTheTrip(f: Boolean) {
        bookTheTrip.value = f
    }
}