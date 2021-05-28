package com.example.madproject.ui.yourtrips

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.lib.isFuture
import com.example.madproject.ui.profile.ProfileViewModel
import com.example.madproject.ui.yourtrips.interestedusers.UserListViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso

class TripListFragment : Fragment(R.layout.fragment_trip_list) {
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

        // reset the flag to "false", since this fragment will set it to "true" if the required navigation is selected
        profileModel.comingFromPrivacy = false

        if (tripListViewModel.tabCompletedTrips) {
            val tab = tabLayout.getTabAt(1)
            tab?.select()
        }

        fab = view.findViewById(R.id.fab)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        // Load the list of trips created by the current user from the DB
        tripListViewModel.getUserTrips().observe(viewLifecycleOwner, {
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
                        tripListViewModel.tabCompletedTrips = false
                        setSelectedList()
                    }

                    "tabCompleted" -> {
                        tripListViewModel.tabCompletedTrips = true
                        setSelectedList()
                    }
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        fab.setOnClickListener{
            tripListViewModel.selectedLocal = Trip()
            tripListViewModel.useDBImage = true
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }

    private fun setSelectedList() {

        val currentList =
            if (tripListViewModel.tabCompletedTrips)
                tripList.filter { !isFuture(it.departureDate, it.departureTime, it.duration) }
            else
                tripList.filter { isFuture(it.departureDate, it.departureTime, it.duration) }

        Log.d("test", "$currentList")

        if (currentList.isNotEmpty()) {
            emptyList.visibility = View.INVISIBLE
            emptyList2.visibility = View.INVISIBLE
        } else {
            emptyList.visibility = View.VISIBLE
            emptyList2.visibility = View.VISIBLE
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

            /*
            Populate the card view of each trip
             */
            fun bind(t: Trip, tlViewModel: TripListViewModel, ulViewModel: UserListViewModel, profileViewModel: ProfileViewModel) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                if (t.imageUrl != "") {
                    Picasso.get().load(t.imageUrl).placeholder(R.drawable.car_example).error(R.drawable.car_example).into(image)
                } else image.setImageResource(R.drawable.car_example)

                cv.setOnClickListener {
                    tlViewModel.selectedLocal = t
                    findNavController(itemView).navigate(R.id.action_tripList_to_tripDetail)
                }

                cardButton.text =
                    if (tlViewModel.tabCompletedTrips) "Rate Passengers" else "Edit"

                cardButton.setOnClickListener {
                    tlViewModel.selectedLocal = t
                    if (tlViewModel.tabCompletedTrips) {
                        ulViewModel.selectedLocalTrip = t
                        profileViewModel.comingFromPrivacy = true
                        findNavController(itemView).navigate(R.id.action_tripList_to_userRate)
                    } else {
                        tlViewModel.useDBImage = true
                        findNavController(itemView).navigate(R.id.action_tripList_to_tripEdit)
                    }
                }
            }

            fun unbind() {
                cardButton.setOnClickListener {  }
                cv.setOnClickListener {  }
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