package com.example.madproject.ui.yourtrips.interestedusers

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.squareup.picasso.Picasso


class UserListFragment : Fragment(R.layout.fragment_user_list) {
    private var userList = listOf<Profile>()
    private lateinit var emptyList: TextView
    private val userListViewModel: UserListViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyList)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView3)
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(2)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        userListViewModel.getUsers().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                userList = it
                if (userList.isNotEmpty()) {
                    emptyList.visibility = View.INVISIBLE
                    recyclerView.adapter = UsersAdapter(userList.toList(), userListViewModel)
                }
            }
        })
    }

    class UsersAdapter(val data: List<Profile>, private val sharedModel: UserListViewModel): RecyclerView.Adapter<UsersAdapter.UserViewHolder>(){

        class UserViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val image = itemView.findViewById<ImageView>(R.id.imageUser)
            private val fullName = itemView.findViewById<TextView>(R.id.name)
            private val cv = itemView.findViewById<CardView>(R.id.card_view)

            fun bind(u: Profile, sharedModel: UserListViewModel) {

                fullName.text = u.fullName
                if (u.imageUrl != "") {
                    Picasso.get().load(u.imageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(image)
                } else image.setImageResource(R.drawable.avatar)

                cv.setOnClickListener {
                    sharedModel.selectedLocalUser = u
                    Navigation.findNavController(itemView).navigate(R.id.action_userList_to_showProfilePrivacy)
                }
            }

            fun unbind() {
                cv.setOnClickListener {  }
            }
        }

        override fun onViewRecycled(holder: UserViewHolder) {
            super.onViewRecycled(holder)
            holder.unbind()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val v= LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_user, parent,false)
            return UserViewHolder(v)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            holder.bind(data[position], sharedModel)
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}