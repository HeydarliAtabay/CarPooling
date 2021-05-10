package com.example.madproject.ui.trips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.firestore.ktx.toObject

class TripListViewModel: ViewModel() {

    private var trips : MutableLiveData<List<Trip>> = MutableLiveData()
    private var selectedDB: MutableLiveData<Trip> = MutableLiveData(Trip())
    var selectedLocal = Trip()
    private var userTrips : MutableLiveData<List<Trip>> = MutableLiveData()
    private var otherTrips : MutableLiveData<List<Trip>> = MutableLiveData()
    var currentPhotoPath = ""
    var useDBImage = false

    // Flags used to manage the trip booking
    var comingFromOther = false
    private var bookTheTrip: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        loadUserTrips()
        loadOtherTrips()
    }

    private fun loadUserTrips() {
        FirestoreRepository().getTrips().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                userTrips.value = null
                return@EventListener
            }

            val retrievedTrips : MutableList<Trip> = mutableListOf()
            for (doc in value!!) {
                val t = doc.toObject(Trip::class.java)
                retrievedTrips.add(t)
                if (t.id == selectedLocal.id) selectedDB.value = t
            }
            userTrips.value = retrievedTrips
        })
    }

    private fun loadOtherTrips() {
        val users = FirestoreRepository().getUsers()
        val retrievedTrips : MutableList<Trip> = mutableListOf()

        users.whereGreaterThan("email",currentUser)
            .addSnapshotListener(EventListener { value, error ->
                if (error != null) {
                otherTrips.value = null
                return@EventListener
                }

                for (user in value!!) {
                    user.reference.collection("createdTrips")
                        ?.addSnapshotListener(EventListener { value, error ->
                            if (error != null) {
                                otherTrips.value = null
                                return@EventListener
                            }
                            for (trip in value!!) {
                                val t = trip.toObject(Trip::class.java)
                                if (t.id == selectedLocal.id) selectedDB.value = t
                                retrievedTrips.add(t)
                            }
                        })
                }

            })

        users.whereLessThan("email",currentUser)
            .addSnapshotListener(EventListener { value, error ->
                if (error != null) {
                    otherTrips.value = null
                    return@EventListener
                }

                for (user in value!!) {
                    user.reference.collection("createdTrips")
                        ?.addSnapshotListener(EventListener { value, error ->
                            if (error != null) {
                                otherTrips.value = null
                                return@EventListener
                            }
                            for (trip in value!!) {
                                val t = trip.toObject(Trip::class.java)
                                if (t.id == selectedLocal.id) selectedDB.value = t
                                retrievedTrips.add(t)
                            }
                        })
                }

            })
        otherTrips.value = retrievedTrips
    }

    fun getUserTrips(): LiveData<List<Trip>> {
        return userTrips
    }

    fun getOtherTrips(): LiveData<List<Trip>> {
        //Log.d("Loriente", otherTrips.value.toString())
        return otherTrips
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