package com.example.mainactivity.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.text.toLowerCase
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.models.*
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import java.net.URL

class ConfirmBookingViewModel(application: Application): AndroidViewModel(application) {
    var court = MutableLiveData<Court>()
    var date = MutableLiveData<String>()
    var timeslot = MutableLiveData<Int>()
    var additionalRequests = MutableLiveData<String>()
    var bookingSaved = MutableLiveData(0)
    var picture = MutableLiveData<Bitmap>()
    val user = MutableLiveData<FireUser>()
    var court_ = MutableLiveData<FireCourt>()
    val users_ = MutableLiveData<List<FireUser>>()
    private var db_ = Firebase.firestore
    var addedUserEmails = listOf<String>()

    var actual_booking = MutableLiveData<Long>()
    var booking_state = MutableLiveData<String>()
    var map = MutableLiveData<MutableMap<String,String>>()
    var booking_team = MutableLiveData<String>()
    var booking_email = MutableLiveData<String>()
    var actual_booking_saved = MutableLiveData(0)

    fun saveCurrentBooking(){
        val actualBooking = this.actual_booking.value ?: 0.toLong()

        this.map.value?.forEach {
            val bookingEmail = it.key
            val bookingTeam = it.value

            val newDocument = FirePendings(
                actualBooking,
                bookingEmail,
                bookingTeam,
                "pending",
                false
            )

            db_.collection("pendings")
                .add(newDocument)
                .addOnSuccessListener {
                    actual_booking_saved.postValue(1)
                    Log.d("success", "success")
                }
                .addOnFailureListener { e ->
                    Log.d("error", "error")
                }
        }


    }

    fun setCourt(courtId: Int) {
        db_.collection("courts")
            .whereEqualTo("court_id", courtId)
            .get()
            .addOnSuccessListener { courtDocs ->
                val courtDoc = courtDocs.documents.first()
                this.court_.postValue(courtDoc.toObject(FireCourt::class.java))
            }
    }

    fun setDate(date: String) {
        this.date.postValue(date)
    }

    fun setTimeslot(id: Int) {
        this.timeslot.postValue(id)
    }

    fun clearUserSearch() {
        users_.value = mutableListOf()
    }
    fun updateBooking(mail: String) {
        addedUserEmails = addedUserEmails.plus(mail)
        var maxBookingId: Long? = null
        db_.collection("bookings")
            .orderBy("booking_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                maxBookingId = if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document.getLong("booking_id")?.plus(1)
                } else {
                    0
                }
                    Log.d("booking id", maxBookingId.toString())
                    Log.d("mail", mail)
                    Log.d("team", booking_team.value.toString())
                    actual_booking.postValue(maxBookingId!!)
                    booking_state.postValue("pending")
                    booking_email.postValue(mail)

                    val tempMap = map.value ?: mutableMapOf()
                    tempMap[mail] = booking_team.value.toString()
                    Log.d("temp map ", tempMap.toString())

                    map.postValue(tempMap)
                    Log.d("map", map.value.toString())

            }
    }

    fun deleteUserFromBooking(mail: String) {
        addedUserEmails = addedUserEmails.minus(mail)
        map.value?.remove(mail)
        Log.d("map", map.value.toString())
    }


    fun getUserByNick(nickName: String) {
        Log.d("nickName", nickName)
        val usersList = mutableListOf<FireUser>()
        if (nickName.length < 2) {
            users_.value = mutableListOf()
        } else {
            db_.collection("users")
                //.whereGreaterThanOrEqualTo("nickname", nickName.lowercase())
                //.whereLessThanOrEqualTo("nickname", nickName.lowercase() + "\uf8ff")
                .get()
                .addOnSuccessListener { querySnapshot ->

                    if (querySnapshot.documents.isEmpty()) {
                        users_.postValue(mutableListOf())
                    }
                    querySnapshot.documents.forEach { userDoc ->
                        val nickname = userDoc.getString("nickname")
                        val lowercaseNickname = nickname?.lowercase()
                        if (lowercaseNickname != null && lowercaseNickname.contains(
                                nickName.lowercase(),
                                ignoreCase = true
                            )
                        ) {
                            val userData = userDoc.toObject(
                                FireUser::class.java
                            )
                            if ((!addedUserEmails.contains(userData?.email)) and (LoggedInUser.user.nickname != userData?.nickname)) {
                                usersList.add(userData!!)
                            }
                            users_.postValue(usersList)
                            Log.d("users", usersList.toString())
                        }
                    }

                }
        }
    }

    fun saveBooking(email: String, myTeam: String) {
        val userRequest = this.additionalRequests.value ?: ""
        val court_id = this.court_.value?.court_id ?: 1
        val date = this.date.value ?: ""
        val timeSlot = this.timeslot.value ?: 0

        /*val b = Booking(user_id = 1, user_request = userRequest, court_id = court_id, date = date, time_slot_id = timeSlot)
        Thread {
            bookingDao.save(b)
            bookingSaved.postValue(1)
        }.start()*/

        var maxBookingId: Long? = null

        db_.collection("bookings")
            .orderBy("booking_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                maxBookingId = if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    document.getLong("booking_id")?.plus(1)
                } else {
                    0
                }
                var mappa = mutableMapOf<String,String>()
                mappa[email] = myTeam

                val booking = FireBooking(
                    maxBookingId!!,
                    LoggedInUser.user.email,
                    userRequest,
                    court_id.toLong(),
                    date,
                    timeSlot,
                    mappa
                )

                db_.collection("bookings")
                    .add(booking)
                    .addOnSuccessListener{
                        bookingSaved.postValue(1)
                    }
            }
    }
}