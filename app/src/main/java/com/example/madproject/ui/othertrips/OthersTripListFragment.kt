package com.example.madproject.ui.othertrips

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Filters
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Trip
import com.example.madproject.lib.MyFunctions
import com.example.madproject.ui.profile.ProfileViewModel
import com.example.madproject.ui.yourtrips.TripListViewModel
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class OthersTripListFragment : Fragment() {
    private var tripList = listOf<Trip>()
    private var filter = Filters()
    private lateinit var emptyList: TextView
    private lateinit var filterDialogBuilder: MaterialAlertDialogBuilder
    private lateinit var filterDialogView : View
    private lateinit var filterFrom: EditText
    private lateinit var filterTo: EditText
    private lateinit var filterDate: EditText
    private lateinit var filterTime: EditText
    private lateinit var filterPrice: EditText
    private var datePicker: MaterialDatePicker<Long>? = null
    private var timePicker: MaterialTimePicker? = null
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    //private lateinit var profileViewModel : ProfileViewModel
    private val filterViewModel: FilterViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        profileViewModel.needRegistration.observe(viewLifecycleOwner, {
            if (it) {
                findNavController().navigate(R.id.action_othersTripList_to_registerProfile)
            }
        })
        Log.d("test", "needRegistration e'"+profileViewModel.needRegistration.value.toString()+" in OtherTrip")
        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        filterDialogBuilder = MaterialAlertDialogBuilder(this.requireActivity())

        if (tripListViewModel.comingFromOther) tripListViewModel.comingFromOther = false

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        setHasOptionsMenu(true)

        // Observe the dynamic list of trips
        tripListViewModel.getOtherTrips().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                tripList = it
                if (tripList.isNotEmpty())
                    emptyList.visibility = View.INVISIBLE
                else
                    emptyList.visibility = View.VISIBLE
                recyclerView.adapter = TripsAdapter(filteredTripList(), tripListViewModel)
            }
        })

        // Observe the dynamic filters
        filterViewModel.getFilter().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Problem in setting the filters!", Toast.LENGTH_LONG).show()
            } else {
                filter = it
                recyclerView.adapter = TripsAdapter(filteredTripList(), tripListViewModel)
            }
        })

        if (filterViewModel.changedOrientation) {
            filter = filterViewModel.temporalFilters
            launchFilterDialog()
            filterViewModel.temporalFilters = Filters()
            filterViewModel.changedOrientation = false
        }

    }

    override fun onPause() {
        super.onPause()
        if (filterViewModel.dialogOpened) {
            if (datePicker?.isVisible == true) datePicker?.dismiss()
            if (timePicker?.isVisible == true) timePicker?.dismiss()
            filterViewModel.temporalFilters = Filters(
                from = filterFrom.text.toString(),
                to = filterTo.text.toString(),
                price = MyFunctions.parsePrice(filterPrice.text.toString()),
                date = filterDate.text.toString(),
                time = filterTime.text.toString()
            )
            filterViewModel.changedOrientation = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filters_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filtersButton -> {
                launchFilterDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun launchFilterDialog() {
        filterViewModel.dialogOpened = true
        filterDialogView = LayoutInflater.from(this.requireActivity())
            .inflate(R.layout.filters_dialog, null, false)
        filterFrom = filterDialogView.findViewById(R.id.filter_departure_location)
        filterTo = filterDialogView.findViewById(R.id.filter_arrival_location)
        filterDate = filterDialogView.findViewById(R.id.filter_date)
        filterTime = filterDialogView.findViewById(R.id.filter_time)
        filterPrice = filterDialogView.findViewById(R.id.filter_price)

        filterFrom.setText(filter.from)
        filterTo.setText(filter.to)
        filterDate.setText(filter.date)
        filterTime.setText(filter.time)
        filterPrice.setText(filter.price)

        fixEditText()

        filterDialogBuilder.setView(filterDialogView)
            .setTitle("Filter the trip list")
            .setPositiveButton("Apply") { _, _ ->
                filterViewModel.setFilter(
                    Filters(
                        from = filterFrom.text.toString(),
                        to = filterTo.text.toString(),
                        price = MyFunctions.parsePrice(filterPrice.text.toString()),
                        date = filterDate.text.toString(),
                        time = filterTime.text.toString()
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .setOnDismissListener {
                filterViewModel.dialogOpened = false
            }
            .show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun filteredTripList(): List<Trip> {

        var list = tripList
            .filter {
                // Filter the Departure Location
                it.from.toLowerCase(Locale.ROOT).contains(filter.from.toLowerCase(Locale.ROOT))
            }.filter {
                // Filter the Arrival Location
                it.to.toLowerCase(Locale.ROOT).contains(filter.to.toLowerCase(Locale.ROOT))
            }.filter {
                // Filter the Max Price
                if (filter.price == "") true
                else
                    it.price.toDouble() <= filter.price.toDouble()
            }.filter {
                // Filter the Date
                if (filter.date == "") true
                else
                    it.departureDate == filter.date
            }

        if (filter.time != "") {
            list = list.filter {
                // Filter the Time
                if (filter.time == it.departureTime) true
                else {
                    // "HH" instead of "hh" represents the 24h format
                    val filterTime = SimpleDateFormat("HH:mm").parse(filter.time)
                    val tripTime = SimpleDateFormat("HH:mm").parse(it.departureTime)
                    tripTime!!.after(filterTime)
                }
            }.sortedWith(compareBy<Trip> { it.departureTime.split(":")[0].toInt() }
                .thenBy { it.departureTime.split(":")[1].toInt() })
        }

        return list
    }

    private fun fixEditText() {
        filterFrom.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                filterFrom.setSelection(0, 0)
            }  else {
                view?.findViewById<TextInputLayout>(R.id.til_filterDeparture)?.error = null
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(filterFrom, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        filterTo.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                filterTo.setSelection(0, 0)
            } else {
                view?.findViewById<TextInputLayout>(R.id.til_filterArrival)?.error = null
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(filterTo, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        filterDate.inputType = InputType.TYPE_NULL

        filterDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view?.findViewById<TextInputLayout>(R.id.til_filterDate)?.error = null
                setDatePicker()
            }
        }

        filterTime.inputType = InputType.TYPE_NULL

        filterTime.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                view?.findViewById<TextInputLayout>(R.id.til_filterTime)?.error = null
                setTimePicker()
            }
        }

        filterPrice.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {  // lost focus
                filterPrice.setSelection(0, 0)
                filterPrice.setText(MyFunctions.parsePrice(filterPrice.text.toString()))
            } else {
                view?.findViewById<TextInputLayout>(R.id.til_filterPrice)?.error = null
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(filterPrice, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setDatePicker() {
        val constraintsBuilder = CalendarConstraints.Builder().setValidator(
            DateValidatorPointForward.now()
        )

        var dPicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select trip departure date")
            .setCalendarConstraints(
                constraintsBuilder.build()
            )

        if (filterDate.text.toString() != "") {
            val currentDate = SimpleDateFormat("MMM dd, yyyy")
            currentDate.timeZone = TimeZone.getTimeZone("UTC")
            val p = currentDate.parse(filterDate.text.toString())
            dPicker = dPicker.setSelection(p?.time)
        }
        datePicker = dPicker.build()

        datePicker?.addOnCancelListener {
            filterDate.clearFocus()
        }

        datePicker?.addOnNegativeButtonClickListener {
            filterDate.clearFocus()
        }

        datePicker?.addOnPositiveButtonClickListener {

            val inputFormat = SimpleDateFormat("dd MMM yyyy")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy")
            filterDate.setText(outputFormat.format(inputFormat.parse(datePicker?.headerText!!)!!))
            filterTime.requestFocus()
        }

        datePicker?.show(this.requireActivity().supportFragmentManager, datePicker.toString())
    }

    private fun setTimePicker() {
        var h = 0
        var m = 0

        if (filterTime.text.toString() != "") {
            val s = filterTime.text.toString().split(":")
            if (s.size == 2) {
                h = MyFunctions.unParseTime(s[0])
                m = MyFunctions.unParseTime(s[1])
            }
        }
        timePicker = MaterialTimePicker.Builder()
            .setTitleText("Select trip departure time")
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(h)
            .setMinute(m)
            .build()

        timePicker?.addOnCancelListener {
            filterTime.clearFocus()
        }

        timePicker?.addOnNegativeButtonClickListener {
            filterTime.clearFocus()
        }

        timePicker?.addOnPositiveButtonClickListener {
            filterTime.setText(MyFunctions.parseTime(timePicker?.hour, timePicker?.minute))
            filterPrice.requestFocus()
        }

        timePicker?.show(this.requireActivity().supportFragmentManager, timePicker.toString())
    }

    class TripsAdapter(val data: List<Trip>, private val sharedModel: TripListViewModel): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.image1)
            private val from = itemView.findViewById<TextView>(R.id.from_dest)
            private val to = itemView.findViewById<TextView>(R.id.to_dest)
            private val date = itemView.findViewById<TextView>(R.id.date_txt)
            private val time = itemView.findViewById<TextView>(R.id.time_txt)
            private val price = itemView.findViewById<TextView>(R.id.price_txt)
            private val bookTripButton = itemView.findViewById<Button>(R.id.editTripButton)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)

            fun bind(t: Trip, sharedModel: TripListViewModel) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                if (t.imageUrl != "") {

                    Picasso.get().load(t.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(image)
                } else image.setImageResource(R.drawable.car_example)
                bookTripButton.text = "Book trip"

                cv.setOnClickListener {
                    sharedModel.selectedLocal = t
                    sharedModel.comingFromOther = true
                    findNavController(itemView).navigate(R.id.action_othersTripList_to_tripDetail)
                }

                bookTripButton.setOnClickListener {
                    sharedModel.tripIdInDialog = t.id
                    openBookingDialog(t, sharedModel)
                }

                if (sharedModel.tripIdInDialog == t.id) {
                    openBookingDialog(t, sharedModel)
                }

            }

            fun unbind() {
                bookTripButton.setOnClickListener {  }
                cv.setOnClickListener {  }
            }

            private fun openBookingDialog(t: Trip, sharedModel: TripListViewModel) {
                MaterialAlertDialogBuilder(itemView.context)
                    .setTitle("New Booking")
                    .setMessage("Are you sure to book this trip?")
                    .setPositiveButton("Yes") { _, _ ->
                        bookTheTrip(t)
                    }
                    .setNegativeButton("No") { _, _ ->
                    }
                    .setOnDismissListener {
                        sharedModel.tripIdInDialog = ""
                    }
                    .show()
            }

            private fun bookTheTrip(trip: Trip) {
                FirestoreRepository().controlBookings(trip)
                    .addOnSuccessListener { it1 ->
                        if (it1.documents.size != 0) {
                            Toast.makeText(itemView.context, "Trip already booked", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            FirestoreRepository().controlProposals(trip)
                                .addOnSuccessListener { it2 ->
                                    if (it2.documents.size != 0) {
                                        Toast.makeText(itemView.context, "Trip already booked", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        FirestoreRepository().proposeBooking(trip)
                                            .addOnSuccessListener {
                                                Toast.makeText(itemView.context, "Booking request successfully sent!", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(itemView.context, "The booking request had a trouble, please retry!", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(itemView.context, "DB access failure", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(itemView.context, "DB access failure", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        override fun onViewRecycled(holder: TripViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val v= LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_trip, parent,false)
            return TripViewHolder(v)
        }

        override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
            holder.bind(data[position], sharedModel)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}