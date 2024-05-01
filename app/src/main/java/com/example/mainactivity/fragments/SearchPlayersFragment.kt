package com.example.mainactivity.fragments

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.example.mainactivity.R
import com.example.mainactivity.activities.ConfirmBookingActivity
import com.example.mainactivity.adapters.InvitationAdapter
import com.example.mainactivity.setBackgroundColorRes
import com.example.mainactivity.setTextColorRes
import com.example.mainactivity.viewmodel.ConfirmBookingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL


class SearchPlayersFragment(val team: String, val position: Int): Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_search_players, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val vm = ViewModelProvider(requireActivity())[ConfirmBookingViewModel::class.java]


        val inviteButton = view.findViewById<Button>(R.id.inviteButton)
        inviteButton.isClickable = false
        inviteButton.alpha = 0.5F


        // setup recycler view
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewAddInvitations)
        val adapter = InvitationAdapter(vm, inviteButton)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)


        inviteButton.setOnClickListener {
            val user = adapter.data[0]
            Log.d("a", user.email)
            vm.updateBooking(user.email)
            Toast.makeText(it.context,"Sent an invite to " + user.name, Toast.LENGTH_SHORT).show()
            val searchText = view.findViewById<TextView>(R.id.editTextSearch)
            searchText.text = ""
            vm.clearUserSearch()
            inviteButton.isClickable = false
            inviteButton.alpha = 0.5F
            // destroy me
            (activity as ConfirmBookingActivity).updateTeam(team, position, user)
            activity?.run {
                supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                ).remove(this@SearchPlayersFragment)
                    .commitAllowingStateLoss()
            }

        }


        // set close button functionality
        val closeButton = view.findViewById<ImageButton>(R.id.closeSearchButton)
        closeButton.setOnClickListener {
            // clear text
            val searchText = view.findViewById<TextView>(R.id.editTextSearch)
            searchText.text = ""
            vm.clearUserSearch()
            // destroy me
            activity?.run {
                supportFragmentManager.beginTransaction().setCustomAnimations(
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom,
                    R.anim.slide_in_bottom,
                    R.anim.slide_out_bottom
                ).remove(this@SearchPlayersFragment)
                    .commitAllowingStateLoss()
            }
        }


        val searchText = view.findViewById<EditText>(R.id.editTextSearch)

        searchText.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (searchText.text.toString().length >= 2) {
                    vm.getUserByNick(searchText.text.toString())
                } else {
                    vm.clearUserSearch()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        vm.users_.observe(viewLifecycleOwner) { it1 ->
            adapter.setUsers(it1)
        }

    }
}
