package com.example.madproject.ui.othertrips

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Filters

class FilterViewModel: ViewModel() {

    private var filter = MutableLiveData(Filters())

    fun getFilter(): LiveData<Filters> {
        return filter
    }

    fun setFilter(f: Filters) {
        filter.value = f
    }

}