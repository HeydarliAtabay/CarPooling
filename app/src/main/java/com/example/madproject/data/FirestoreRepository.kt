package com.example.madproject.data

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*

class FirestoreRepository {
    private var fireStoreDB = FirebaseFirestore.getInstance()

    companion object{
        // Current Authenticated user
        lateinit var currentUser: FirebaseUser
    }

    /*
    Function to insert the trip "t" inside Firebase
     */
    fun insertTrip(t: Trip): Task<Void> {

        return fireStoreDB.collection("trips").document(t.id).set(t)
    }

    /*
    Function to delete the trip "t" from Firebase
     */
    fun deleteTrip(t: Trip): Task<Void> {
        return fireStoreDB.collection("trips").document(t.id).delete()
    }

    /*
    Function to get the collection of trips created by the current user
     */
    fun getUserTrips(): Query {
        return fireStoreDB.collection("trips").whereEqualTo("ownerEmail", currentUser.email)
    }

    /*
    Function to get the document of the trip "t"
     */
    fun getTrip(t: Trip): DocumentReference {
        return fireStoreDB.collection("trips").document(t.id)
    }

    /*
    Function to get every trips from the "trips" collection
     */
    fun getAllTrips(): Query {
        return fireStoreDB.collection("trips").whereNotEqualTo("ownerEmail", currentUser.email)
    }

    /*
    Function to get the document of the current user
     */
    fun getUser(): DocumentReference {
        return fireStoreDB.collection("users").document(currentUser.email!!)
    }

    /*
    Function to modify the document of the current user
     */
    fun setUser(p: Profile): Task<Void> {
        return fireStoreDB.collection("users").document(currentUser.email!!).set(p)
    }

    /*
    Function to get the proposals of of the selected trip "t"
     */
    fun controlProposals(t: Trip): Task<QuerySnapshot> {
        return fireStoreDB.collection("trips/${t.id}/proposals")
            .whereEqualTo("clientEmail", currentUser.email)
            .get()
    }

    /*
    Function to get the confirmed bookings of of the selected trip "t"
     */
    fun controlBookings(t: Trip): Task<QuerySnapshot> {
        return fireStoreDB.collection("trips/${t.id}/confirmedBookings")
            .whereEqualTo("clientEmail", currentUser.email)
            .get()
    }

    /*
    Function to insert a new proposal inside the trip "t"
     */
    fun proposeBooking(t: Trip): Task<Void> {
        val newBookingId = fireStoreDB.collection("trips/${t.id}/proposals").document().id

        return fireStoreDB.collection("trips/${t.id}/proposals").document(newBookingId)
                .set(Booking(id = newBookingId, tripId = t.id,clientEmail = currentUser.email!!))
    }

    /*
    This transaction will confirm the selected proposals ("bookingsConfirmed") in the following way:
        - check whether the trip has enough available seats
        - if yes it decreases the number of seats and move the bookings to created Trips
        - if there are no more available seats the remaining proposals are deleted
     */
    fun bookingTransaction(t: Trip, bookingsConfirmed: List<Booking>, proposals: List<Booking>): Task<Transaction> {

        val tripIWantToBook = fireStoreDB.collection("trips").document(t.id)

        return fireStoreDB.runTransaction { transaction ->

            val snapshotTrip = transaction.get(tripIWantToBook)
            val availableSeats = snapshotTrip.getString("availableSeat")!!.toInt()
            if (availableSeats >= bookingsConfirmed.size) {
                val newAvailableSeats = availableSeats - bookingsConfirmed.size
                for (b in bookingsConfirmed) {
                    b.confirmed = true
                    transaction.set(
                        fireStoreDB.collection("trips/${t.id}/confirmedBookings").document(b.id),
                        b
                    )
                    transaction.delete(
                        fireStoreDB.collection("trips/${t.id}/proposals").document(b.id)
                    )
                }
                if (newAvailableSeats == 0) {
                    for (b in proposals) {
                        transaction.delete(
                            fireStoreDB.collection("trips/${t.id}/proposals").document(b.id)
                        )
                    }
                }

                transaction.update(tripIWantToBook, "availableSeat", newAvailableSeats.toString())
            } else {
                throw RuntimeException(
                    "Problem in the confirmation, only $availableSeats available seats!"
                )
            }
        }
    }

    /*
    Function to get the list of booking proposals of the trip "t"
     */
    fun getProposals(t: Trip): CollectionReference {
        return fireStoreDB.collection("trips/${t.id}/proposals")
    }

    /*
    Function to get the list of confirmed bookings of the trip "t"
     */
    fun getConfirmed(t: Trip): CollectionReference {
        return fireStoreDB.collection("trips/${t.id}/confirmedBookings")
    }

    /*
    Function to set the confirmed flag on the booking "b"
     */
    fun setProposalFlag(b: Booking, t: Trip, flag: Boolean): Task<Void> {
        return fireStoreDB.collection("trips/${t.id}/proposals").document(b.id)
            .update("confirmed", flag)
    }

    /*
    Function to get the list of other users
     */
    fun getUsersList(): Query {
        return fireStoreDB.collection("users")
            .whereNotEqualTo("email", currentUser.email!!)
    }

    /*
    Function to start the transaction which inserts a new rating, and deletes the relative booking.
    This transaction will:
        - insert the new rating inside the proper collection (looking for the current user's nickName)
        - delete the booking, so it is not anymore shown (It is ok because the trip is completed)
    */
    fun insertRating(r: Rating, userEmail: String, passenger: Boolean, b: Booking): Task<Transaction> {
        val collection = if (passenger) "passengerRatings" else "driverRatings"
        val ratingCollectionRef = fireStoreDB.collection("users/$userEmail/$collection")

        return fireStoreDB.runTransaction { transaction ->
            r.nickName = transaction.get(fireStoreDB.collection("users")
                .document(currentUser.email!!))
                .getString("nickName") ?: ""

            val newId = ratingCollectionRef.document().id

            // set the new rating
            transaction.set(
                ratingCollectionRef.document(newId),
                r
            )

            // delete the corresponding booking
            transaction.delete(
                fireStoreDB.collection("trips/${r.tripId}/confirmedBookings").document(b.id)
            )
        }
    }

    /*
    Function to get the collection of driver ratings of the selected user having "userEmail" as email
     */
    fun getDriverRating(userEmail: String): CollectionReference {
        return fireStoreDB.collection("users/$userEmail/driverRatings")
    }

    /*
    Function to get the collection of passenger ratings of the selected user having "userEmail" as email
     */
    fun getPassengerRating(userEmail: String): CollectionReference {
        return fireStoreDB.collection("users/$userEmail/passengerRatings")
    }

}