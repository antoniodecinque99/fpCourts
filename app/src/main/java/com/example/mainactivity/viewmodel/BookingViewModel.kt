package com.example.mainactivity.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.joins.BookingCourt
import com.example.mainactivity.models.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BookingViewModel(application: Application): AndroidViewModel(application) {

    val booking = MutableLiveData<List<BookingCourt>>()
    val courts = MutableLiveData<List<Court>>()
    val db = BookingDatabase.getDatabase(application.applicationContext)
    var timeslot_id = MutableLiveData<Int>()
    var court_id = MutableLiveData<Int>()
    var date = MutableLiveData<String>()
    val favoriteList = MutableLiveData<MutableList<Long>>()

    var busyTimeSlots = MutableLiveData<Map<Int, List<Int>>>()

    private var db_ = Firebase.firestore
    val courts_= MutableLiveData<List<FireCourt>>()

    fun getCourts() {
        db_.collection("courts").get()
            .addOnSuccessListener { querySnapshot ->
                val courtsList = mutableListOf<FireCourt>()
                for (document in querySnapshot) {
                    val court = document.toObject(FireCourt::class.java)
                    courtsList.add(court)
                }
                courts_.postValue(courtsList)
            }
    }

    fun getFavoriteCourts(){
        val currentUserEmail = LoggedInUser.user.email
        db_.collection("favorites")
            .whereEqualTo("user_email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot) {
                    val courtIds = doc.get("court_id") as MutableList<Long>
                    favoriteList.postValue(courtIds)
                }
            }
    }

    fun addFavoriteCourt(courtId: Long){
        val currentUserEmail = LoggedInUser.user.email

        db_.collection("favorites")
            .whereEqualTo("user_email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db_.batch()
                for (doc in querySnapshot) {
                    val courtIds = doc.get("court_id") as MutableList<Long>
                    if (!courtIds.contains(courtId)) {
                        courtIds.add(courtId)
                        val documentRef = db_.collection("favorites").document(doc.id)
                        batch.update(documentRef, "court_id", courtIds)
                    }
                }
                batch.commit()
            }
    }

    fun removeFavoriteCourt(courtId: Long){
        val currentUserEmail = LoggedInUser.user.email
        db_.collection("favorites")
            .whereEqualTo("user_email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = db_.batch()
                for (doc in querySnapshot) {
                    val courtIds = doc.get("court_id") as MutableList<Long>
                    Log.d("list_remove", this.favoriteList.value.toString())
                    courtIds.remove(courtId)
                    val documentRef = db_.collection("favorites").document(doc.id)
                    batch.update(documentRef, "court_id", courtIds)
                }
                batch.commit()
            }
    }


    fun getCourtBySport(courtSport: String) {
        db_.collection("courts")
            .whereEqualTo("sport", courtSport)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val courtsList = mutableListOf<FireCourt>()
                for (document in querySnapshot) {
                    val court = document.toObject(FireCourt::class.java)
                    courtsList.add(court)
                }
                Thread {
                    courts_.postValue(courtsList)
                }.start()
            }
    }


    fun getBusySlots (date: String) {
        val busyTimeSlotsMap = mutableMapOf<Int, MutableList<Int>>()

        db_.collection("bookings")
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val courtId = document.getLong("court_id")?.toInt()
                    val timeSlotId = document.getLong("time_slot_id")?.toInt()

                    if (courtId != null && timeSlotId != null) {
                        if (busyTimeSlotsMap.containsKey(courtId)) {
                            busyTimeSlotsMap[courtId]?.add(timeSlotId)
                        } else {
                            busyTimeSlotsMap[courtId] = mutableListOf(timeSlotId)
                        }
                    }
                }

                busyTimeSlots.postValue(busyTimeSlotsMap)
            }
    }
}