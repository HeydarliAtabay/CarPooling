package com.example.madproject.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.EventListener

class ProfileViewModel: ViewModel() {

    private val yourProfile: MutableLiveData<Profile>
        by lazy { MutableLiveData(Profile()).also { loadProfile() } }

    var localProfile = Profile()

    // Variables to manage the photo inside trip edit
    var currentPhotoPath = ""
    var bigPhotoPath = ""
    var useDBImage = false

    // Flags to know from which path the various fragments are called
    var comingFromPrivacy = false
    var needRegistration = false

    // This flag is used to maintain the state of the logout dialog
    var logoutDialogOpened = false
    var changedOrientation = false

    // Variable to manage the orientation of the screen in the async tasks
    var orientation = -1

    private fun loadProfile() {
        FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                yourProfile.value = null
                return@EventListener
            }

            yourProfile.value = value?.toObject(Profile::class.java)
        })
    }

    fun getDBUser() : LiveData<Profile>{
        return yourProfile
    }

    fun setDBUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }
}