package com.example.madproject.data

data class Trip(var id: String = "",
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
