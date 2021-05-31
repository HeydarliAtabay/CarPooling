package com.example.madproject.ui.yourtrips.interestedusers

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.*
import com.example.madproject.ui.yourtrips.TripListViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso

class UserListFragment : Fragment(R.layout.fragment_user_list) {
    private var proposals = listOf<Profile>()
    private var confirmed = listOf<Profile>()
    private var selectedTrip = Trip()
    private lateinit var emptyList: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookButton: Button
    private lateinit var tvSeats: TextView
    private val userListViewModel: UserListViewModel by activityViewModels()
    private val tripListViewModel: TripListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        bookButton = view.findViewById(R.id.bookButton)
        tabLayout = view.findViewById(R.id.tab)
        tvSeats = view.findViewById(R.id.tvSeats)

        recyclerView = view.findViewById(R.id.recyclerView3)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setItemViewCacheSize(2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        confirmed = userListViewModel.getConfirmed().value ?: listOf()
        proposals = userListViewModel.getProposals().value ?: listOf()

        // If the user list refers to a "Completed trip", load the proper view
        if (tripListViewModel.pathManagement == "tabCompleted") {

            tabLayout.visibility = View.INVISIBLE

            // Get the list of users who have a confirmed booking on the selected trip
            userListViewModel.getConfirmed().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    confirmed = it
                    setConfirmedList()
                }
            })
        } else {

            // If the fragment was destroyed (i.e., for orientation change) with the booking tab opened,
            // select the confirmed bookings tab
            if (userListViewModel.tabBookings) {
                val tab = tabLayout.getTabAt(1)
                tab?.select()
                setConfirmedList()
            } else
                setProposalsList()

            // Get the list of users who made a proposal to the selected trip
            userListViewModel.getProposals().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    if (!sameLists(proposals, it)) {
                        proposals = it
                        if (!userListViewModel.tabBookings) setProposalsList()
                    }
                }
            })

            // Get the list of users who have a confirmed booking on the selected trip
            userListViewModel.getConfirmed().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    confirmed = it
                    if (userListViewModel.tabBookings)
                        setConfirmedList()

                }
            })

            // Get the selected trip updated from the DB. It is needed to observe the available seats
            userListViewModel.getDBTrip().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    selectedTrip = it
                    // If there are no more available seats it is selected the bookings tab
                    if (selectedTrip.availableSeat == "0") {
                        val tab = tabLayout.getTabAt(1)
                        tab?.select()
                    }
                }
            })

            // Setting the listeners on the tab
            tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

                override fun onTabSelected(tab: TabLayout.Tab?) {
                    // Handle tab select
                    when (tab?.contentDescription) {
                        "tabProp" -> setProposalsList()

                        "tabBook" -> setConfirmedList()
                    }
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {}

                override fun onTabUnselected(tab: TabLayout.Tab?) {}
            })

            // If the orientation was changed with the dialog opened -> reopen the dialog
            if (userListViewModel.changedOrientation) {
                createConfirmBookingDialog()
                userListViewModel.changedOrientation = false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (userListViewModel.confirmBookingDialogOpened)
            userListViewModel.changedOrientation = true
    }

    /*
    Function to check if the old list of users and the new list of users (got from the DB) are the same
    If yes the current list is not updated, in this way the recycler view is not re-rendered
     */
    private fun sameLists(old: List<Profile>, new: List<Profile>): Boolean {
        if (old.size != new.size) return false
        if (new.isEmpty()) return false

        for (n in new) {
            if (!old.contains(n)) return false
        }
        return true
    }

    /*
    Load the recycler view with the list of users who made a proposal to the trip
     */
    private fun setProposalsList() {
        if (userListViewModel.tabBookings)
            userListViewModel.tabBookings = false

        if (proposals.isNotEmpty()) {
            emptyList.visibility = View.INVISIBLE
            bookButton.visibility = View.VISIBLE
            tvSeats.text = getString(R.string.remaining_seats, selectedTrip.availableSeat)
            tvSeats.visibility = View.VISIBLE
            bookButtonListen()
        } else {
            if (selectedTrip.availableSeat == "0") emptyList.text = getString(R.string.no_seats_message)
            else emptyList.text = getString(R.string.no_proposals_message)
            emptyList.visibility = View.VISIBLE
            bookButton.visibility = View.INVISIBLE
            bookButton.setOnClickListener { }
            tvSeats.visibility = View.INVISIBLE
        }

        recyclerView.adapter = UsersAdapter(
            proposals.toList(), userListViewModel,
            true, tripListViewModel
        )

    }

    /*
    Load the recycler view with the list of users who have a confirmed booking on the trip
     */
    private fun setConfirmedList() {
        bookButton.visibility = View.INVISIBLE
        tvSeats.visibility = View.INVISIBLE
        bookButton.setOnClickListener { }
        if (!userListViewModel.tabBookings)
            userListViewModel.tabBookings = true

        if (confirmed.isNotEmpty())
            emptyList.visibility = View.INVISIBLE
        else {
            if (userListViewModel.tabBookings)
                emptyList.text = getString(R.string.ratings_completed)
            else
                emptyList.text = getString(R.string.no_bookings_message)
            emptyList.visibility = View.VISIBLE
        }
        recyclerView.adapter = UsersAdapter(
            confirmed.toList(), userListViewModel,
            false, tripListViewModel
        )
    }

    private fun bookButtonListen() {
        bookButton.setOnClickListener {
            createConfirmBookingDialog()
        }
    }

    /*
    Function to create the dialog to confirm the selected proposals and to send the request to the DB
     */
    private fun createConfirmBookingDialog() {
        userListViewModel.confirmBookingDialogOpened = true
        MaterialAlertDialogBuilder(this.requireContext())
            .setTitle("Confirm Bookings")
            .setMessage("Are you sure to confirm the selected bookings?")
            .setPositiveButton("Yes") { _, _ ->
                val bookingsConf = mutableListOf<Booking>()
                val props = mutableListOf<Booking>()
                for (u in proposals) {
                    if (userListViewModel.getBooking(u).confirmed)
                        bookingsConf.add(userListViewModel.getBooking(u))
                    else
                        props.add(userListViewModel.getBooking(u))
                }

                if (bookingsConf.size > 0) {
                    FirestoreRepository().bookingTransaction(
                        selectedTrip,
                        bookingsConf,
                        props
                    ).addOnSuccessListener {
                        Toast.makeText(
                            this.requireActivity(),
                            "Selected bookings successfully confirmed!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener { e ->
                        Toast.makeText(
                            this.requireActivity(),
                            e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this.requireActivity(),
                        "Select some proposal to confirm the booking!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("No") { _, _ ->
            }
            .setOnDismissListener {
                userListViewModel.confirmBookingDialogOpened = false
            }
            .show()
    }

    class UsersAdapter(
        val data: List<Profile>,
        private val sharedModel: UserListViewModel,
        private val locationProp: Boolean,
        private val tripListModel: TripListViewModel,
    ) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val image = itemView.findViewById<ImageView>(R.id.imageUser)
            private val fullName = itemView.findViewById<TextView>(R.id.name)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private val check = itemView.findViewById<ImageButton>(R.id.checkedButt)
            private var dialog: AlertDialog? = null

            /*
            Populate the card view of each user
             */
            fun bind(
                u: Profile,
                sharedModel: UserListViewModel,
                locationProp: Boolean,
                tripListModel: TripListViewModel
            ) {

                fullName.text = u.fullName

                // If the user comes to this list to rate the passengers load the proper view
                if (tripListModel.pathManagement == "tabCompleted") {
                    check.setBackgroundResource(R.drawable.button_state_selector)
                    check.setImageResource(R.drawable.ic_star)
                    check.setOnClickListener {
                        sharedModel.comment = ""
                        sharedModel.rating = 0.0F
                        openRatingDialog(u, sharedModel)
                    }
                } else {
                    if (locationProp) {
                        val booking = sharedModel.getBooking(u)
                        if (booking.confirmed) check.setImageResource(R.drawable.ic_icons8_checked_32_yes)
                        else check.setImageResource(R.drawable.ic_icons8_checked_32_no)

                        check.setOnClickListener {
                            sharedModel.setBookingFlag(u)
                            booking.confirmed = !booking.confirmed
                            if (booking.confirmed) check.setImageResource(R.drawable.ic_icons8_checked_32_yes)
                            else check.setImageResource(R.drawable.ic_icons8_checked_32_no)
                        }

                    } else check.setImageResource(R.drawable.ic_icons8_checked_32_yes)
                }

                if (u.imageUrl != "") {
                    Picasso.get().load(u.imageUrl).placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar).into(image)
                } else image.setImageResource(R.drawable.avatar)

                cv.setOnClickListener {
                    sharedModel.selectedLocalUserEmail = u.email
                    if (tripListModel.pathManagement == "tabCompleted") {
                        findNavController(itemView)
                            .navigate(R.id.action_userRate_to_showProfilePrivacy)
                    } else
                        findNavController(itemView)
                            .navigate(R.id.action_userList_to_showProfilePrivacy)
                }

                if (sharedModel.userEmailInDialog == u.email) {
                    openRatingDialog(u, sharedModel)
                }
            }

            fun unbind() {
                cv.setOnClickListener { }
                check.setOnClickListener { }
                dialog?.dismiss()
            }

            private fun openRatingDialog(u: Profile, sharedModel: UserListViewModel) {
                val ratingDialogBuilder = MaterialAlertDialogBuilder(itemView.context)
                val ratingDialogView: View = LayoutInflater.from(itemView.context)
                    .inflate(R.layout.rating_dialog, null, false)
                val ratingBar = ratingDialogView.findViewById<RatingBar>(R.id.ratingStars)
                val comment = ratingDialogView.findViewById<EditText>(R.id.ratingComment)

                ratingBar.rating = sharedModel.rating
                comment.setText(sharedModel.comment)

                sharedModel.userEmailInDialog = u.email

                // Listeners to update properly the viewModel (useful for layout orientation change)
                ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
                    sharedModel.rating = rating
                }
                comment.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        sharedModel.comment = s.toString()
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                })

                dialog?.dismiss()
                dialog = ratingDialogBuilder.setView(ratingDialogView)
                    .setTitle("Insert a new rating")
                    .setPositiveButton("Yes") { _, _ ->
                        FirestoreRepository().insertRating(
                            r = Rating(
                                tripId = sharedModel.selectedLocalTrip.id,
                                rating = ratingBar.rating,
                                comment = comment.text.toString()
                            ),
                            userEmail = u.email,
                            passenger = true,
                            b = sharedModel.getBooking(u)
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
                        sharedModel.userEmailInDialog = ""
                        sharedModel.comment = ""
                        sharedModel.rating = 0.0F
                    }.show()

            }
        }

        override fun onViewRecycled(holder: UserViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_user, parent, false)
            return UserViewHolder(v)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(data[position], sharedModel, locationProp, tripListModel)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}