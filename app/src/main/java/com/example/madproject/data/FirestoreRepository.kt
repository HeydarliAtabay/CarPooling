package com.example.madproject.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreRepository {
    var firestoreDB = FirebaseFirestore.getInstance()
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented

    fun addTrip(t: Trip): Task<Void> {
        val id = firestoreDB.collection("users/user@gmail.com/createdTrips").document().id
        t.id = id
        return firestoreDB.collection("users/user@gmail.com/createdTrips").document(id).set(t)
    }

    fun updateTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("users/user@gmail.com/createdTrips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("users/user@gmail.com/createdTrips")
    }

    fun getUser() : DocumentReference {
        return firestoreDB.collection("users").document("user@gmail.com")
    }

    fun setUser(p : Profile) : Task<Void> {
        return firestoreDB.collection("users").document("user@gmail.com").set(p)
    }
}