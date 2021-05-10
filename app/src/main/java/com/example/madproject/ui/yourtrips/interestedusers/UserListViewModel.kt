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
    private var filteredUsers : MutableLiveData<List<Profile>> = MutableLiveData(listOf())
    private var selectedDBUser: MutableLiveData<Profile> = MutableLiveData(Profile())
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
                if (filteredUsers.value != null) {
                    for (user in filteredUsers.value!!) {
                        if (u.email == user.email) {
                            retrievedFilteredUsers.add(u)
                            break
                        }
                    }
                }
            }
            allUsers = retrievedUsers
            filteredUsers.value = retrievedFilteredUsers
        })
    }

    fun getUsers(): LiveData<List<Profile>> {
        FirestoreRepository().getBookings(selectedLocalTrip).addSnapshotListener(EventListener { value, e ->

            if (e != null) {
                filteredUsers.value = null
                return@EventListener
            }

            val retrievedUsers : MutableList<Profile> = mutableListOf()
            for (doc in value!!) {
                val booking = doc.toObject(Booking::class.java)
                for (user in allUsers) {
                    if (booking.clientEmail == user.email) retrievedUsers.add(user)
                }
            }
            filteredUsers.value = retrievedUsers
        })
        return filteredUsers
    }

    fun resetFilteredUsers() {
        filteredUsers.value = listOf()
    }

    fun getSelectedDB(): LiveData<Profile> {
        for (user in allUsers) {
            if (user.email == selectedLocalUser.email) {
                selectedDBUser.value = user
                break
            }
        }
        return selectedDBUser
    }

}