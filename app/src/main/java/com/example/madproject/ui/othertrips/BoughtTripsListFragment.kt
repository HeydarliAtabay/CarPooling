package com.example.madproject.ui.othertrips

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.lib.isFuture
import com.example.madproject.ui.profile.ProfileViewModel
import com.example.madproject.ui.yourtrips.TripListViewModel
import com.example.madproject.ui.yourtrips.interestedusers.UserListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import com.example.madproject.data.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class BoughtTripsListFragment : Fragment(R.layout.fragment_trip_list) {
    private var tripList = listOf<Trip>()
    private lateinit var emptyList: TextView
    private lateinit var emptyList2: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var fab: FloatingActionButton
    private lateinit var recyclerView: RecyclerView
    private val tripListViewModel: TripListViewModel by activityViewModels()
    private val userListModel: UserListViewModel by activityViewModels()
    private val profileModel: ProfileViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        emptyList2 = view.findViewById(R.id.emptyList2)
        tabLayout = view.findViewById(R.id.tabUserTrips)

        // Reset the flag that manages the tab selection in "Your Trips",
        // after going to this page from navigation drawer
        tripListViewModel.tabCompletedTrips = false

        tripListViewModel.pathManagementTrip = "boughtUpcomingTrips"

        if (tripListViewModel.tabCompletedTripsBooked) {
            val tab = tabLayout.getTabAt(1)
            tab?.select()
            tripListViewModel.pathManagementTrip = "boughtCompletedTrips"
        }

        fab = view.findViewById(R.id.fab)
        fab.hide()
        emptyList2.visibility = View.INVISIBLE

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        // Load the list of booked trips for the current user from the DB
        tripListViewModel.getConfirmedTrips().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                tripList = it
                setSelectedList()
            }
        })

        // Setting the listeners on the tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab select
                when (tab?.contentDescription) {
                    "tabUpcoming" -> {
                        tripListViewModel.tabCompletedTripsBooked = false
                        tripListViewModel.pathManagementTrip = "boughtUpcomingTrips"
                        setSelectedList()
                    }

                    "tabCompleted" -> {
                        tripListViewModel.tabCompletedTripsBooked = true
                        tripListViewModel.pathManagementTrip = "boughtCompletedTrips"
                        setSelectedList()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

    }

    private fun setSelectedList() {

        val currentList =
            if (tripListViewModel.tabCompletedTripsBooked)
                tripList.filter { !isFuture(it.departureDate, it.departureTime, it.duration) }
            else
                tripList.filter { isFuture(it.departureDate, it.departureTime, it.duration) }

        if (currentList.isNotEmpty()) {
            emptyList.visibility = View.INVISIBLE
        } else {
            emptyList.visibility = View.VISIBLE
        }
        recyclerView.adapter = TripsAdapter(currentList, tripListViewModel, userListModel, profileModel)

    }

    class TripsAdapter(
        val data: List<Trip>,
        private val tripListViewModel: TripListViewModel,
        private val userListViewModel: UserListViewModel,
        private val profileViewModel: ProfileViewModel
    ): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.image1)
            private val from = itemView.findViewById<TextView>(R.id.from_dest)
            private val to = itemView.findViewById<TextView>(R.id.to_dest)
            private val date = itemView.findViewById<TextView>(R.id.date_txt)
            private val time = itemView.findViewById<TextView>(R.id.time_txt)
            private val price = itemView.findViewById<TextView>(R.id.price_txt)
            private val cardButton = itemView.findViewById<Button>(R.id.editTripButton)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private var dialog: AlertDialog? = null

            /*
            Populate the card view of each trip
             */
            fun bind(
                t: Trip,
                tlViewModel: TripListViewModel,
                ulViewModel: UserListViewModel,
                profileViewModel: ProfileViewModel
            ) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                val booking :Booking = profileViewModel.getBookingByTripId(t.id)
                if (t.imageUrl != "") {
                    Picasso.get().load(t.imageUrl).placeholder(R.drawable.car_example)
                        .error(R.drawable.car_example).into(image)
                } else image.setImageResource(R.drawable.car_example)

                cv.setOnClickListener {
                    tlViewModel.selectedLocal = t
                    Navigation.findNavController(itemView)
                        .navigate(R.id.action_bookedTrips_to_tripDetail)
                }

                if (tlViewModel.tabCompletedTripsBooked && !booking.driverRated) {
                    cardButton.text = itemView.context.getString(R.string.rate_driver)
                    cardButton.setOnClickListener {
                        tlViewModel.selectedLocal = t
                        openRatingDriverDialog(t, booking, ulViewModel, profileViewModel)
                    }
                } else cardButton.visibility = View.INVISIBLE

                if (ulViewModel.tripInDialog == t.id) {
                    openRatingDriverDialog(t, booking, ulViewModel, profileViewModel)
                }
            }

            private fun openRatingDriverDialog(t: Trip, b: Booking, sharedModel: UserListViewModel, profileModel: ProfileViewModel) {
                val ratingDialogBuilder = MaterialAlertDialogBuilder(itemView.context)
                val ratingDialogView: View = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.rating_dialog, null, false)
                val ratingBar = ratingDialogView.findViewById<RatingBar>(R.id.ratingStars)
                val comment = ratingDialogView.findViewById<EditText>(R.id.ratingComment)

                ratingBar.rating = sharedModel.rating
                comment.setText(sharedModel.comment)

                sharedModel.tripInDialog = t.id

                // Listeners to update properly the viewModel (useful for layout orientation change)
                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    sharedModel.rating = rating
                }
                comment.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        sharedModel.comment = s.toString()
                    }

                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                    }
                })

                dialog?.dismiss()
                dialog = ratingDialogBuilder.setView(ratingDialogView)
                    .setTitle("Insert a new rating")
                    .setPositiveButton("Yes") { _, _ ->
                        FirestoreRepository().insertRating(
                            r = Rating(
                                tripId = t.id,
                                rating = ratingBar.rating,
                                comment = comment.text.toString()
                            ),
                            userEmail = t.ownerEmail,
                            passenger = false,
                            b = b
                        ).addOnSuccessListener {
                            Toast.makeText(
                                itemView.context,
                                "New rating added. Thank you for your feedback!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }.addOnFailureListener {
                            Toast.makeText(
                                itemView.context,
                                "Problems in adding the rating!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("No") { _, _ ->
                    }
                    .setOnDismissListener {
                        sharedModel.tripInDialog = ""
                        sharedModel.comment = ""
                        sharedModel.rating = 0.0F
                    }.show()

            }


        fun unbind() {
            cardButton.setOnClickListener { }
            cv.setOnClickListener { }
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
            holder.bind(data[position], tripListViewModel, userListViewModel, profileViewModel)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}