package com.example.mainactivity.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.SwipeGesture
import com.example.mainactivity.adapters.InboxAdapter
import com.example.mainactivity.viewmodel.InboxViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class InboxActivity : AppCompatActivity() {
    private val vm by viewModels<InboxViewModel>()
    private lateinit var pendingList: RecyclerView
    private lateinit var adapter: InboxAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inbox)

        pendingList = findViewById(R.id.pendingList)
        adapter = InboxAdapter(vm)
        pendingList.adapter = adapter

        vm.loadPendings()
        vm.pendings.observe(this) { pendings ->
            val pendingEmptyStateIllustration =
                findViewById<ImageView>(R.id.pendingEmptyStateIllustration)
            val pendingEmptyText = findViewById<TextView>(R.id.pendingEmptyText)
            val pendingEmptyText2 = findViewById<TextView>(R.id.pendingEmptyText2)

            if (pendings.isEmpty()) {
                pendingEmptyStateIllustration.visibility = View.VISIBLE
                pendingEmptyText.visibility = View.VISIBLE
                pendingEmptyText2.visibility = View.VISIBLE
                pendingList.visibility = View.GONE
            } else {
                pendingEmptyStateIllustration.visibility = View.GONE
                pendingEmptyText.visibility = View.GONE
                pendingEmptyText2.visibility = View.GONE
                pendingList.visibility = View.VISIBLE
            }

            adapter.setPending(pendings)
        }

        val swipeGesture = object : SwipeGesture(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.getBindingAdapterPosition()
                val pendingItem = adapter.getPendingItem(position)

                if (pendingItem.status == "pending") {
                    // Status is pending, do not dismiss
                    return
                }

                // Capture the position before any updates
                val capturedPosition = position

                lateinit var oldStatus: String
                lateinit var newStatus: String
                if (pendingItem.status == "accepted") {
                    oldStatus = "accepted"
                    pendingItem.status = "dismissed_accepted"
                    newStatus = "dismissed_accepted"
                } else if (pendingItem.status == "rejected") {
                    oldStatus = "rejected"
                    pendingItem.status = "dismissed_rejected"
                    newStatus = "dismissed_rejected"
                }

                vm.updatePendingStatus(pendingItem.booking_id!!, newStatus) {
                    vm.loadPendings()
                }

                val snackBar = Snackbar.make(
                    pendingList, "Pending item deleted", Snackbar.LENGTH_LONG
                ).addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        super.onDismissed(transientBottomBar, event)
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        transientBottomBar?.setAction("UNDO") {
                            // Use the captured position here
                            vm.updatePendingStatus(pendingItem.booking_id!!, oldStatus) {
                                vm.loadPendings()
                            }

                            // Scroll to the captured position after undo
                            pendingList.scrollToPosition(capturedPosition)
                        }
                        super.onShown(transientBottomBar)
                    }
                }).apply {
                    animationMode = Snackbar.ANIMATION_MODE_FADE
                }

                snackBar.setActionTextColor(resources.getColor(R.color.red))
                snackBar.show()
            }
        }

        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(pendingList)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation5)
        bottomNavigation.setSelectedItemId(R.id.page_Inbox)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.page_profile -> {
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
                R.id.page_courtsList -> {
                    val intent = Intent(this, CourtsListActivity::class.java)
                    overridePendingTransition(0, 0)
                    startActivity(intent)
                    finish()
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
}
