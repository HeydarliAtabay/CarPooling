package com.example.madproject.ui.yourtrips.interestedusers

import android.util.Log
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
    private var filteredUsers : MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var selectedDBUser: MutableLiveData<Profile> = MutableLiveData(Profile())
    private var selectedBookings: List<Booking> = listOf()
    var selectedLocalUser = Profile()
    var selectedLocalTrip = Trip()

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
                if (filteredUsers.value?.contains(u) == true) retrievedFilteredUsers.add(u)
            }
            allUsers = retrievedUsers
            filteredUsers.value = retrievedFilteredUsers
        })
    }

    fun getUsers(): LiveData<List<Profile>> {
        FirestoreRepository().getProposals(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                filteredUsers.value = null
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
            filteredUsers.value = retrievedUsers
            selectedBookings = retrievedBookings
        })
        return filteredUsers
    }

    fun resetFilteredUsers() {
        filteredUsers.value = listOf()
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