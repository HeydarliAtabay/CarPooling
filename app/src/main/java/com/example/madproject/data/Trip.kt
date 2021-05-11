package com.example.madproject.data

data class Trip(var id: String = "",
                var imageUrl: String = "",
                val from: String = "",
                val to: String = "",
                val departureDate: String = "",
                val departureTime: String = "",
                val duration: String = "",
                val availableSeat: String = "",
                val additionalInfo: String = "",
                val intermediateStops: String = "",
                val price: String = "",
                val ownerEmail: String = ""
                )