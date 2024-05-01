package com.example.mainactivity.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.models.FireBooking
import com.example.mainactivity.models.FireBookingCourt
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate

class ShowBookingViewModel(application: Application): AndroidViewModel(application) {
    private var db = Firebase.firestore
    val bookingCourts = MutableLiveData<List<FireBookingCourt>>()
    val booking = MutableLiveData<List<FireBooking>>()

    fun getBookings(today: LocalDate) {
        val currentUserEmail = LoggedInUser.user.email
        db.collection("bookings")
            .whereEqualTo("date", today.toString())
            .addSnapshotListener { querySnapshot, _ ->

                if (querySnapshot!!.documents.isEmpty()) {
                    bookingCourts.postValue(mutableListOf())
                    return@addSnapshotListener
                }

                val queryResults = querySnapshot.documents.filter { document ->
                    val teamsMap = document["teams"] as? Map<String, String>

                    (teamsMap?.containsKey(currentUserEmail) == true &&
                            (teamsMap[currentUserEmail] == "blackTeam" ||
                                    teamsMap[currentUserEmail] == "whiteTeam")) ||
                            document["user_email"] == currentUserEmail
                }

                if (queryResults.isEmpty()) {
                    bookingCourts.postValue(mutableListOf())
                }

                val updatedBookingCourts = mutableListOf<FireBookingCourt>()
                queryResults.forEach { bookingDoc ->
                    val bookingData = bookingDoc.data
                    val courtId = bookingData?.get("court_id") as Long

                    db.collection("courts")
                        .whereEqualTo("court_id", courtId)
                        .get()
                        .addOnSuccessListener { courtDoc ->
                            val courtData = courtDoc.documents.first().data!!

                            val bookingCourt = FireBookingCourt(
                                bookingData["booking_id"] as Long,
                                bookingData["user_email"] as String,
                                bookingData["user_request"] as String,
                                bookingData["court_id"] as Long,
                                bookingData["date"] as String,
                                bookingData["time_slot_id"] as Long,
                                bookingData["teams"] as? Map<String, String>,
                                courtData["name"] as String,
                                courtData["sport"] as String,
                                courtData["address"] as String,
                                (courtData["price"].toString()).toDouble()
                            )

                            updatedBookingCourts.add(bookingCourt)

                            // Check if all bookings have been fetched
                            if (updatedBookingCourts.size == queryResults.size) {
                                bookingCourts.postValue(updatedBookingCourts.sortedBy { it.time_slot_id }.sortedBy { it.court_name })
                            }
                        }
                }
            }
    }


    fun getAllBookings() {
        db.collection("bookings")
            .addSnapshotListener { querySnapshot, _ ->
                val currentUserEmail = LoggedInUser.user.email

                val filteredQueryResults = querySnapshot!!.documents.filter { document ->
                    val teamsMap = document["teams"] as? Map<String, String>
                    (teamsMap?.containsKey(currentUserEmail) == true &&
                            (teamsMap[currentUserEmail] == "blackTeam" ||
                                    teamsMap[currentUserEmail] == "whiteTeam")) ||
                            document["user_email"] == currentUserEmail
                }

                val updatedBookingList = mutableListOf<FireBooking>()

                filteredQueryResults.forEach { document ->
                    val booking = document.toObject(FireBooking::class.java)
                    updatedBookingList.add(booking!!)
                }

                booking.postValue(updatedBookingList)
            }
    }

}