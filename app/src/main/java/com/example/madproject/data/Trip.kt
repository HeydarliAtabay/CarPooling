package com.example.madproject.data

data class Trip(var id: Int = -1,
                val imagePath: String = "",
                val from: String = "",
                val to: String = "",
                val departureDate: String = "",
                val departureTime: String = "",
                val duration: String = "",
                val availableSeat: String = "",
                val additionalInfo: String = "",
                val intermediateStop: String = "",
                var price: String = ""
                ){}
