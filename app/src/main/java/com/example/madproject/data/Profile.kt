package com.example.madproject.data

data class Profile(
    val email: String = "",
    val fullName: String = "",
    val nickName: String = "",
    val dateOfBirth: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    var imageUrl: String? = ""
    )