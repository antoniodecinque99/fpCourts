package com.example.mainactivity.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.models.FireBooking
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.today
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ShowProfileViewModel(application: Application): AndroidViewModel(application) {
    val userStatistics = MutableLiveData<HashMap<String, HashMap<String, Int>>>()
    val picture = MutableLiveData<Bitmap>()
    val user = MutableLiveData<FireUser>()
    val db = Firebase.firestore
    val loggedInUser = LoggedInUser.user


    fun getUser(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener {
                val fireUser = it.first().toObject(FireUser::class.java)
                this.user.postValue(fireUser)
                getStatistics(fireUser.statistics.keys.toList(), fireUser)
            }
    }

    private fun getStatistics(sports: List<String>, user: FireUser) {
        var played: Int
        var planned: Int
        var seen: Int
        var organized: Int

        val totalStatisticsToCompute = sports.size*4
        var computedStatistics = 0

        val statistics = hashMapOf<String, HashMap<String, Int>>()
        if(sports.isEmpty()) this.userStatistics.postValue(statistics)

        for(sport in sports) {
            played = 0
            planned = 0
            organized = 0
            seen = 0
            val level = user.statistics[sport]?.get("level") ?: 0

            statistics[sport] = hashMapOf()
            statistics[sport]?.set("level", level)
            statistics[sport]?.set("played", 0)
            statistics[sport]?.set("planned", 0)
            statistics[sport]?.set("organized", 0)
            statistics[sport]?.set("seen", 0)

            //compute played
            db.collection("bookings")
                .whereLessThan("date", today())
                .get()
                .addOnSuccessListener { querySnapshot ->

                    val queryResults = querySnapshot!!.documents.filter { document ->
                        val teamsMap = document["teams"] as? Map<String, String>

                        (teamsMap?.containsKey(loggedInUser.email) == true)
                    }

                    if (queryResults.isEmpty()) {
                        computedStatistics++
                        return@addOnSuccessListener
                    }

                    val bookingNumbers = queryResults.size
                    var bookingsCompleted = 0

                    queryResults.forEach { bookingDoc ->
                        val bookingData = bookingDoc.data
                        val courtId = bookingData?.get("court_id") as Long

                        db.collection("courts")
                            .whereEqualTo("court_id", courtId)
                            .get()
                            .addOnSuccessListener { courtDoc ->
                                bookingsCompleted++

                                if(courtDoc.first().data["sport"] == sport) {
                                    statistics[sport]?.compute("played") { key, value ->
                                        if(value==null) 0 else value+1
                                    }
                                }

                                if(bookingsCompleted == bookingNumbers) {
                                    computedStatistics++

                                    if(computedStatistics == totalStatisticsToCompute) {
                                        this.userStatistics.postValue(statistics)


                                    }
                                }
                            }
                    }
                }

            //compute planned
            db.collection("bookings")
                .whereGreaterThan("date", today())
                .get()
                .addOnSuccessListener { querySnapshot ->

                    val queryResults = querySnapshot!!.documents.filter { document ->
                        val teamsMap = document["teams"] as? Map<String, String>

                        (teamsMap?.containsKey(loggedInUser.email) == true)
                    }

                    if (queryResults.isEmpty()) {
                        computedStatistics++
                        return@addOnSuccessListener
                    }

                    val bookingNumbers = queryResults.size
                    var bookingsCompleted = 0

                    queryResults.forEach { bookingDoc ->
                        val bookingData = bookingDoc.data
                        val courtId = bookingData?.get("court_id") as Long

                        db.collection("courts")
                            .whereEqualTo("court_id", courtId)
                            .get()
                            .addOnSuccessListener { courtDoc ->
                                bookingsCompleted++

                                if(courtDoc.first().data["sport"] == sport) {
                                    statistics[sport]?.compute("planned") { key, value ->
                                        if(value==null) 0 else value+1
                                    }
                                }

                                if(bookingsCompleted == bookingNumbers) {
                                    computedStatistics++

                                    if(computedStatistics == totalStatisticsToCompute) {
                                        this.userStatistics.postValue(statistics)
                                    }
                                }
                            }
                    }
                }

            //compute organized
            db
                .collection("bookings")
                .whereEqualTo("user_email", loggedInUser.email)
                .get()
                .addOnSuccessListener {q ->
                    q.forEach { doc ->
                        db
                            .collection("courts")
                            .whereEqualTo("court_id", doc.data["court_id"])
                            .get()
                            .addOnSuccessListener {
                                val filtered = it.filter { q -> q.data["sport"] == sport }
                                if(filtered.isNotEmpty()) {
                                    statistics[sport]?.compute("organized") { _, value ->
                                        if (value == null) 0 else value + 1
                                    }
                                    computedStatistics++
                                    if(computedStatistics == totalStatisticsToCompute) this.userStatistics.postValue(statistics)
                                }
                            }
                    }
                }

            //compute seen
            db
                .collection("bookings")
                .whereLessThan("date", today())
                .get()
                .addOnSuccessListener {bookingsQuery ->

                    db
                        .collection("courts")
                        .whereEqualTo("sport", sport)
                        .get()
                        .addOnSuccessListener {courtsQuery ->
                            val courtIds = courtsQuery.documents
                                .map { d -> d.data?.get("court_id") as Long }

                            val queryResults = bookingsQuery!!.documents.filter { document ->
                                val teamsMap = document["teams"] as? Map<String, String>

                                (teamsMap?.containsKey(loggedInUser.email) == true) && (courtIds.contains(document["court_id"] as Long))
                            }

                            if (queryResults.isEmpty()) {
                                computedStatistics++
                                return@addOnSuccessListener
                            }

                            val seenUsers = HashSet<String>()
                            for(booking in queryResults) {
                                val bookingUsers = booking.toObject(FireBooking::class.java)?.teams
                                if (bookingUsers != null) {
                                    for(user in bookingUsers.keys) {
                                        seenUsers.add(user)
                                    }
                                }
                            }
                            statistics[sport]?.set("seen", seenUsers.size-1)
                            computedStatistics++
                            if(computedStatistics == totalStatisticsToCompute) {
                                this.userStatistics.postValue(statistics)
                            }
                        }
                }
        }
    }

    fun setPicture(pic: Bitmap) {
        this.picture.postValue(pic)
    }

    /*fun updateUserStatistics(stats: HashMap<String, HashMap<String, Int>>) {
        var newUser = user.value
        if (newUser != null) {
            newUser.statistics = stats
            db.collection("users").whereEqualTo("email", newUser.email).get()
                .addOnSuccessListener {q ->
                    val reference = q.documents[0].reference
                    reference.set(newUser)
                }
            user.postValue(newUser)
        }
    }*/
}