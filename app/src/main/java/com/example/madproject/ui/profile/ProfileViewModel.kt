package com.example.madproject.ui.profile
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class ProfileViewModel(): ViewModel() {

    private var profile: MutableLiveData<Profile> = MutableLiveData(Profile())

    init {
        loadUser()
    }

    private fun loadUser() {
        FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                profile.value = null
                return@EventListener
            }

            profile.value = value?.toObject(Profile::class.java)
        })
    }

    fun getUser(): LiveData<Profile> {
        return profile
    }

    fun setUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }
}