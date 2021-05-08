package com.example.madproject.data

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*

class FirestoreRepository() {
    var firestoreDB = FirebaseFirestore.getInstance()
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented
    var user = Profile(email = "user@gmail.com")

    fun insertTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("users/${user.email}/createdTrips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("users/${user.email}/createdTrips")
    }

    fun getUser(): DocumentReference {
        return firestoreDB.collection("users").document(user.email)
    }

    fun setUser(p: Profile): Task<Void> {
        return firestoreDB.collection("users").document(user.email).set(p)
    }

    fun controlBooking(t: Trip): Task<QuerySnapshot> {
        val p = "user@gmail.com" //momentaneamente
        return firestoreDB.collection("bookings")
            .whereEqualTo("tripId", t.id)
            .whereEqualTo("clientEmail", p)
            .get()
    }

    fun bookingTransaction(t: Trip): Task<Transaction> {
        val booking = Booking(user.email, t.id)
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
        return firestoreDB.collection("users").whereNotEqualTo("email", user.email)
    }

}