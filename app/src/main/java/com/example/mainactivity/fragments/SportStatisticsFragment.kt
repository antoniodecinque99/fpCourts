package com.example.mainactivity.fragments

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.getLevelDescription
import com.example.mainactivity.getSportIcon
import com.example.mainactivity.viewmodel.ShowProfileViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class CollectionSportStatisticsFragment() : Fragment() {
// When requested, this adapter returns a sportStatisticsFragment,
// representing an object in the collection.
    private lateinit var sportStatisticsCollectionAdapter: SportStatisticsCollectionAdapter
    private lateinit var viewPager: ViewPager2
    private var userStatistics: HashMap<String, HashMap<String, Int>> = hashMapOf()
    private val vm by viewModels<ShowProfileViewModel>()
    private val user = LoggedInUser.user


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_collection_sport_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //vm.userStatistics.postValue(user.statistics)
        vm.getUser(user.email)
        vm.userStatistics.observe(viewLifecycleOwner) {
            userStatistics = it
            val sports = userStatistics.keys.toList()
            val sportsStatistics = userStatistics.values.toList()
            sportStatisticsCollectionAdapter = SportStatisticsCollectionAdapter(this, sportsStatistics)
            viewPager = view.findViewById(R.id.pager)
            viewPager.adapter = sportStatisticsCollectionAdapter
            val tabLayout = view.findViewById<TabLayout>(R.id.sportStatsTabLayout)
            tabLayout.isInlineLabel = true
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = sports[position]
                tab.setIcon(getSportIcon(sports[position]))
            }.attach()
        }
    }
}


class SportStatisticsCollectionAdapter(fragment: Fragment, userSports: List<HashMap<String, Int>>) : FragmentStateAdapter(fragment) {

    val userStatistics = userSports
    override fun getItemCount(): Int = userStatistics.size

    override fun createFragment(position: Int): Fragment {
        // Return a NEW fragment instance in createFragment(int)
        val fragment = SportStatisticsFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_OBJECT, position + 1)
            userStatistics[position]["level"]?.let { putInt("level", it) }
            userStatistics[position]["played"]?.let { putInt("played", it) }
            userStatistics[position]["seen"]?.let { putInt("seen", it) }
            userStatistics[position]["organized"]?.let { putInt("organized", it) }
            userStatistics[position]["planned"]?.let { putInt("planned", it) }
        }
        return fragment
    }
}

private const val ARG_OBJECT = "object"

// Instances of this class are fragments representing a single
// object in our collection.
class SportStatisticsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_sport_statistics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_OBJECT) }?.apply {
            // here we modify the data for each sport.
            val level = getInt("level")
            val levelText: TextView = view.findViewById(R.id.levelNumber)
            levelText.text = level.toString()
            val progressBar: ProgressBar = view.findViewById(R.id.progressBar3)
            progressBar.progressTintList = ColorStateList.valueOf(resources.getColor(R.color.red))
            progressBar.progress = level
            val levelName: TextView = view.findViewById(R.id.levelText)
            levelName.text = getLevelDescription(level)

            val played: TextView = view.findViewById(R.id.statistics_played)
            played.text = getInt("played").toString()
            val seen: TextView = view.findViewById(R.id.statistics_met)
            seen.text = getInt("seen").toString()
            val organized: TextView = view.findViewById(R.id.statistics_organized)
            organized.text = getInt("organized").toString()
            val planned: TextView = view.findViewById(R.id.statistics_planned)
            planned.text = getInt("planned").toString()
        }
    }
}

