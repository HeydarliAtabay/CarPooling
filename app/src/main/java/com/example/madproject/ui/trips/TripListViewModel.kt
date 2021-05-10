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

    private var userTrips : MutableLiveData<List<Trip>> = MutableLiveData()
    private var otherTrips : MutableLiveData<List<Trip>> = MutableLiveData()
    var selected = Trip()
    var currentPhotoPath = ""
    var useDBImage = false
    var comingFromOther = false
    var currentUser = "user3@gmail.com"
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented

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
                retrievedTrips.add(doc.toObject(Trip::class.java))
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
                                retrievedTrips.add(trip.toObject(Trip::class.java))
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
                                retrievedTrips.add(trip.toObject(Trip::class.java))
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
}