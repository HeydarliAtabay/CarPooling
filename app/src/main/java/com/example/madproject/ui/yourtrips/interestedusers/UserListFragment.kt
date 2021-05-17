package com.example.madproject.ui.yourtrips.interestedusers

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso


class UserListFragment : Fragment(R.layout.fragment_user_list) {
    private var proposals = listOf<Profile>()
    private var confirmed = listOf<Profile>()
    private lateinit var emptyList: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookButton: Button
    private val userListViewModel: UserListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        bookButton = view.findViewById(R.id.bookButton)
        tabLayout = view.findViewById(R.id.tab)

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
                proposals = it
                if (!userListViewModel.tabBookings) setProposalsList()
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

        // Setting the listeners on the tab
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            // tab?.contentDescription == "tabProp"
            // tab?.contentDescription == "tabBook"
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

            override fun onTabReselected(tab: TabLayout.Tab?) { }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })
    }

    private fun setProposalsList() {
        userListViewModel.tabBookings = false
        if (proposals.isNotEmpty()) {
            emptyList.visibility = View.INVISIBLE
            bookButton.visibility = View.VISIBLE
            bookButtonListen()
        }
        else {
            emptyList.visibility = View.VISIBLE
            bookButton.visibility = View.INVISIBLE
            bookButton.setOnClickListener { }
        }
        recyclerView.adapter = UsersAdapter(proposals.toList(), userListViewModel, true)


    }

    private fun setConfirmedList() {
        bookButton.visibility = View.INVISIBLE
        bookButton.setOnClickListener { }
        userListViewModel.tabBookings = true
        if (confirmed.isNotEmpty())
            emptyList.visibility = View.INVISIBLE
        else
            emptyList.visibility = View.VISIBLE
        recyclerView.adapter = UsersAdapter(confirmed.toList(), userListViewModel, false)
    }

    private fun bookButtonListen() {
        bookButton.setOnClickListener {
            Toast.makeText(this.requireActivity(), "Bookings confirmed", Toast.LENGTH_SHORT).show()
        }
    }

    class UsersAdapter(val data: List<Profile>, private val sharedModel: UserListViewModel, private val locationProp: Boolean)
        : RecyclerView.Adapter<UsersAdapter.UserViewHolder>(){

        class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.imageUser)
            private val fullName = itemView.findViewById<TextView>(R.id.name)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private val check = itemView.findViewById<ImageButton>(R.id.checkedButt)

            fun bind(u: Profile, sharedModel: UserListViewModel, locationProp: Boolean) {

                fullName.text = u.fullName

                if (locationProp) {
                    if (sharedModel.getBooking(u).confirmed) check.setImageResource(R.drawable.ic_icons8_checked_32_yes)
                    else check.setImageResource(R.drawable.ic_icons8_checked_32_no)

                    check.setOnClickListener {
                        sharedModel.setBookingFlag(u)
                        if (sharedModel.getBooking(u).confirmed) check.setImageResource(R.drawable.ic_icons8_checked_32_yes)
                        else check.setImageResource(R.drawable.ic_icons8_checked_32_no)
                    }

                } else check.setImageResource(R.drawable.ic_icons8_checked_32_yes)


                if (u.imageUrl != "") {
                    Picasso.get().load(u.imageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(image)
                } else image.setImageResource(R.drawable.avatar)

                cv.setOnClickListener {
                    sharedModel.selectedLocalUser = u
                    Navigation.findNavController(itemView).navigate(R.id.action_userList_to_showProfilePrivacy)
                }
            }

            fun unbind(locationProp: Boolean) {
                cv.setOnClickListener {  }
                if (locationProp) check.setOnClickListener {  }
            }
        }

        override fun onViewRecycled(holder: UserViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind(locationProp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val v= LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_user, parent,false)
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