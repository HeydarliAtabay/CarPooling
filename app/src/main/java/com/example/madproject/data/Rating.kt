package com.example.madproject.data

data class Rating(
    val tripId: String = "",
    val rating: Float = 0.0F,
    val comment: String = "",
    var nickName: String = ""
    )