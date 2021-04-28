package com.example.madproject

import java.math.BigDecimal

data class Trip(val imagePath: String,
                val from: String,
                val to: String,
                val departureDate: String,
                val departureTime: String,
                val duration: String,
                val availableSeat: String,
                val additionalInfo: String,
                val intermediateStop: String,
                val price:BigDecimal){

}
