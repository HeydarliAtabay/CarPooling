package com.example.madproject.ui.trips

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Trip

class SharedTripViewModel: ViewModel() {
    val selected = MutableLiveData<Trip>()

    fun select(trip: Trip) {
        selected.value = trip
    }
}