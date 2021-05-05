package com.example.madproject.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.File

class ViewModelFactory(private val file: File?): ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedProfileViewModel::class.java)) {
            return SharedProfileViewModel(
                file = file
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}