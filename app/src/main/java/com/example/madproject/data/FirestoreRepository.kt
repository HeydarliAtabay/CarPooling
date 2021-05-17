package com.example.madproject.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage

class FirestoreRepository() {
    private var fireStoreDB = FirebaseFirestore.getInstance()

    companion object{
        lateinit var auth: FirebaseUser
    }

    fun insertTrip(t: Trip): Task<Transaction> {

        return fireStoreDB.runTransaction { transaction ->

            transaction.set(
                fireStoreDB.collection("trips").document(t.id),
                mapOf("tripId" to t.id)
            )

            transaction.set(
                fireStoreDB.collection("users/${auth.email}/createdTrips").document(t.id),
                t
            )
        }
    }

    fun deleteTrip(t: Trip): Task<Transaction> {
        return fireStoreDB.runTransaction { transaction ->
            transaction.delete(fireStoreDB.collection("users/${auth.email}/createdTrips").document(t.id))
            transaction.delete(fireStoreDB.collection("trips").document(t.id))
        }
    }

    fun getTrips(): CollectionReference {
        return fireStoreDB.collection("users/${auth.email}/createdTrips")
    }

    fun getUser(): DocumentReference {
        return fireStoreDB.collection("users").document(auth.email!!)
    }

    fun setUser(p: Profile): Task<Void> {
        return fireStoreDB.collection("users").document(auth.email!!).set(p)
    }

    fun controlBooking(t: Trip): Task<QuerySnapshot> {
        val p = auth.email
        return fireStoreDB.collection("trips/${t.id}/proposals")
            .whereEqualTo("clientEmail", p)
            .get()
    }

    fun proposeBooking(t: Trip): Task<Void> {
        val newBookingId = fireStoreDB.collection("trips/${t.id}/proposals").document().id
        return fireStoreDB.collection("trips/${t.id}/proposals")
            .document(newBookingId)
            .set(Booking(id = newBookingId,clientEmail = auth.email!!))
    }

    fun bookingTransaction(t: Trip): Task<Transaction> {
        val booking = Booking(auth.email!!)
        val tripIWantToBook =
            fireStoreDB.collection("users/${t.ownerEmail}/createdTrips").document(t.id)
        val newBooking = fireStoreDB.collection("bookings").document()
        return fireStoreDB.runTransaction { transaction ->
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

    fun getProposals(t: Trip): Query {
        return fireStoreDB.collection("trips/${t.id}/proposals")
    }

    fun getConfirmed(t: Trip): Query {
        return fireStoreDB.collection("trips/${t.id}/confirmedBookings")
    }

    fun setProposalFlag(b: Booking, t: Trip, flag: Boolean): Task<Void> {
        return fireStoreDB.collection("trips/${t.id}/proposals").document(b.id)
            .update("confirmed", flag)
    }

    fun getUsersList(): Query {
        return fireStoreDB.collection("users")
            .whereNotEqualTo("email", auth.email!!)
    }
}