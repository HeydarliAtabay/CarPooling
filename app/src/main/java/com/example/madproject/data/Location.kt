package com.example.madproject.data

import android.location.Address

data class Location(
    var latitude: Double? = null,
    var longitude: Double? = null,
    var location: String? = null
) {

    constructor(address: Address): this(address.latitude, address.longitude) {
        val locationComponents = mutableListOf<String>()
        for (i in 0..address.maxAddressLineIndex) {
            locationComponents.add(address.getAddressLine(i))
        }
        this.location = locationComponents.joinToString(separator = ", ")
    }
}