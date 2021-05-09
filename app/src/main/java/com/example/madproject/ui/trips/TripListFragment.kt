package com.example.madproject.ui.trips

import android.os.Bundle
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Picasso

class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    private var tripList = listOf<Trip>()
    private lateinit var emptyList: TextView
    private lateinit var emptyList2: TextView
    private lateinit var fab: FloatingActionButton
    private val tripListViewModel: TripListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        emptyList2 = view.findViewById(R.id.emptyList2)

        fab = view.findViewById(R.id.fab)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        tripListViewModel.getTrips().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                tripList = it
                if (tripList.isNotEmpty()) {
                    emptyList.visibility = View.INVISIBLE
                    emptyList2.visibility = View.INVISIBLE
                    recyclerView.adapter = TripsAdapter(tripList.toList(), tripListViewModel)
                }
            }
        })

        fab.setOnClickListener{
            tripListViewModel.selectedLocal = Trip()
            tripListViewModel.useDBImage = true
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }

    // The following two methods will go in OtherTripsFragment

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.filters_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.filtersButton -> {

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    class TripsAdapter(val data: List<Trip>, private val sharedModel: TripListViewModel): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.image1)
            private val from = itemView.findViewById<TextView>(R.id.from_dest)
            private val to = itemView.findViewById<TextView>(R.id.to_dest)
            private val date = itemView.findViewById<TextView>(R.id.date_txt)
            private val time = itemView.findViewById<TextView>(R.id.time_txt)
            private val price = itemView.findViewById<TextView>(R.id.price_txt)
            private val editTripButton = itemView.findViewById<Button>(R.id.editTripButton)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)

            fun bind(t: Trip, sharedModel: TripListViewModel) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                if (t.imageUrl != "") {
                    Picasso.get().load(t.imageUrl).error(R.drawable.car_example).into(image)
                } else image.setImageResource(R.drawable.car_example)

                cv.setOnClickListener {
                    sharedModel.selectedLocal = t
                    findNavController(itemView).navigate(R.id.action_tripList_to_tripDetail)
                }

                editTripButton.setOnClickListener {
                    sharedModel.selectedLocal = t
                    sharedModel.useDBImage = true
                    findNavController(itemView).navigate(R.id.action_tripList_to_tripEdit)
                }
            }

            fun unbind() {
                editTripButton.setOnClickListener {  }
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
            holder.bind(data[position], sharedModel)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}