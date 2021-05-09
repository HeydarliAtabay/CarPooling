package com.example.madproject.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class FirestoreRepository() {
    private var firestoreDB = FirebaseFirestore.getInstance()
    private var authEmail: String = "user@gmail.com"

    //SET CURRENT LOGGED USER
    fun setLoggedUser(u: FirebaseUser){
        Log.d("test",u.email!!)
        authEmail = u.email!!
    }

    fun insertTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("users/${authEmail}/createdTrips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("users/${authEmail}/createdTrips")
    }

    fun getUser(): DocumentReference {
        return firestoreDB.collection("users").document(authEmail)
    }

    fun setUser(p: Profile): Task<Void> {
        return firestoreDB.collection("users").document(authEmail).set(p)
    }

    fun controlBooking(t: Trip): Task<QuerySnapshot> {
        val p = "user@gmail.com" //momentaneamente
        return firestoreDB.collection("bookings")
            .whereEqualTo("tripId", t.id)
            .whereEqualTo("clientEmail", p)
            .get()
    }

    fun bookingTransaction(t: Trip): Task<Transaction> {
        val booking = Booking(authEmail, t.id)
        val tripIWantToBook =
            firestoreDB.collection("users/${t.ownerEmail}/createdTrips").document(t.id)
        val newBooking = firestoreDB.collection("bookings").document()
        return firestoreDB.runTransaction { transaction ->
            val snapshotTrip = transaction.get(tripIWantToBook)
            val availableSeats = snapshotTrip.getString("availableSeat")!!.toInt()
            if (availableSeats > 0) {
                val newAvailableSeats = availableSeats - 1
                transaction.update(tripIWantToBook, "availableSeat", newAvailableSeats.toString())
                transaction.set(newBooking, booking)

            } else {
                throw RuntimeException(
                    "No available seats"
                )
            }
        }
    }

    fun getBookings(t: Trip): Query {
        return firestoreDB.collection("bookings").whereEqualTo("tripId", t.id)
    }

    fun getUsersList(): Query {
        return firestoreDB.collection("users").whereNotEqualTo("email", authEmail)
    }

}