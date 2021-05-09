package com.example.madproject.ui.trips

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BookingAlertDialog: BottomSheetDialogFragment() {

    private val sharedModel: TripListViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireActivity())
            .setTitle("New Booking")
            .setMessage("Are you sure to book this trip?")
            .setPositiveButton("Yes",
                DialogInterface.OnClickListener { _, _ ->
                    sharedModel.setBookTheTrip(true)
                })
            .setNegativeButton("No",
                DialogInterface.OnClickListener { _, _ ->
                })

        return builder.create()
    }
}