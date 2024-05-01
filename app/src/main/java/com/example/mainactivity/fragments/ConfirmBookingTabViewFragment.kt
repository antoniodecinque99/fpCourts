package com.example.mainactivity.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mainactivity.R
import com.example.mainactivity.getLevelDescription
import com.example.mainactivity.getSportIcon
import com.example.mainactivity.getSportNames
import com.example.mainactivity.models.PersonSports
import com.example.mainactivity.viewmodel.ConfirmBookingViewModel
import com.example.mainactivity.viewmodel.ShowProfileViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class ConfirmBookingTabViewFragment() : Fragment() {
// When requested, this adapter returns a sportStatisticsFragment,
// representing an object in the collection.
    private lateinit var confirmBookingTabsAdapter: ConfirmBookingTabsAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_confirm_booking_tabs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        confirmBookingTabsAdapter = ConfirmBookingTabsAdapter(this)
        viewPager = view.findViewById(R.id.confirm_booking_pager)
        viewPager.adapter = confirmBookingTabsAdapter
        val tabLayout = view.findViewById<TabLayout>(R.id.confirm_booking_tab_layout)
        tabLayout.isInlineLabel = true
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            if (position == 0) {
                tab.text = "Details"
            } else {
                tab.text = "Teams"
            }
        }.attach()

        // vm.getSportsPerUser(0)
        /*vm.personSports.observe(viewLifecycleOwner) {
            personSports = it
            confirmBookingTabsAdapter = ConfirmBookingTabsAdapter(this, personSports)
            viewPager = view.findViewById(R.id.pager)
            viewPager.adapter = confirmBookingTabsAdapter
            val tabLayout = view.findViewById<TabLayout>(R.id.sportStatsTabLayout)
            tabLayout.isInlineLabel = true
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->

                tab.text = personSports[position].sport_name
                tab.setIcon(getSportIcon(personSports[position].sport_name))
            }.attach()
        }*/
    }
}


class ConfirmBookingTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment: Fragment
        if (position == 0) {
            fragment = ConfirmBookingDetailsFragment() // details
        } else {
            fragment = ConfirmBookingTeamsFragment() // teams
        }
        /*fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, position + 1)
            putInt("level", personSports[position].level)
            putInt("played", personSports[position].played)
            putInt("won", personSports[position].won)
            putInt("lost", personSports[position].lost)
            putInt("seen", personSports[position].seenUsers)
            putInt("organized", personSports[position].organized)
            putInt("planned", personSports[position].planned)
        }*/
        return fragment
    }
}

private const val ARG_OBJECT = "object"

// Instances of this class are fragments representing a single
// object in our collection.
class ConfirmBookingTeamsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_teams_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        /*arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            // here we modify the data for each sport.
            val level = getInt("level")
            val levelText: TextView = view.findViewById(R.id.levelNumber)
            levelText.text = level.toString()
            val progressBar: ProgressBar = view.findViewById(R.id.progressBar3)
            progressBar.progress = level
            val levelName: TextView = view.findViewById(R.id.levelText)
            levelName.text = getLevelDescription(level)

            val played: TextView = view.findViewById(R.id.statistics_played)
            played.text = getInt("played").toString()
            val won: TextView = view.findViewById(R.id.statistics_won)
            won.text = getInt("won").toString()
            val lost: TextView = view.findViewById(R.id.statistics_lost)
            lost.text = getInt("lost").toString()
            val seen: TextView = view.findViewById(R.id.statistics_met)
            seen.text = getInt("seen").toString()
            val organized: TextView = view.findViewById(R.id.statistics_organized)
            organized.text = getInt("organized").toString()
            val planned: TextView = view.findViewById(R.id.statistics_planned)
            planned.text = getInt("planned").toString()
        }*/
    }
}

class ConfirmBookingDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_details_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }
}

