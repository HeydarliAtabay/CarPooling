package com.example.madproject.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class ProfileViewModel: ViewModel() {

    private var profile: MutableLiveData<Profile> = MutableLiveData(Profile())
    var localProfile = Profile()
    var currentPhotoPath = ""
    var useDBImage = false
    var comingFromPrivacy = false
    var needRegistration = false

    // This flag is used to maintain the state of the logout dialog
    var logoutDialogOpened = false
    var changedOrientation = false

    var bigPhotoPath = ""

    // Variable to manage the orientation of the screen in the async tasks
    var orientation = -1

    init {
        loadProfile()
    }

    private fun loadProfile() {
        FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                profile.value = null
                return@EventListener
            }

            profile.value = value?.toObject(Profile::class.java)
        })
    }

    fun getDBUser() : LiveData<Profile>{
        return profile
    }

    fun setDBUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }
}