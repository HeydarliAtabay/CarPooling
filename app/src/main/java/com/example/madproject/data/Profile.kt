package com.example.madproject.data

data class Profile(val fullName: String = "",
                   val nickName: String = "",
                   val dateOfBirth: String = "",
                   val email: String = "",
                   val phoneNumber: String = "",
                   val location: String = "",
                   val currentPhotoPath: String? = ""
                   ) {}