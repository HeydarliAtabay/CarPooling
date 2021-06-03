package com.example.madproject.data

import com.example.madproject.lib.compareDates
import com.google.firebase.firestore.GeoPoint

data class Trip(var id: String = ""): Comparable<Trip> {
    var imageUrl: String = ""
    var from: String = ""
    var to: String = ""
    var departureDate: String = ""
    var departureTime: String = ""
    var duration: String = ""
    var availableSeat: String = ""
    var additionalInfo: String = ""
    var intermediateStops: String = ""
    var price: String = ""
    var ownerEmail: String = ""
    var departureCoordinates: GeoPoint? = null
    var arrivalCoordinates: GeoPoint? = null
    var intermediateCoordinates: ArrayList<GeoPoint> = arrayListOf()

    constructor(
        id: String = "",
        imageUrl: String = "",
        from: String = "",
        to: String = "",
        departureDate: String = "",
        departureTime: String = "",
        duration: String = "",
        availableSeat: String = "",
        additionalInfo: String = "",
        intermediateStops: String = "",
        price: String = "",
        ownerEmail: String = "",
        departureCoo: GeoPoint? = null,
        arrivalCoo: GeoPoint? = null,
        intermediateCoo: ArrayList<GeoPoint> = arrayListOf()

    ) : this(id) {
        this.imageUrl = imageUrl
        this.from = from
        this.to = to
        this.departureDate = departureDate
        this.departureTime = departureTime
        this.duration = duration
        this.availableSeat = availableSeat
        this.additionalInfo = additionalInfo
        this.intermediateStops = intermediateStops
        this.price = price
        this.ownerEmail = ownerEmail
        this.departureCoordinates = departureCoo
        this.arrivalCoordinates = arrivalCoo
        this.intermediateCoordinates = intermediateCoo
    }

    override operator fun compareTo(other: Trip): Int {
        return compareDates(this.departureDate, this.departureTime, other.departureDate, other.departureTime)
    }

}