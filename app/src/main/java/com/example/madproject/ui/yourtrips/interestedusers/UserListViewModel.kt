package com.example.madproject.ui.yourtrips.interestedusers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Booking
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.google.firebase.firestore.EventListener

class UserListViewModel: ViewModel() {

    private var allUsers = mutableListOf<Profile>()
    private var proposals : MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var confirmedBook : MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var selectedDBUser: MutableLiveData<Profile> = MutableLiveData(Profile())
    private var selectedBookings: List<Booking> = listOf()
    var selectedLocalUser = Profile()
    var selectedLocalTrip = Trip()

    // Flag to manage the landscape selection of the tab
    var tabBookings = false

    init {
        loadOtherUsers()
    }

    private fun loadOtherUsers() {
        FirestoreRepository().getUsersList().addSnapshotListener(EventListener { value, e ->
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
                if (u.email == selectedLocalUser.email) selectedDBUser.value = u

                // Update the list of users who booked a selected trip
                if (proposals.value?.contains(u) == true) retrievedFilteredUsers.add(u)
            }
            allUsers = retrievedUsers
            proposals.value = retrievedFilteredUsers
        })
    }

    fun getProposals(): LiveData<List<Profile>> {
        FirestoreRepository().getProposals(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                proposals.value = null
                return@EventListener
            }

            val retrievedUsers : MutableList<Profile> = mutableListOf()
            val retrievedBookings : MutableList<Booking> = mutableListOf()
            for (doc in value!!) {
                val booking = doc.toObject(Booking::class.java)
                retrievedBookings.add(booking)
                for (user in allUsers) {
                    if (booking.clientEmail == user.email) retrievedUsers.add(user)
                }
            }
            proposals.value = retrievedUsers
            selectedBookings = retrievedBookings
        })
        return proposals
    }

    fun getConfirmed(): LiveData<List<Profile>> {
        FirestoreRepository().getConfirmed(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                proposals.value = null
                return@EventListener
            }

            val retrievedUsers : MutableList<Profile> = mutableListOf()
            for (doc in value!!) {
                val booking = doc.toObject(Booking::class.java)
                for (user in allUsers) {
                    if (booking.clientEmail == user.email) retrievedUsers.add(user)
                }
            }
            confirmedBook.value = retrievedUsers
        })
        return confirmedBook
    }

    fun resetFilteredUsers() {
        proposals.value = listOf()
    }

    fun getSelectedDB(): LiveData<Profile> {

        if (allUsers.contains(selectedLocalUser))
            selectedDBUser.value = allUsers[allUsers.indexOf(selectedLocalUser)]

        return selectedDBUser
    }

    fun getBooking(u: Profile): Booking {
        for (b in selectedBookings)
            if (b.clientEmail == u.email) return b
        return Booking()
    }

    fun setBookingFlag(u: Profile) {
        for (b in selectedBookings)
            if (b.clientEmail == u.email)
                FirestoreRepository().setProposalFlag(b, selectedLocalTrip, !b.confirmed)
    }

}