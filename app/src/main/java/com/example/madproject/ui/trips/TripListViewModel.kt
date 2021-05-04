package com.example.madproject.ui.trips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Trip

class TripListViewModel: ViewModel() {

    private val trips = MutableLiveData<List<Trip>> (listOf())

    fun getTrips(): LiveData<List<Trip>> {
        return trips
    }

    fun loadTrip(t: Trip){
        // When using Firebase the List of trips must be retrieved by this function

    }

}