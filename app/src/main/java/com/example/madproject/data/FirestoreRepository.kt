package com.example.madproject.data

import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    var firestoreDB = FirebaseFirestore.getInstance()
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented

    fun addTrip(t: Trip): Task<Void> {
        val id = firestoreDB.collection("Trips").document().id
        t.id = id
        return firestoreDB.collection("Trips").document(id).set(t)
    }

    fun updateTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("Trips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("Trips")
    }
}