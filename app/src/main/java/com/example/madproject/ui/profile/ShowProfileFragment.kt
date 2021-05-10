package com.example.madproject.ui.profile

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.madproject.R
import com.example.madproject.data.Profile
import com.example.madproject.ui.trips.interestedusers.UserListViewModel
import com.squareup.picasso.Picasso

class ShowProfileFragment : Fragment() {

    private lateinit var fullName : TextView
    private lateinit var nickName : TextView
    private lateinit var dateOfBirth : TextView
    private lateinit var email : TextView
    private lateinit var phoneNumber : TextView
    private lateinit var location : TextView
    private lateinit var image : ImageView
    private var profile: Profile = Profile()
    private val model: ProfileViewModel by activityViewModels()
    private val listModel: UserListViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?):View?{

        if (model.comingFromPrivacy) {
            return inflater.inflate(R.layout.privacy_show_profile, container, false)
        }

        return inflater.inflate(R.layout.fragment_show_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        fullName = view.findViewById(R.id.fullName)
        nickName = view.findViewById(R.id.nickName)
        email = view.findViewById(R.id.email)
        image = view.findViewById(R.id.imageView3)

        if (!model.comingFromPrivacy) {
            dateOfBirth = view.findViewById(R.id.dateOfBirth)
            phoneNumber = view.findViewById(R.id.phoneNumber)
            location = view.findViewById(R.id.location)
        }

        if (model.comingFromPrivacy) {
            listModel.getSelectedDB().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    profile = it
                    setProfile()
                }
            })
        } else {
            model.getDBUser().observe(viewLifecycleOwner, {
                if (it == null) {
                    Toast.makeText(context, "Firebase Failure!", Toast.LENGTH_LONG).show()
                } else {
                    profile = it
                    setProfile()
                }
            })
        }
        setProfile()

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (!model.comingFromPrivacy)
            inflater.inflate(R.menu.edit_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.editButton -> {
                model.localProfile = profile
                model.useDBImage = true
                findNavController().navigate(R.id.action_showProfile_to_editProfile)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setProfile(){
        fullName.text = profile.fullName
        nickName.text = profile.nickName
        email.text = profile.email

        if (!model.comingFromPrivacy) {
            dateOfBirth.text = profile.dateOfBirth
            phoneNumber.text = profile.phoneNumber
            location.text = profile.location
        }

        if (profile.imageUrl != "") {
            Picasso.get().load(profile.imageUrl).error(R.drawable.avatar).into(image)
        } else image.setImageResource(R.drawable.avatar)
    }
}