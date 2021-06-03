package com.example.madproject.ui.yourtrips.interestedusers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Booking
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration

class UserListViewModel: ViewModel() {

    private var allUsers = mutableListOf<Profile>()
    private var proposals: MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var confirmedBook : MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var selectedDBUser: MutableLiveData<Profile> = MutableLiveData(Profile())
    private var selectedBookingsProposals: List<Booking> = listOf()
    private var selectedConfirmedBookings: List<Booking> = listOf()
    var selectedLocalUserEmail = ""

    // Variables used to manage the listeners
    private var listener1: ListenerRegistration? = null
    private var listener2: ListenerRegistration? = null
    private var listener3: ListenerRegistration? = null
    private var listener4: ListenerRegistration? = null

    // Manage the selected Trip update
    var selectedLocalTrip = Trip()
    private var selectedTrip = MutableLiveData<Trip>()

    // Flag to manage the landscape selection of the tab
    var tabBookings = false

    // Flag to manage the booking confirmation dialog
    var confirmBookingDialogOpened = false
    var changedOrientation = false

    // Data used to manage the rating dialog restore state for the rating dialog
    var userEmailInDialog = ""
    var tripInDialog = ""
    var rating = 0.0F
    var comment = ""

    init {
        loadOtherUsers()
    }

    private fun loadOtherUsers() {
        listener1 = FirestoreRepository().getUsersList().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                allUsers = mutableListOf()
                return@EventListener
            }

            val retrievedUsers : MutableList<Profile> = mutableListOf()
            val retrievedFilteredUsers : MutableList<Profile> = mutableListOf()
            for (doc in value!!) {
                val u = doc.toObject(Profile::class.java)

                retrievedUsers.add(u)

                // Update the selected user in Show Profile
                if (u.email == selectedLocalUserEmail) selectedDBUser.value = u

                // Update the list of users who booked a selected trip
                for (p in proposals.value!!)
                    if (p.email == u.email) retrievedFilteredUsers.add(u)
            }

            allUsers = retrievedUsers
            proposals.value = retrievedFilteredUsers
        })
    }

    fun getProposals(): LiveData<List<Profile>> {
        listener2 = FirestoreRepository().getProposals(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                proposals.value = null
                return@EventListener
            }

            val retrievedMap: MutableMap<Profile, Booking> = mutableMapOf()
            val retrievedUsers : MutableList<Profile> = mutableListOf()
            val retrievedBookings : MutableList<Booking> = mutableListOf()
            for (doc in value!!) {
                val booking = doc.toObject(Booking::class.java)
                retrievedBookings.add(booking)
                var u = Profile()
                for (user in allUsers) {
                    if (booking.clientEmail == user.email) {
                        u = user
                        retrievedUsers.add(user)
                    }
                }
                retrievedMap[u] = booking
            }
            proposals.value = retrievedUsers
            selectedBookingsProposals = retrievedBookings
        })
        return proposals
    }

    fun getConfirmed(): LiveData<List<Profile>> {
        listener3 = FirestoreRepository().getConfirmed(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                proposals.value = null
                return@EventListener
            }

            val retrievedUsers : MutableList<Profile> = mutableListOf()
            val retrievedBookings : MutableList<Booking> = mutableListOf()
            for (doc in value!!) {
                val booking = doc.toObject(Booking::class.java)
                retrievedBookings.add(booking)
                // Insert only the passengers who did not receive a rating
                if (!booking.passengerRated) {
                    for (user in allUsers) {
                        if (booking.clientEmail == user.email) retrievedUsers.add(user)
                    }
                }
            }
            confirmedBook.value = retrievedUsers
            selectedConfirmedBookings = retrievedBookings
        })
        return confirmedBook
    }

    fun resetFilteredUsers() {
        proposals.value = listOf()
    }

    fun getSelectedDB(): LiveData<Profile> {

        for (u in allUsers) {
            if (u.email == selectedLocalUserEmail)
                selectedDBUser.value = u
        }
        return selectedDBUser
    }

    fun getBooking(u: Profile): Booking {
        for (b in selectedBookingsProposals)
            if (b.clientEmail == u.email) return b
        for (b in selectedConfirmedBookings)
            if (b.clientEmail == u.email) return b
        return Booking()
    }

    fun setBookingFlag(u: Profile): Task<Void> {
        val b = getBooking(u)
        return FirestoreRepository().setProposalFlag(b, selectedLocalTrip, !b.confirmed)
    }

    fun getDBTrip(): LiveData<Trip> {
        selectedTrip.value = selectedLocalTrip
        listener4 = FirestoreRepository().getTrip(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                selectedTrip.value = Trip()
                return@EventListener
            }

            selectedTrip.value = value?.toObject(Trip::class.java)
        })
        return selectedTrip
    }

    fun clearListeners() {
        listener1?.remove()
        listener2?.remove()
        listener3?.remove()
        listener4?.remove()
    }
}