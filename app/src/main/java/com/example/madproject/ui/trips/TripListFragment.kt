package com.example.madproject.ui.trips

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Trip
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.ValueIds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    private lateinit var tripList: List<Trip>
    private lateinit var emptyList: TextView
    private lateinit var emptyList2: TextView
    private lateinit var sharedPref: SharedPreferences
    private val sharedModel: SharedTripViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        emptyList = view.findViewById(R.id.emptyList)
        emptyList2 = view.findViewById(R.id.emptyList2)

        val fab=view.findViewById<FloatingActionButton>(R.id.fab)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        if(sharedPref.contains(ValueIds.JSON_OBJECT_TRIPS.value)) {
            loadTrips()
            emptyList.visibility = View.INVISIBLE
            emptyList2.visibility = View.INVISIBLE
            recyclerView.adapter = TripsAdapter(tripList, sharedModel)
        }

        fab.setOnClickListener{
            sharedModel.select(Trip())
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }

    private fun loadTrips(){
        val gson = Gson()
        val pref = sharedPref.getString(ValueIds.JSON_OBJECT_TRIPS.value, null)
        val type = object : TypeToken<List<Trip>>() {}.type
        if (pref != null) {
            tripList = gson.fromJson(pref, type)
        }
    }

    class TripsAdapter(val data: List<Trip>, val sharedModel: SharedTripViewModel): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.image1)
            private val from_dest = itemView.findViewById<TextView>(R.id.from_dest)
            private val to_dest = itemView.findViewById<TextView>(R.id.to_dest)
            private val date = itemView.findViewById<TextView>(R.id.date_txt)
            private val time = itemView.findViewById<TextView>(R.id.time_txt)
            private val price = itemView.findViewById<TextView>(R.id.price_txt)
            private val editTripButton = itemView.findViewById<Button>(R.id.editTripButton)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)
            private var currentPhotoPath:String = ""

            fun bind(t: Trip, sharedModel: SharedTripViewModel) {
                from_dest.text = t.from
                to_dest.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price?.toEngineeringString()
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