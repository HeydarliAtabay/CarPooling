package com.example.madproject.data

data class Trip(val user: String = "",
                var id: String = "",
                var imageUrl: String = "",
                val from: String = "",
                val to: String = "",
                val departureDate: String = "",
                val departureTime: String = "",
                val duration: String = "",
                val availableSeat: String = "",
                val additionalInfo: String = "",
                val intermediateStop: String = "",
                val price: String = "",
                val ownerEmail: String = ""
                )