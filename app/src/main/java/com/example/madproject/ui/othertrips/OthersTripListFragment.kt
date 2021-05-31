package com.example.madproject.ui.othertrips

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Filters
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
import com.example.madproject.lib.*
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
    private val filterViewModel: FilterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        /*
        If the "needRegistration" flag is true the navigation is redirected to the registration fragment
         */
        if (profileViewModel.needRegistration) {
            val email = FirestoreRepository.currentUser.email ?: ""
            if (email == "") {
                // email field cannot be empty -> there were some problem in the sign-in -> logout
                Toast.makeText(requireContext(), "Missing email, try to sign-in again!", Toast.LENGTH_SHORT).show()
                performLogout(getString(R.string.default_web_client_id), requireActivity(), requireContext())
            }
            val name = FirestoreRepository.currentUser.displayName ?: ""
            val phone = FirestoreRepository.currentUser.phoneNumber ?: ""
            val image = if (FirestoreRepository.currentUser.photoUrl != null) FirestoreRepository.currentUser.photoUrl.toString()
            else ""

            profileViewModel.localProfile = Profile(
                email = email,
                fullName = name,
                phoneNumber = phone,
                imageUrl = image
            )
            findNavController().navigate(R.id.action_othersTripList_to_registerProfile)
        }

        return inflater.inflate(R.layout.fragment_others_trip_list, container, false)
    }

    @ExperimentalStdlibApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        emptyList = view.findViewById(R.id.emptyList)
        filterDialogBuilder = MaterialAlertDialogBuilder(this.requireActivity())

        //if (tripListViewModel.comingFromOther) tripListViewModel.comingFromOther = false
        if (tripListViewModel.pathManagement == "comingFromOther") tripListViewModel.pathManagement = ""

        // Reset the flag that manages the tab selection in "your trips", after going to this page from navigation drawer
        tripListViewModel.tabCompletedTrips = false

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        tripListViewModel.orientation = this.requireActivity().requestedOrientation

        setHasOptionsMenu(true)

        // Observe the dynamic list of trips
        tripListViewModel.getOtherTrips().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                tripList = filteredTripList(it)
                if (tripList.isNotEmpty())
                    emptyList.visibility = View.INVISIBLE
                else
                    emptyList.visibility = View.VISIBLE
                recyclerView.adapter = TripsAdapter(tripList, tripListViewModel)
            }
        })


        // Observe the dynamic filters
        filterViewModel.getFilter().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Problem in setting the filters!", Toast.LENGTH_LONG).show()
            } else {
                filter = it
                val nl = filteredTripList(tripList)
                if (nl.isNotEmpty())
                    emptyList.visibility = View.INVISIBLE
                else
                    emptyList.visibility = View.VISIBLE
                recyclerView.adapter = TripsAdapter(nl, tripListViewModel)
            }
        })

        // If the orientation changed with the dialog opened -> reopen the dialog
        if (filterViewModel.changedOrientation) {
            filter = filterViewModel.temporalFilters
            launchFilterDialog()
            filterViewModel.temporalFilters = Filters()
            filterViewModel.changedOrientation = false
        }

    }

    override fun onPause() {
        super.onPause()
        // If the fragment is paused with the dialog opened save the filters and dismiss the datePicker
        // and the timePicker
        if (filterViewModel.dialogOpened) {
            if (datePicker?.isVisible == true) datePicker?.dismiss()
            if (timePicker?.isVisible == true) timePicker?.dismiss()
            filterViewModel.temporalFilters = Filters(
                from = filterFrom.text.toString(),
                to = filterTo.text.toString(),
                price = parsePrice(filterPrice.text.toString()),
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

    /*
    Create the filter dialog with the custom layout
     */
    @SuppressLint("InflateParams")
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

        filterDialogBuilder
            .setView(filterDialogView)
            .setTitle("Filter the trip list")
            .setPositiveButton("Apply") { _, _ ->
                filterViewModel.setFilter(
                    Filters(
                        from = filterFrom.text.toString(),
                        to = filterTo.text.toString(),
                        price = parsePrice(filterPrice.text.toString()),
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

    /*
    Filter the list of trips with the selected filters
     */
    @ExperimentalStdlibApi
    private fun filteredTripList(longList: List<Trip>): List<Trip> {

        var list = longList
            .asSequence()
            .filter {
                isFuture(it.departureDate, it.departureTime, "")
                //MyFunctions.isFuture(it.departureDate, it.departureTime, "")
            }
            .filter {
                // Filter the Departure Location
                it.from.lowercase().contains(filter.from.lowercase())
            }.filter {
                // Filter the Arrival Location
                it.to.lowercase().contains(filter.to.lowercase())
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
            .toList()

        if (filter.time != "") {
            list = list.filter {
                // Filter the Time
                if (filter.time == it.departureTime) true
                else {
                    // "HH" instead of "hh" represents the 24h format
                    val filterTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(filter.time)
                    val tripTime = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(it.departureTime)
                    tripTime!!.after(filterTime)
                }
            }.sortedWith(compareBy<Trip> { it.departureTime.split(":")[0].toInt() }
                .thenBy { it.departureTime.split(":")[1].toInt() })
        }

        return list
    }

    /*
    Manage the actions on the edit texts of this fragment
     */
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
                filterPrice.setText(parsePrice(filterPrice.text.toString()))
            } else {
                view?.findViewById<TextInputLayout>(R.id.til_filterPrice)?.error = null
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(filterPrice, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    /*
    Set the date picker
     */
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
            val currentDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
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

            val inputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            filterDate.setText(outputFormat.format(inputFormat.parse(datePicker?.headerText!!)!!))
            filterTime.requestFocus()
        }

        datePicker?.show(this.requireActivity().supportFragmentManager, datePicker.toString())
    }

    /*
    Set the time picker
     */
    private fun setTimePicker() {
        var h = 0
        var m = 0

        if (filterTime.text.toString() != "") {
            val s = filterTime.text.toString().split(":")
            if (s.size == 2) {
                h = unParseTime(s[0])
                m = unParseTime(s[1])
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
            filterTime.setText(parseTime(timePicker?.hour, timePicker?.minute))
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

            /*
            Populate the card view of each trip
             */
            fun bind(t: Trip, sharedModel: TripListViewModel) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                if (t.imageUrl != "") {

                    Picasso.get().load(t.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(image)
                } else image.setImageResource(R.drawable.car_example)
                bookTripButton.text = itemView.context.getString(R.string.book_button)

                cv.setOnClickListener {
                    sharedModel.selectedLocal = t
                    sharedModel.pathManagement = "comingFromOther"
                    //sharedModel.comingFromOther = true
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