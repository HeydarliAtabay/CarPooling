package com.example.madproject.ui.trips

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.lib.FixOrientation
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    private var tripList = listOf<Trip>()
    private lateinit var emptyList: TextView
    private lateinit var emptyList2: TextView
    private val sharedModel: SharedTripViewModel by activityViewModels()
    private lateinit var tripListViewModel: TripListViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)
        emptyList2 = view.findViewById(R.id.emptyList2)

        tripListViewModel = ViewModelProvider(this, TripListFactory())
            .get(TripListViewModel::class.java)

        val fab=view.findViewById<FloatingActionButton>(R.id.fab)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        tripListViewModel.getTrips().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                tripList = it
                if (tripList.isNotEmpty()) {
                    emptyList.visibility = View.INVISIBLE
                    emptyList2.visibility = View.INVISIBLE
                    recyclerView.adapter = TripsAdapter(tripList.toList(), sharedModel)
                }
            }
        })

        /*
        val db = FirebaseFirestore.getInstance()

        db.collection("Trips").get()
                .addOnSuccessListener { documents ->

                    for (doc in documents) {
                        tripList.add(doc.toObject(Trip::class.java))
                    }
                    if(tripList.size != 0) {
                        emptyList.visibility = View.INVISIBLE
                        emptyList2.visibility = View.INVISIBLE
                        recyclerView.adapter = TripsAdapter(tripList.toList(), sharedModel)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                }
*/
        fab.setOnClickListener{
            sharedModel.select(Trip())
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }

    class TripsAdapter(val data: List<Trip>, private val sharedModel: SharedTripViewModel): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.image1)
            private val from = itemView.findViewById<TextView>(R.id.from_dest)
            private val to = itemView.findViewById<TextView>(R.id.to_dest)
            private val date = itemView.findViewById<TextView>(R.id.date_txt)
            private val time = itemView.findViewById<TextView>(R.id.time_txt)
            private val price = itemView.findViewById<TextView>(R.id.price_txt)
            private val editTripButton = itemView.findViewById<Button>(R.id.editTripButton)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private var currentPhotoPath:String = ""

            fun bind(t: Trip, sharedModel: SharedTripViewModel) {
                from.text = t.from
                to.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price
                currentPhotoPath = t.imagePath

                setPic()

                cv.setOnClickListener {
                    sharedModel.select(t)
                    findNavController(itemView).navigate(R.id.action_tripList_to_tripDetail)
                }

                editTripButton.setOnClickListener {
                    sharedModel.select(t)
                    findNavController(itemView).navigate(R.id.action_tripList_to_tripEdit)
                }
            }

            fun unbind() {
                editTripButton.setOnClickListener { null }
                cv.setOnClickListener { null }
            }

            private fun setPic() {
                if (currentPhotoPath != "") {
                    val imgFile = File(currentPhotoPath)
                    val photoURI:Uri = FileProvider.getUriForFile(itemView.context.applicationContext, "com.example.android.fileprovider", imgFile)
                    val pic = FixOrientation.handleSamplingAndRotationBitmap(itemView.context.applicationContext, photoURI)
                    image.setImageBitmap(pic)
                } else image.setImageResource(R.drawable.car_example)
            }

        }

        override fun onViewRecycled(holder: TripViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
            val v= LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_card, parent,false)
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