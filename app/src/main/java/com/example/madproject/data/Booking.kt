package com.example.madproject.data

data class Booking(
    val id: String = "",
    val tripId: String = "",
    val clientEmail : String = "",
    var confirmed: Boolean = false
)