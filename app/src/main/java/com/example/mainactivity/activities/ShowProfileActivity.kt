package com.example.mainactivity.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.Storage
import com.example.mainactivity.adapters.MySportsAdapter
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.viewmodel.ShowProfileViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


class ShowProfileActivity : AppCompatActivity() {

    val user = LoggedInUser.user
    val auth = Firebase.auth
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val vm by viewModels<ShowProfileViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        vm.getUser(user.email)
        vm.user.observe(this) {
            loadData(it)
            Storage.getProfilePic(user.email) {pic ->
                if (pic != null) {
                    vm.setPicture(pic)
                }
            }
        }
        vm.picture.observe(this) {
            val pic = findViewById<ImageView>(R.id.imageView)
            pic.setImageBitmap(it)
        }


        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigation.selectedItemId = R.id.page_profile
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_showBooking -> {
                    overridePendingTransition(0, 0)
                    finish()
                    true
                }
                R.id.page_courtsList -> {
                    val intent = Intent(this, CourtsListActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
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

        val imageButton = findViewById<Button>(R.id.edit_profile)
        imageButton.setOnClickListener {
            val editPage = Intent(this, EditProfileActivity::class.java)
            startActivity(editPage)
            finish()
        }

        //handle logout
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.logoutButton2).setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }

        // setup RecyclerView for My Sports section
        val recyclerView: RecyclerView = findViewById(R.id.mySportsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.isNestedScrollingEnabled = false

        // observe sport data
        var userSportNames: List<String> = mutableListOf()
        var userLevels: List<Int> = mutableListOf()
        vm.userStatistics.observe(this) { it1 ->
            it1.forEach {entry ->
                userSportNames+=entry.key
                userLevels+=(entry.value["level"] ?: 0)
            }

            val mySportsAdapter = MySportsAdapter(userSportNames, userLevels)
            recyclerView.adapter = mySportsAdapter

            if (it1.isEmpty()) {
                findViewById<LinearLayout>(R.id.profileStatistics).visibility = View.GONE
                findViewById<ConstraintLayout>(R.id.noStatisticsLayout).visibility = View.VISIBLE
            } else {
                findViewById<LinearLayout>(R.id.profileStatistics).visibility = View.VISIBLE
                findViewById<ConstraintLayout>(R.id.noStatisticsLayout).visibility = View.GONE
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
        loadData(user)
    }

    private fun loadData(fireUser: FireUser) {
        val name = findViewById<TextView>(R.id.name)
        val age = findViewById<TextView>(R.id.age)
        val loc = findViewById<TextView>(R.id.location)
        val nick = findViewById<TextView>(R.id.nickname)
        val description = findViewById<TextView>(R.id.description)

        val currentDate = LocalDate.now()
        val birthDate = LocalDate.parse(fireUser.birthDate, dateFormatter)

        val years = ChronoUnit.YEARS.between(birthDate, currentDate).toString()

        name.setText(fireUser.name)
        age.setText(years)
        loc.setText(fireUser.location)
        nick.setText(fireUser.nickname)
        description.setText(fireUser.description)
    }

    private fun loadPicture(_url: String) {
        Thread {
            val url = URL(_url)
            val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
            vm.setPicture(bitmap)
        }.start()
    }
}