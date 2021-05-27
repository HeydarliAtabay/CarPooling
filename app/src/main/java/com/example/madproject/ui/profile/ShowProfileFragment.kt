package com.example.madproject.ui.profile

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.example.madproject.data.Rating
import com.example.madproject.ui.comments.RatingsViewModel
import com.example.madproject.ui.yourtrips.TripListViewModel
import com.example.madproject.ui.yourtrips.interestedusers.UserListViewModel
import com.squareup.picasso.Picasso

class ShowProfileFragment : Fragment() {

    private lateinit var fullName : TextView
    private lateinit var nickName : TextView
    private lateinit var dateOfBirth : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var location : TextView
    private lateinit var image : ImageView
    private lateinit var passengerRatingView: TextView
    private lateinit var driverRatingView: TextView
    private lateinit var driverRatingsButton: ImageButton
    private lateinit var passengerRatingsButton: ImageButton
    private var profile: Profile = Profile()
    private var passengerRatings: List<Rating> = listOf()
    private var driverRatings: List<Rating> = listOf()
    private val profileModel: ProfileViewModel by activityViewModels()
    private val userListModel: UserListViewModel by activityViewModels()
    private val tripListModel: TripListViewModel by activityViewModels()
    private val ratingsModel: RatingsViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?):View?{

        // If the flag "comingFromPrivacy" is true it is inflated the layout to show only limited
        // user information. If the flag is false it is loaded the full layout to show the information
        // of the current user

        return if (profileModel.comingFromPrivacy)
            inflater.inflate(R.layout.privacy_show_profile, container, false)
        else
            inflater.inflate(R.layout.fragment_show_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        email = view.findViewById(R.id.email)
        image = view.findViewById(R.id.imageView3)
        passengerRatingView = view.findViewById(R.id.passengerRating)
        driverRatingView = view.findViewById(R.id.driverRating)
        driverRatingsButton = view.findViewById(R.id.driverComments)
        passengerRatingsButton = view.findViewById(R.id.passengerComments)

        // Load these information only if it is shown the current user profile
        if (!profileModel.comingFromPrivacy) {
            // Reset the flag that manages the tab selection in "your trips", after going to this page from navigation drawer
            tripListModel.tabCompletedTrips = false
            dateOfBirth = view.findViewById(R.id.dateOfBirth)
            phoneNumber = view.findViewById(R.id.phoneNumber)
            location = view.findViewById(R.id.location)
        }

        // Depending on the user to load, get the data from the right viewModel
        if (profileModel.comingFromPrivacy) {

            // Navigate to the comments page when click on the arrow, selecting which ratings to load
            driverRatingsButton.setOnClickListener {
                ratingsModel.showDriverRatings = true
                findNavController().navigate(R.id.action_showProfilePrivacy_to_comments)
            }

            passengerRatingsButton.setOnClickListener {
                ratingsModel.showDriverRatings = false
                findNavController().navigate(R.id.action_showProfilePrivacy_to_comments)
            }

            // Set the new user in ratings view model in order to load his ratings
            ratingsModel.selectUser(userListModel.selectedLocalUserEmail)
            userListModel.getSelectedDB().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    profile = it
                    setProfile()
                }
            })
        } else {

            // Navigate to the comments page when click on the arrow, selecting which ratings to load
            driverRatingsButton.setOnClickListener {
                ratingsModel.showDriverRatings = true
                findNavController().navigate(R.id.action_showProfile_to_comments)
            }

            passengerRatingsButton.setOnClickListener {
                ratingsModel.showDriverRatings = false
                findNavController().navigate(R.id.action_showProfile_to_comments)
            }

            // Set the new user in ratings view model in order to load his ratings
            ratingsModel.selectUser(profileModel.getDBUser().value?.email ?: "")
            profileModel.getDBUser().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    profile = it
                    setProfile()
                }
            })
        }

        ratingsModel.getDriverRatings().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                driverRatings = it
                setProfile()
            }
        })

        ratingsModel.getPassengerRatings().observe(viewLifecycleOwner, {
            if (it == null) {
                Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
            } else {
                passengerRatings = it
                setProfile()
            }
        })

        setProfile()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (!profileModel.comingFromPrivacy)
            inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                profileModel.localProfile = profile
                profileModel.useDBImage = true
                findNavController().navigate(R.id.action_showProfile_to_editProfile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setProfile(){
        if (profile.imageUrl != "") {
            Picasso.get().load(profile.imageUrl).placeholder(R.drawable.avatar).error(R.drawable.avatar).into(image)
        } else image.setImageResource(R.drawable.avatar)

        fullName.text = profile.fullName
        nickName.text = profile.nickName
        email.text = profile.email

        if (!profileModel.comingFromPrivacy) {
            dateOfBirth.text = profile.dateOfBirth
            phoneNumber.text = profile.phoneNumber
            location.text = profile.location
        }

        // Load ratings grade
        driverRatingView.text =
            if (driverRatings.isEmpty())
                getString(R.string.no_ratings)
            else
                getString(R.string.show_ratings, String.format(" % .1f", driverRatings.map { it.rating }.average()))
        passengerRatingView.text =
            if (passengerRatings.isEmpty())
                getString(R.string.no_ratings)
            else
                getString(R.string.show_ratings, String.format(" % .1f", passengerRatings.map { it.rating }.average()))
    }
}