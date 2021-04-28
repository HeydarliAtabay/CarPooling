package com.example.madproject

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
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.lib.FixOrientation
import com.example.madproject.lib.ValueIds
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    lateinit var tripList: List<Trip>
    lateinit var emptyList: TextView
    lateinit var sharedPref: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        emptyList = view.findViewById(R.id.emptyList)

        val fab=view.findViewById<FloatingActionButton>(R.id.fab)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        if(sharedPref.contains(ValueIds.JSON_OBJECT_TRIPS.value)) {
            loadTrips()
            emptyList.visibility = View.INVISIBLE
            recyclerView.adapter = TripsAdapter(tripList)
        }

        fab.setOnClickListener{
            val action = TripListFragmentDirections.actionTripListToTripEdit(
                group11Lab2TRIPID = -1,
                group11Lab2TRIPARRIVAL = "",
                group11Lab2TRIPDEPARTURE = "",
                group11Lab2CURRENTCARPHOTOPATH = "",
                group11Lab2TRIPDATE = "",
                group11Lab2TRIPDURATION = "",
                group11Lab2TRIPINFO = "",
                group11Lab2TRIPPRICE = "",
                group11Lab2TRIPSEATS = "",
                group11Lab2TRIPSTOPS = "",
                group11Lab2TRIPTIME = ""
            )
            findNavController().navigate(action)
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

    class TripsAdapter(val data:List<Trip>): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

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

            fun bind(t: Trip) {
                from_dest.text = t.from
                to_dest.text = t.to
                date.text = t.departureDate
                time.text = t.departureTime
                price.text = t.price.toEngineeringString()
                currentPhotoPath = t.imagePath

                setPic()

                cv.setOnClickListener {
                    val action = TripListFragmentDirections.actionTripListToTripDetail(
                        group11Lab2TRIPID = t.id,
                        group11Lab2TRIPARRIVAL = t.to,
                        group11Lab2TRIPDEPARTURE = t.from,
                        group11Lab2CURRENTCARPHOTOPATH = t.imagePath,
                        group11Lab2TRIPDATE = t.departureDate,
                        group11Lab2TRIPDURATION = t.duration,
                        group11Lab2TRIPINFO = t.additionalInfo,
                        group11Lab2TRIPPRICE = t.price.toEngineeringString(),
                        group11Lab2TRIPSEATS = t.availableSeat,
                        group11Lab2TRIPSTOPS = t.intermediateStop,
                        group11Lab2TRIPTIME = t.departureTime
                    )
                    findNavController(itemView).navigate(action)
                }

                editTripButton.setOnClickListener {
                    val action = TripListFragmentDirections.actionTripListToTripEdit(
                        group11Lab2TRIPID = t.id,
                        group11Lab2TRIPARRIVAL = t.to,
                        group11Lab2TRIPDEPARTURE = t.from,
                        group11Lab2CURRENTCARPHOTOPATH = t.imagePath,
                        group11Lab2TRIPDATE = t.departureDate,
                        group11Lab2TRIPDURATION = t.duration,
                        group11Lab2TRIPINFO = t.additionalInfo,
                        group11Lab2TRIPPRICE = t.price.toEngineeringString(),
                        group11Lab2TRIPSEATS = t.availableSeat,
                        group11Lab2TRIPSTOPS = t.intermediateStop,
                        group11Lab2TRIPTIME = t.departureTime
                    )
                    findNavController(itemView).navigate(action)
                }
            }

            fun unbind() {
                editTripButton.setOnClickListener { null }
                cv.setOnClickListener { null }
            }

            private fun setPic() {
                if (currentPhotoPath != "") {
                    val imgFile = File(currentPhotoPath!!)
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

            holder.bind(data[position])

        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}