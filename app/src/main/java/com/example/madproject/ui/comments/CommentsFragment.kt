package com.example.madproject.ui.comments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.madproject.R
import com.example.madproject.data.Rating
import com.example.madproject.lib.DividerItemDecorator

class CommentsFragment : Fragment(R.layout.fragment_comments) {

    private var ratingsList = listOf<Rating>()
    private lateinit var emptyList: TextView
    private lateinit var ratingsTitle: TextView
    private lateinit var tvRating: TextView
    private lateinit var totalRatings: TextView
    private lateinit var ratingBar: RatingBar
    private lateinit var recyclerView: RecyclerView
    private val ratingsModel: RatingsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        emptyList = view.findViewById(R.id.emptyComments)
        ratingsTitle = view.findViewById(R.id.ratingTitle)
        tvRating = view.findViewById(R.id.tvRating)
        totalRatings = view.findViewById(R.id.totalRatings)
        ratingBar = view.findViewById(R.id.ratingShow)
        recyclerView = view.findViewById(R.id.rvComments)

        ratingsTitle.visibility = View.INVISIBLE
        tvRating.visibility = View.INVISIBLE
        totalRatings.visibility = View.INVISIBLE
        ratingBar.visibility = View.INVISIBLE

        // Custom divider created by the extended class in MyFunctions.kt
        val divider = DividerItemDecorator(ContextCompat.getDrawable(requireContext(), R.drawable.divider)!!)

        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(3)
        recyclerView.addItemDecoration(divider)
        recyclerView.layoutManager = LinearLayoutManager(this.requireActivity())

        if (ratingsModel.showDriverRatings) {
            ratingsTitle.text = getString(R.string.ratings_title, "Driver")

            ratingsModel.getDriverRatings().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    ratingsList = it
                    setSelectedList()
                }
            })

        } else {
            ratingsTitle.text = getString(R.string.ratings_title, "Passenger")

            ratingsModel.getPassengerRatings().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    ratingsList = it
                    setSelectedList()
                }
            })
        }
    }

    private fun setSelectedList() {
        if (ratingsList.isEmpty()) {
            ratingsTitle.visibility = View.INVISIBLE
            tvRating.visibility = View.INVISIBLE
            totalRatings.visibility = View.INVISIBLE
            ratingBar.visibility = View.INVISIBLE
            emptyList.visibility = View.VISIBLE
        } else {

            val ratingAv = ratingsList.map { it.rating }.average()

            tvRating.text =
                getString(R.string.show_ratings,
                    String.format(" % .1f", ratingAv))

            totalRatings.text =
                getString(R.string.num_ratings, ratingsList.count())

            ratingBar.rating = ratingAv.toFloat()

            ratingsTitle.visibility = View.VISIBLE
            tvRating.visibility = View.VISIBLE
            totalRatings.visibility = View.VISIBLE
            ratingBar.visibility = View.VISIBLE
            emptyList.visibility = View.INVISIBLE

            // Create the recyclerView
            recyclerView.adapter = CommentsAdapter(ratingsList.sortedByDescending { it.rating })
        }
    }

    class CommentsAdapter(val data: List<Rating>): RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>(){

        class CommentViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
            private val username = itemView.findViewById<TextView>(R.id.usernameCom)
            private val ratingBar = itemView.findViewById<RatingBar>(R.id.ratingShowCom)
            private val ratingText = itemView.findViewById<TextView>(R.id.ratingShowText)
            private val comment = itemView.findViewById<TextView>(R.id.commentText)

            /*
            Populate the card view of each comment instance
             */
            fun bind(r: Rating) {
                username.text = r.nickName
                ratingBar.rating = r.rating
                ratingText.text = itemView.context.getString(R.string.show_ratings, r.rating.toString())
                comment.text = r.comment
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val v= LayoutInflater.from(parent.context)
                .inflate(R.layout.recyclerview_card_comment, parent,false)
            return CommentViewHolder(v)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(data[position])
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }
}