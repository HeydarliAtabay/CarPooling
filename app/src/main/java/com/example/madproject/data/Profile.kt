package com.example.madproject.data

data class Profile(val email: String = "") {
    var fullName: String = ""
    var nickName: String = ""
    var dateOfBirth: String = ""
    var phoneNumber: String = ""
    var location: String = ""
    var imageUrl: String? = ""

    constructor(
        email: String = "",
        fullName: String = "",
        nickName: String = "",
        dateOfBirth: String = "",
        phoneNumber: String = "",
        location: String = "",
        imageUrl: String? = ""
    ): this(email) {
        this.fullName = fullName
        this.nickName = nickName
        this.dateOfBirth = dateOfBirth
        this.phoneNumber = phoneNumber
        this.location = location
        this.imageUrl = imageUrl
    }
}