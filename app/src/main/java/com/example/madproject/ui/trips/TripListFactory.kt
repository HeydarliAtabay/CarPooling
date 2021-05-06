package com.example.madproject.ui.trips

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class TripListFactory: ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripListViewModel::class.java)) {
            return TripListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}