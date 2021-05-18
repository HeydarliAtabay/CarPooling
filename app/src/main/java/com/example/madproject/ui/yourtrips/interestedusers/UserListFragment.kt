package com.example.madproject.ui.yourtrips.interestedusers

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Booking
import com.example.madproject.data.FirestoreRepository
import com.example.madproject.data.Profile
import com.example.madproject.data.Trip
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        bookButton = view.findViewById(R.id.bookButton)
        tabLayout = view.findViewById(R.id.tab)
        tvSeats = view.findViewById(R.id.tvSeats)

        if (userListViewModel.tabBookings) {
            val tab = tabLayout.getTabAt(1)
            tab?.select()
        }

        recyclerView = view.findViewById(R.id.recyclerView3)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

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

        userListViewModel.getConfirmed().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                confirmed = it
                if (userListViewModel.tabBookings) setConfirmedList()
            }
        })

        userListViewModel.getDBTrip().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                selectedTrip = it
                if (selectedTrip.availableSeat == "0") {
                    val tab = tabLayout.getTabAt(1)
                    tab?.select()
                    userListViewModel.tabBookings = true
                }
                if (!userListViewModel.tabBookings) setProposalsList()
                else setConfirmedList()
            }
        })

        // Setting the listeners on the tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Handle tab select
                when (tab?.contentDescription) {
                    "tabProp" -> {
                        setProposalsList()
                    }

                    "tabBook" -> {
                        setConfirmedList()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        if (userListViewModel.changedOrientationBooking) {
            createBookingDialog()
            userListViewModel.changedOrientationBooking = false
        }
    }

    override fun onPause() {
        super.onPause()
        if (userListViewModel.bookingDialogOpened)
            userListViewModel.changedOrientationBooking = true
    }

    private fun sameLists(old: List<Profile>, new: List<Profile>): Boolean {
        if (old.size != new.size) return false
        if (new.isEmpty()) return false

        for (n in new) {
            if (!old.contains(n)) return false
        }
        return true
    }

    private fun setProposalsList() {
        userListViewModel.tabBookings = false
        if (proposals.isNotEmpty()) {
            emptyList.visibility = View.INVISIBLE
            bookButton.visibility = View.VISIBLE
            tvSeats.text = "Available Seats: ${selectedTrip.availableSeat}"
            tvSeats.visibility = View.VISIBLE
            bookButtonListen()
        } else {
            if (selectedTrip.availableSeat == "0") emptyList.text = "No available seats"
            else emptyList.text = "No proposals found"
            emptyList.visibility = View.VISIBLE
            bookButton.visibility = View.INVISIBLE
            bookButton.setOnClickListener { }
            tvSeats.visibility = View.INVISIBLE
        }

        recyclerView.adapter = UsersAdapter(proposals.toList(), userListViewModel, true)
    }

    private fun setConfirmedList() {
        bookButton.visibility = View.INVISIBLE
        tvSeats.visibility = View.INVISIBLE
        bookButton.setOnClickListener { }
        userListViewModel.tabBookings = true
        if (confirmed.isNotEmpty())
            emptyList.visibility = View.INVISIBLE
        else {
            emptyList.text = "No confirmed bookings"
            emptyList.visibility = View.VISIBLE
        }
        recyclerView.adapter = UsersAdapter(confirmed.toList(), userListViewModel, false)
    }

    private fun bookButtonListen() {
        bookButton.setOnClickListener {
            createBookingDialog()
        }
    }

    private fun createBookingDialog() {
        userListViewModel.bookingDialogOpened = true
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
                userListViewModel.bookingDialogOpened = false
            }
            .show()
    }

    class UsersAdapter(
        val data: List<Profile>,
        private val sharedModel: UserListViewModel,
        private val locationProp: Boolean
    ) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

        class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val image = itemView.findViewById<ImageView>(R.id.imageUser)
            private val fullName = itemView.findViewById<TextView>(R.id.name)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private val check = itemView.findViewById<ImageButton>(R.id.checkedButt)

            fun bind(u: Profile, sharedModel: UserListViewModel, locationProp: Boolean) {

                fullName.text = u.fullName

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


                if (u.imageUrl != "") {
                    Picasso.get().load(u.imageUrl).placeholder(R.drawable.avatar)
                        .error(R.drawable.avatar).into(image)
                } else image.setImageResource(R.drawable.avatar)

                cv.setOnClickListener {
                    sharedModel.selectedLocalUser = u
                    Navigation.findNavController(itemView)
                        .navigate(R.id.action_userList_to_showProfilePrivacy)
                }
            }

            fun unbind(locationProp: Boolean) {
                cv.setOnClickListener { }
                if (locationProp) check.setOnClickListener { }
            }
        }

        override fun onViewRecycled(holder: UserViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind(locationProp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_user, parent, false)
            return UserViewHolder(v)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(data[position], sharedModel, locationProp)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}