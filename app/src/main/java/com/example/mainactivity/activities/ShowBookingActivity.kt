package com.example.mainactivity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.fragments.CalendarSheetFragment
import com.example.mainactivity.R
import com.example.mainactivity.adapters.ShowBookingAdapter
import com.example.mainactivity.convertDisplayToDate
import com.example.mainactivity.convertDateToDisplay
import com.example.mainactivity.viewmodel.ShowBookingViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShowBookingActivity : AppCompatActivity() {
    val vm by viewModels<ShowBookingViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_booking)
        var toolbar = findViewById<Toolbar>(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar)

        val calendarSheetFragment = CalendarSheetFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCalendarSheet, calendarSheetFragment)
            .commit()

        val recyclerView = findViewById<RecyclerView>(R.id.bookingsList)
        val adapter = ShowBookingAdapter(vm)

        recyclerView.adapter = adapter
        vm.getBookings(LocalDate.now())

        vm.bookingCourts.observe(this) {
            adapter.setTodayBooking(it)

            if (it.isEmpty()) {
                recyclerView.visibility = View.GONE
                findViewById<TextView>(R.id.noBookingsText).visibility = View.VISIBLE
                findViewById<TextView>(R.id.pendingEmptyText2).visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                findViewById<TextView>(R.id.noBookingsText).visibility = View.GONE
                findViewById<TextView>(R.id.pendingEmptyText2).visibility = View.GONE
            }

        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        findViewById<TextView>(R.id.todayText).setText(convertDateToDisplay(LocalDate.now().format(dateFormatter), false))

        val addBooking = findViewById<Button>(R.id.addBooking)
        addBooking.setOnClickListener {
            //Log.d("date0", findViewById<TextView>(R.id.todayText).text.toString())
            val intent = Intent(this, BookingActivity::class.java)
                .putExtra("current_date", convertDisplayToDate(findViewById<TextView>(R.id.todayText).text.toString()))
            startActivity(intent)
        }

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation2)
        bottomNavigation.setSelectedItemId(R.id.page_showBooking)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.page_profile-> {
                    val intent = Intent(this, ShowProfileActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    true
                }
                R.id.page_courtsList -> {
                    val intent = Intent(this, CourtsListActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    true
                }
                R.id.page_Inbox -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        val db = FirebaseFirestore.getInstance()
        val userEmail = LoggedInUser.user.email

        // Create a query to fetch the documents that satisfy the conditions
        val query = db.collection("pendings")
            .whereEqualTo("user_email", userEmail)
            .whereEqualTo("seen", false)

        // Create a listener registration variable
        var registration: ListenerRegistration? = null

        // Function to update the notification count
        fun updateNotificationCount(snapshot: QuerySnapshot?) {
            val count = snapshot?.size() ?: 0

            // Update the text view with the count
            val notificationCount = findViewById<TextView>(R.id.notification_count)
            notificationCount.text = count.toString()

            // Set the visibility of the text view based on the count
            if (count > 0) {
                notificationCount.visibility = View.VISIBLE
            } else {
                notificationCount.visibility = View.GONE
            }
        }

        // Register a listener to observe real-time changes
        registration = query.addSnapshotListener { snapshot, e ->
            if (e != null) {
                // Handle any errors that occurred
                return@addSnapshotListener
            }

            updateNotificationCount(snapshot)
        }

        // Fetch initial notification count
        query.get().addOnSuccessListener { snapshot ->
            updateNotificationCount(snapshot)
        }
    }

    override fun onResume() {
        super.onResume()
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation2)
        val menu = bottomNavigation.menu
        val desiredItemId = R.id.page_showBooking
        val desiredItem = menu.findItem(desiredItemId)
        desiredItem?.isChecked = true
    }

}