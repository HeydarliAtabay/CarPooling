package com.example.madproject.ui.profile
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.Profile

class SharedProfileViewModel(): ViewModel() {

    val profile = MutableLiveData(Profile())
    /*
    val profile: MutableLiveData<Profile> by lazy {
        MutableLiveData<Profile>().also {
            loadProfile()
        }
    }*/

    fun select(p: Profile) {
        profile.value = p
    }

    fun getProfile(): LiveData<Profile> {
        return profile
    }

    private fun loadProfile() {
        // When using Firebase, the profile information must be retrieved by this function
    }
}