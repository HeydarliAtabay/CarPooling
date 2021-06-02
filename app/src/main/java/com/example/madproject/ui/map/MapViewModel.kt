package com.example.madproject.ui.map

import androidx.lifecycle.ViewModel
import org.osmdroid.util.GeoPoint

class MapViewModel: ViewModel() {

    /*
        String used to manage the path of Map Fragment. Possible values:
            - "showRoute"       -> path "Trip Detail -> Show Map"
            - "selectDeparture" -> path "Trip Edit -> Select Departure"
            - "selectArrival"   -> path "Trip Edit -> Select Arrival"
            - "selectIntStops"  -> path "Trip Edit -> Select Intermediate Trips"
     */
    var pathManagement = ""

    var geoPoints = mutableListOf<GeoPoint>()
}