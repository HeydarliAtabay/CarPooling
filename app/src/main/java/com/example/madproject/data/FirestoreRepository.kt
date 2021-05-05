package com.example.madproject.data

import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File

class FirestoreRepository {
    var firestoreDB = FirebaseFirestore.getInstance()
    var storage = FirebaseStorage.getInstance()
    var storageRef = storage.reference
    //var user = FirebaseAuth.getInstance().currentUser  Use it when the auth is implemented

    fun addTrip(t: Trip): Task<Void> {
        val id = firestoreDB.collection("users/user@gmail.com/createdTrips").document().id
        t.id = id
        return firestoreDB.collection("users/user@gmail.com/createdTrips").document(id).set(t)
    }

    fun updateTrip(t: Trip): Task<Void> {
        return firestoreDB.collection("users/user@gmail.com/createdTrips").document(t.id).set(t)
    }

    fun getTrips(): CollectionReference {
        return firestoreDB.collection("users/user@gmail.com/createdTrips")
    }

    fun getUser() : DocumentReference {
        return firestoreDB.collection("users").document("user@gmail.com")
    }

    fun setUser(p : Profile) : Task<Void> {
        return firestoreDB.collection("users").document("user@gmail.com").set(p)
    }

    fun setUserImage(profile: Profile): UploadTask {
        val file = Uri.fromFile(File(profile.currentPhotoPath!!))
        val imageRef = storageRef.child("${profile.email}/profileImage.jpg")
        return imageRef.putFile(file)
    }

    fun getUserImage(file: File?): Pair<File, FileDownloadTask> {
        val imageRef = storageRef.child("user@gmail.com/profileImage.jpg")
        File.createTempFile("JPEG_", ".jpg", file)
            .apply {
                Log.d("test","${this.absolutePath}")
                return Pair<File,FileDownloadTask>(this,imageRef.getFile(this))
            }

    }
}