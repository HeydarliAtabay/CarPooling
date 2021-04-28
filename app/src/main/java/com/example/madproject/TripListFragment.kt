package com.example.madproject

import android.os.Bundle
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.*
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fab=view.findViewById<FloatingActionButton>(R.id.fab)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        val images = listOf<Int>(R.drawable.car_example, R.drawable.car_example, R.drawable.car_example)
        val fromlist = listOf<String>("Rome", "Milano", "Ivrea")
        val tolist = listOf<String>("Torino", "Torino", "Torino")
        val dates = listOf<String>("28/04", "30/04", "02/05")
        val times = listOf<String>("08:30", "10:00", "11:00")
        val prices = listOf<String>("50", "20", "10")

        val items = mutableListOf<Trip>()

        for (i in 0..2) {
            val item = Trip(images[i], fromlist[i], tolist[i], dates[i], times[i], prices[i])
            items += listOf(item)
        }

        recyclerView.adapter = TripsAdapter(items)

        fab.setOnClickListener{
            val action = TripListFragmentDirections.actionTripListToTripEdit(
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

    class TripsAdapter(val data: List<Trip>): RecyclerView.Adapter<TripsAdapter.TripViewHolder>(){

        class TripViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            val image = itemView.findViewById<ImageView>(R.id.image1)
            val from_dest = itemView.findViewById<TextView>(R.id.from_dest)
            val to_dest = itemView.findViewById<TextView>(R.id.to_dest)
            val date = itemView.findViewById<TextView>(R.id.date_txt)
            val time = itemView.findViewById<TextView>(R.id.time_txt)
            val price = itemView.findViewById<TextView>(R.id.price_txt)
            val editTripButton = itemView.findViewById<Button>(R.id.editTripButton)
            val cv = itemView.findViewById<CardView>(R.id.card_view)

            fun bind(t: Trip) {
                from_dest.text = t.from
                to_dest.text = t.to
                date.text = t.date
                time.text = t.time
                price.text = t.price

                cv.setOnClickListener {
                    val action = TripListFragmentDirections.actionTripListToTripDetail(
                        group11Lab2TRIPARRIVAL = to_dest.text.toString(),
                        group11Lab2TRIPDEPARTURE = from_dest.text.toString(),
                        group11Lab2CURRENTCARPHOTOPATH = "",
                        group11Lab2TRIPDATE = date.text.toString(),
                        group11Lab2TRIPDURATION = "5000000000000000",
                        group11Lab2TRIPINFO = "",
                        group11Lab2TRIPPRICE = price.text.toString(),
                        group11Lab2TRIPSEATS = "",
                        group11Lab2TRIPSTOPS = "",
                        group11Lab2TRIPTIME = time.text.toString()
                    )
                    findNavController(itemView).navigate(action)
                }

                editTripButton.setOnClickListener {
                    val action = TripListFragmentDirections.actionTripListToTripEdit(
                        group11Lab2TRIPARRIVAL = to_dest.text.toString(),
                        group11Lab2TRIPDEPARTURE = from_dest.text.toString(),
                        group11Lab2CURRENTCARPHOTOPATH = "",
                        group11Lab2TRIPDATE = date.text.toString(),
                        group11Lab2TRIPDURATION = "40000000000000000000",
                        group11Lab2TRIPINFO = "",
                        group11Lab2TRIPPRICE = price.text.toString(),
                        group11Lab2TRIPSEATS = "",
                        group11Lab2TRIPSTOPS = "",
                        group11Lab2TRIPTIME = time.text.toString()
                    )
                    findNavController(itemView).navigate(action)
                }
            }

            fun unbind() {
                editTripButton.setOnClickListener { null }
                cv.setOnClickListener { null }
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