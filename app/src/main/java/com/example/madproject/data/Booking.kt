package com.example.madproject.data

data class Booking(var id: String = "") {
    var tripId: String = ""
    var clientEmail: String = ""
    var confirmed: Boolean = false
    var driverRated: Boolean = false
    var passengerRated: Boolean = false

    constructor(
        id: String = "",
        tripId: String = "",
        clientEmail: String = "",
        confirmed: Boolean = false,
        driverRated: Boolean = false,
        passengerRated: Boolean = false
    ): this(id) {
        this.tripId = tripId
        this.clientEmail = clientEmail
        this.confirmed = confirmed
        this.driverRated = driverRated
        this.passengerRated = passengerRated
    }

    override fun toString(): String {
        return "id: $id, trip: $tripId, client: $clientEmail, confirmed: $confirmed, driverRated: $driverRated, passengerRated: $passengerRated"
    }
}