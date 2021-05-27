package com.example.madproject.ui.comments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Rating
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration

class RatingsViewModel: ViewModel() {

    // Lists of Live Data which contain the list of ratings
    private val driverRatings: MutableLiveData<List<Rating>> = MutableLiveData()
    private val passengerRatings: MutableLiveData<List<Rating>> = MutableLiveData()

    // Listeners on the collections
    private var driverListener: ListenerRegistration? = null
    private var passengerListener: ListenerRegistration? = null

    // Selected user used to load the ratings of this user
    private var selectedUserEmail: String = ""

    // Flag to determine if the user clicks on driverRatings button (true) or passengerRatings button (false)
    var showDriverRatings = false

    /*
    Function called when it is needed to load the ratings of a different user:
        - Remove (if present) the old listeners
        - Add the new listeners in order to update the lists
     */
    fun selectUser(userEmail: String) {
        if (userEmail == "") return

        selectedUserEmail = userEmail

        driverListener?.remove()
        passengerListener?.remove()

        driverListener = FirestoreRepository().getDriverRating(selectedUserEmail)
            .addSnapshotListener (EventListener { value, error ->
                if (error != null) {
                    driverRatings.value = null
                    return@EventListener
                }
                val retrievedRatings = mutableListOf<Rating>()
                for (v in value!!) {
                    retrievedRatings.add(v.toObject(Rating::class.java))
                }
                driverRatings.value = retrievedRatings
            })

        passengerListener = FirestoreRepository().getPassengerRating(selectedUserEmail)
            .addSnapshotListener (EventListener { value, error ->
                if (error != null) {
                    passengerRatings.value = null
                    return@EventListener
                }
                val retrievedRatings = mutableListOf<Rating>()
                for (v in value!!) {
                    retrievedRatings.add(v.toObject(Rating::class.java))
                }
                passengerRatings.value = retrievedRatings
            })

    }

    fun getPassengerRatings(): LiveData<List<Rating>> {
        return passengerRatings
    }

    fun getDriverRatings(): LiveData<List<Rating>> {
        return driverRatings
    }

}