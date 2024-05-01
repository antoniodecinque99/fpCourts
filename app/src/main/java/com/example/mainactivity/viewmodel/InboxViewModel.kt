package com.example.mainactivity.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.animation.core.snap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.models.FireBookingCourtPending
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InboxViewModel (application: Application): AndroidViewModel(Application()) {
    private val db = Firebase.firestore
    val pendings = MutableLiveData<List<FireBookingCourtPending>>()

    fun loadPendings() {
        val pendingList = mutableListOf<FireBookingCourtPending>()

        db.collection("pendings")
            .whereEqualTo("user_email", LoggedInUser.user.email)
            .addSnapshotListener { pendingQuerySnapshot, _ ->
                pendingQuerySnapshot?.let { snapshot ->
                    pendingList.clear()

                    if (snapshot.isEmpty) {
                        pendings.postValue(listOf())
                        return@addSnapshotListener
                    }

                    val sortedList = mutableListOf<FireBookingCourtPending>()

                    var pendingCount = 0
                    for (document in snapshot) {
                        val status = document.getString("status")
                        val seen = document.getBoolean("seen")

                        if (status != "dismissed_rejected" && status != "dismissed_accepted") {
                            val bookingId = document.getLong("booking_id")
                            val team = document.getString("team")

                            db.collection("bookings")
                                .whereEqualTo("booking_id", bookingId)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { bookingQuerySnapshot ->
                                    if (!bookingQuerySnapshot.isEmpty) {
                                        val bookingDocument = bookingQuerySnapshot.documents[0]
                                        val courtId = bookingDocument.getLong("court_id")
                                        val date = bookingDocument.getString("date")
                                        val timeSlotId = bookingDocument.getLong("time_slot_id")
                                        val userEmailOrganizer =
                                            bookingDocument.getString("user_email")

                                        db.collection("courts")
                                            .whereEqualTo("court_id", courtId)
                                            .limit(1)
                                            .get()
                                            .addOnSuccessListener { courtQuerySnapshot ->
                                                val courtDocument = courtQuerySnapshot.documents[0]
                                                val courtName = courtDocument.getString("name")
                                                val courtSport = courtDocument.getString("sport")
                                                val courtAddress =
                                                    courtDocument.getString("address")
                                                val courtPrice = courtDocument.getDouble("price")


                                                val fireBookingCourtPending =
                                                    FireBookingCourtPending()
                                                fireBookingCourtPending.booking_id = bookingId
                                                fireBookingCourtPending.user_email_organizer =
                                                    userEmailOrganizer
                                                fireBookingCourtPending.court_id = courtId
                                                fireBookingCourtPending.date = date
                                                fireBookingCourtPending.time_slot_id = timeSlotId
                                                fireBookingCourtPending.court_name = courtName
                                                fireBookingCourtPending.court_sport = courtSport
                                                fireBookingCourtPending.court_address = courtAddress
                                                fireBookingCourtPending.court_price = courtPrice
                                                fireBookingCourtPending.status = status
                                                fireBookingCourtPending.team = team
                                                fireBookingCourtPending.seen = seen

                                                sortedList.add(fireBookingCourtPending)

                                                pendingCount++
                                                if (pendingCount == snapshot.size()) {
                                                    val sortedAndFiltered =
                                                        sortedList.sortedWith(
                                                            compareBy<FireBookingCourtPending> { getStatusOrder(it.status) }
                                                                .thenByDescending { it.date }.reversed()
                                                                .thenByDescending { it.time_slot_id }.reversed()
                                                        )
                                                    pendings.postValue(sortedAndFiltered)
                                                }
                                            }
                                    }
                                }
                        } else {
                            pendingCount++
                            if (pendingCount == snapshot.size()) {
                                val sortedAndFiltered =
                                    sortedList.sortedWith(
                                        compareBy<FireBookingCourtPending> { getStatusOrder(it.status) }
                                            .thenByDescending { it.date }.reversed()
                                            .thenByDescending { it.time_slot_id }.reversed()
                                    )

                                pendings.postValue(sortedAndFiltered)
                            }
                        }
                    }
                }
            }
    }

    // Function to determine the order of status values
    fun getStatusOrder(status: String?): Int {
        return when (status) {
            "pending" -> 0
            "accepted" -> 1
            "rejected" -> 2
            else -> Int.MAX_VALUE // Fallback for unknown statuses
        }
    }


    fun updatePendingStatus(bookingId: Long, status: String, onComplete: () -> Unit) {
        db.collection("pendings")
            .whereEqualTo("booking_id", bookingId)
            .whereEqualTo("user_email", LoggedInUser.user.email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                db.collection("pendings")
                    .document(querySnapshot.documents[0].id)
                    .update("status", status)
                    .addOnSuccessListener {
                        onComplete()
                    }
            }
    }
}