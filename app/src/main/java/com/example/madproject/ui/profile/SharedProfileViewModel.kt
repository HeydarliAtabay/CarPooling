package com.example.madproject.ui.profile
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Profile

class SharedProfileViewModel(): ViewModel() {
    val profile = MutableLiveData(Profile())

    fun select(p: Profile) {
        profile.value = p
    }

}