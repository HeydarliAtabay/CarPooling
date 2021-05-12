package com.example.madproject.ui.othertrips

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Filters

class FilterViewModel: ViewModel() {

    private var filter = MutableLiveData(Filters())

    var dialogOpened = false
    var changedOrientation = false

    var temporalFilters = Filters()

    fun getFilter(): LiveData<Filters> {
        return filter
    }

    fun setFilter(f: Filters) {
        filter.value = f
    }

}