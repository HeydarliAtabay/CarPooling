package com.example.madproject.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

class FirestoreRepository() {
    var firestoreDB = FirebaseFirestore.getInstance()
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented

    fun insertTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("users/user@gmail.com/createdTrips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("users/user@gmail.com/createdTrips")
    }

    fun getUser(): DocumentReference {
        return firestoreDB.collection("users").document("user@gmail.com")
    }

    fun setUser(p: Profile): Task<Void> {
        return firestoreDB.collection("users").document("user@gmail.com").set(p)
    }

    fun controlBooking(t: Trip): Task<QuerySnapshot> {
        val p = "user@gmail.com" //momentaneamente
        return firestoreDB.collection("bookings")
            .whereEqualTo("tripId", t.id)
            .whereEqualTo("clientEmail", p)
            .get()
    }

    fun bookingTransaction(t: Trip): Task<Transaction> {
        val booking = Booking("user@gmail.com", t.id)
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
}