package com.example.madproject.ui.profile
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.UploadTask
import java.io.File

class SharedProfileViewModel(private val file: File?): ViewModel() {

    /*
    private val profile: MutableLiveData<Profile> by lazy {
        MutableLiveData<Profile>().also {
            loadUser()
        }
    }*/

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
            Log.d("test", "loadUser")
            val p = value?.toObject(Profile::class.java)

            val filename = "${file?.absolutePath}/${p!!.currentPhotoPath}"
            val photo = File(filename)
            if(!photo.exists()){

                val pair = FirestoreRepository().getUserImage(file)
                //first -> File dov'è salvata l'immagine
                //second -> FileDownloadTask
                pair.second
                    .addOnSuccessListener {
                        //p.currentPhotoPath = pair.first.absolutePath
                        profile.value = p
                    }
                    .addOnFailureListener{
                        p.currentPhotoPath = ""
                        profile.value = p
                    }
            } else {
                profile.value = p
            }
        })
    }

    fun getUser(): LiveData<Profile> {
        Log.d("test", "getUser")
        return profile
    }

    /*
    val profile : MutableLiveData<Profile> = MutableLiveData(Profile())

    fun getUser(file: File?): LiveData<Profile> {
        FirestoreRepository().getUser().addSnapshotListener(EventListener { value, e ->
            if (e != null) {
                profile.value = null
                return@EventListener
            }

            val p = value?.toObject(Profile::class.java)

            val filename = "${file?.absolutePath}/${p!!.currentPhotoPath}"
            val photo = File(filename)
            if(!photo.exists()){

                val pair = FirestoreRepository().getUserImage(file)
                //first -> File dov'è salvata l'immagine
                //second -> FileDownloadTask
                pair.second
                    .addOnSuccessListener {
                        //p.currentPhotoPath = pair.first.absolutePath
                        profile.value = p
                    }
                    .addOnFailureListener{
                        p.currentPhotoPath = ""
                        profile.value = p
                    }
            } else {
                profile.value = p
            }
        })

        return profile
    }*/

    fun setUser(p:Profile) : Task<Void> {
        return FirestoreRepository().setUser(p)
    }

    fun setUserImage(profile: Profile, file: File?) : UploadTask {
        Log.d("test", "set")
        return FirestoreRepository().setUserImage(profile, file)
    }

}