package com.example.madproject

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController

class TripListFragment : Fragment(R.layout.fragment_trip_list) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edit = view.findViewById<Button>(R.id.edit)
        val details = view.findViewById<Button>(R.id.details)

        details.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripDetail)
        }

        edit.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }
}