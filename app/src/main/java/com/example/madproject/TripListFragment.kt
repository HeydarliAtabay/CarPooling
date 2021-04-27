package com.example.madproject

import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TripListFragment : Fragment(R.layout.fragment_trip_list) {
    private var layoutManager: RecyclerView.LayoutManager?=null
    private var adapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>?=null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val edit = view.findViewById<Button>(R.id.edit)
        val details = view.findViewById<Button>(R.id.details)

        val recyclerView=view.findViewById<RecyclerView>(R.id.recyclerView2)
        layoutManager= LinearLayoutManager(context)
        recyclerView.layoutManager =layoutManager

        adapter=RecyclerViewAdapter()
        recyclerView.adapter=adapter


        details.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripDetail)
        }

        edit.setOnClickListener {
            findNavController().navigate(R.id.action_tripList_to_tripEdit)
        }
    }
class RecyclerViewAdapter: RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(){

    val images = listOf<Int>(R.drawable.car_example, R.drawable.car_example, R.drawable.car_example)
    val fromlist = listOf<String>("Rome", "Milano", "Ivrea")
    val tolist = listOf<String>("Torino", "Torino", "Torino")
    val dates = listOf<String>("28/04", "30/04", "02/05")
    val times = listOf<String>("08:30", "10:00", "11:00")
    val prices = listOf<String>("50", "20", "10")

    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        var image : ImageView
        var from_dest : TextView
        var to_dest: TextView
        var date: TextView
        var time : TextView
        var price : TextView

        init{
            image= itemView.findViewById(R.id.image1)
            from_dest= itemView.findViewById(R.id.from_dest)
            to_dest = itemView.findViewById(R.id.to_dest)
            date= itemView.findViewById(R.id.date_txt)
            time=itemView.findViewById(R.id.time_txt)
            price=itemView.findViewById(R.id.price_txt)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v= LayoutInflater.from(parent.context)
        .inflate(R.layout.recyclerview_card,parent,false)
        return ViewHolder(v)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.from_dest.text=fromlist[position]
        holder.to_dest.text=tolist[position]
        holder.date.text=dates[position]
        holder.time.text=times[position]
        holder.price.text=prices[position]
        holder.image.setImageResource(images[position])



        holder.itemView.setOnClickListener{v:View ->
            holder.price.text="basildi"
        }
    }

    override fun getItemCount(): Int {
        return fromlist.size
    }
}
}