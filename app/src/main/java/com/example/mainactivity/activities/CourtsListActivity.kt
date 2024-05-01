package com.example.mainactivity.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.adapters.CourtsListAdapter
import com.example.mainactivity.models.*
import com.example.mainactivity.viewmodel.CourtsListViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class CourtsListActivity : AppCompatActivity() {

    val vm by viewModels<CourtsListViewModel>()

    lateinit var db: BookingDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courts_list)

        db = BookingDatabase.getDatabase(this)


        val list = findViewById<RecyclerView>(R.id.courtsList)
        val adapter = CourtsListAdapter(vm)
        list.adapter = adapter

        vm.courts.observe(this) {
            adapter._setCourts(it)
        }

        vm.showCourtInfoId.observe(this) {
            val i = Intent(this, CourtDetailsActivity::class.java)
                .putExtra("court_id", it)
            startActivity(i)
        }


        val sportButton = findViewById<Button>(R.id.selectSportButton)
        val popupMenu = PopupMenu(this, sportButton)
        popupMenu.menuInflater.inflate(R.menu.popupmenu, popupMenu.menu)
        val resetButton = findViewById<ImageView>(R.id.resetSport)
        resetButton.setOnClickListener {
            vm.loadCourts(null)
            sportButton.setText("Select sport")
            resetButton.visibility = View.INVISIBLE

        }
        sportButton.setOnClickListener {
            popupMenu.show()
        }
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.item1 -> {
                    sportButton.setText("Football")
                    vm.getCourtBySport("Football")
                    resetButton.visibility = View.VISIBLE
                    true
                }
                R.id.item2 -> {
                    sportButton.setText("Tennis")
                    vm.getCourtBySport("Tennis")
                    resetButton.visibility = View.VISIBLE
                    true
                }
                R.id.item3 -> {
                    sportButton.setText("Padel")
                    vm.getCourtBySport("Padel")
                    resetButton.visibility = View.VISIBLE
                    true
                }
                R.id.item4 -> {
                    sportButton.setText("Volleyball")
                    vm.getCourtBySport("Volleyball")
                    resetButton.visibility = View.VISIBLE
                    true
                }
                R.id.item5 -> {
                    sportButton.setText("Basketball")
                    vm.getCourtBySport("Basketball")
                    resetButton.visibility = View.VISIBLE
                    true
                }
                else -> false
            }
        }

        vm.loadCourts(null)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation4)
        bottomNavigation.setSelectedItemId(R.id.page_courtsList)
        bottomNavigation.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.page_profile-> {
                    val intent = Intent(this, ShowProfileActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.page_showBooking -> {
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.page_Inbox -> {
                    val intent = Intent(this, InboxActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }

        findViewById<ImageButton>(R.id.courtsList_backButton).setOnClickListener {
            finish()
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

    }
}