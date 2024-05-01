package com.example.mainactivity.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.R
import com.example.mainactivity.Storage
import com.example.mainactivity.getSportIcon
import com.example.mainactivity.models.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.URL

class BookingDetailsViewModel(application: Application, bookingIdFromIntent: Int) : AndroidViewModel(application) {
    var reservation = MutableLiveData<Booking>()
    var court = MutableLiveData<Court>()
    var courtImageId = MutableLiveData<Int>()
    private var bookingId = bookingIdFromIntent
    var sportImageId = MutableLiveData<Int>()

    private val db = BookingDatabase.getDatabase(application.applicationContext)

    var db_ = Firebase.firestore
    var booking = MutableLiveData<FireBooking>()
    var court_ = MutableLiveData<FireCourt>()
    var inviteStatusesTemp = mutableMapOf<String, String>()
    var inviteStatuses = MutableLiveData<MutableMap<String, String>>()
    var pendingUsersTemp = mutableListOf<FireUser?>()
    var pendingUsers = MutableLiveData<MutableList<FireUser?>>()
    var pendingUserTeamsTemp = mutableMapOf<String, String>()
    var pendingUserTeams = MutableLiveData<MutableMap<String, String>>()
    var userTemp = MutableLiveData<FireUser>()
    var users = MutableLiveData<MutableList<FireUser?>>()
    var usersTemp = mutableListOf<FireUser?>()
    var profilePicsTemp = mutableMapOf<String, Bitmap>()
    var profilePics = MutableLiveData<MutableMap<String, Bitmap>>()
    var pendingProfilePicsTemp = mutableMapOf<String, Bitmap>()
    var pendingProfilePics = MutableLiveData<MutableMap<String, Bitmap>>()
    var deletedCompleted = MutableLiveData<Boolean>(false)

    var areTherePendingUsers = MutableLiveData<Boolean>()

    val storage = Firebase.storage
    // Create a storage reference from our app
    val storageRef = storage.reference
    var courtImagePath = MutableLiveData<String>()
    var courtImageBitmap = MutableLiveData<Bitmap>()


    fun getUser(mail: String) {
        db_.collection("users")
            .whereEqualTo("email", mail)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]
                userTemp.postValue(document.toObject(FireUser::class.java))
                usersTemp.add(document.toObject(FireUser::class.java))
                if (usersTemp.size == booking.value?.teams?.size) {
                    users.value = usersTemp
                }
            }
    }

    fun getProfilePic(mail: String) {
        // Fetch and display the user's pictureUri using coroutines
        // Fetch and display the user's pictureUri using coroutines
        //GlobalScope.launch(Dispatchers.Main) {
            //withContext(Dispatchers.IO) {
                db_.collection("users")
                    .whereEqualTo("email", mail)
                    .get()
                    .addOnSuccessListener {querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            val userDocument = querySnapshot.documents[0]

                            Storage.getProfilePic(mail) {
                                if(it != null) {
                                    profilePicsTemp[mail] = it
                                }
                                if (profilePicsTemp.size == booking.value?.teams?.size) {
                                    profilePics.value = profilePicsTemp
                                }
                            }
                    }
                    /*pictureUri?.let {
                        try {
                            val inputStream: InputStream = URL(pictureUri).openStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)

                            withContext(Dispatchers.Main) {
                                profilePicsTemp[mail] = bitmap
                                if (profilePicsTemp.size == booking.value?.teams?.size) {
                                    profilePics.value = profilePicsTemp
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }*/
                }
            //}
        //}
    }

    fun getPendingUsers() {
        var maxPendings = inviteStatuses.value!!.filterValues {it == "pending"
        }.count()
        if (maxPendings == 0) {
            Log.d("asdas", "SO QUAA")
            areTherePendingUsers.value = false
        }
        var count = 0
        if (inviteStatuses.value!!.isEmpty()) {
            areTherePendingUsers.value = false
        }
        inviteStatuses.value?.forEach {
            if (it.value == "pending") {
                db_.collection("users")
                    .whereEqualTo("email", it.key)
                    .limit(1)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val document = querySnapshot.documents[0]
                        pendingUsersTemp.add(document.toObject(FireUser::class.java))
                        Log.d("AA", pendingUsersTemp.toString())
                        Log.d("AAA", "mario" + inviteStatusesTemp.size.toString())
                        count += 1
                        if (count >= maxPendings) {
                            pendingUsers.value = pendingUsersTemp
                        }
                    }
                    .addOnCompleteListener {
                        if (pendingUsersTemp.size > 0) {
                            areTherePendingUsers.value = true
                        } else {
                            areTherePendingUsers.value = false
                        }
                    }
            }
        }
    }

    fun getPendingProfilePics() {
        pendingUsers.value?.forEach {it1 ->
            // Fetch and display the user's pictureUri using coroutines
            //GlobalScope.launch(Dispatchers.Main) {
                //withContext(Dispatchers.IO) {
            if (it1 != null) {
                Storage.getProfilePic(it1.email) {
                    pendingProfilePicsTemp[it1.email] = it!!
                    pendingProfilePics.postValue(pendingProfilePicsTemp)
                }
            }
                    /*it1!!.pictureUri.let {
                        try {
                            val inputStream: InputStream = URL(it).openStream()
                            val bitmap = BitmapFactory.decodeStream(inputStream)

                            withContext(Dispatchers.Main) {
                                pendingProfilePicsTemp[it1.email] = bitmap
                                pendingProfilePics.postValue(pendingProfilePicsTemp)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }*/
                //}
            //}
        }
    }
    fun getInviteStatus() {
        db_.collection("pendings")
            .whereEqualTo("booking_id", bookingId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (doc in querySnapshot.documents) {
                    val mail = doc.get("user_email") as String
                    val status = doc.get("status") as String
                    val team = doc.get("team") as String
                    inviteStatusesTemp[mail] = status
                    pendingUserTeamsTemp[mail] = team
                }
                inviteStatuses.postValue(inviteStatusesTemp)
                pendingUserTeams.postValue(pendingUserTeamsTemp)
            }
    }

    fun getBooking() {
        db_.collection("bookings")
            .whereEqualTo("booking_id", bookingId)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]
                booking.postValue(document.toObject(FireBooking::class.java))
            }

        /*Thread {
            reservation.postValue(bookingDao.getBookingById(bookingId))
        }.start()*/
    }

    fun getCourt() {
        val courtId = booking.value?.court_id

        db_.collection("courts")
            .whereEqualTo("court_id", courtId)
            .get()
            .addOnSuccessListener { courtDocs ->
                val document = courtDocs.documents.first()
                court_.postValue(document.toObject(FireCourt::class.java))
            }

        /*Thread {
            var id = reservation.value?.court_id
            if (id == null) {
                id = 1
            }
            court.postValue(courtDao.getCourtByIdAsCourtObject(id))
        }.start()*/
    }



    private fun setCourtImage(courtId: Int?): Int {
        if (courtId == null) {
            return R.drawable.cit
        }
        return com.example.mainactivity.getCourtImage(courtId)
    }

    fun getCourtImageFilename() {
        var id = court_.value!!.court_id
        // query firestore for image path
        db_.collection("courtImages")
            .whereEqualTo("court_id", id)
            .get()
            .addOnSuccessListener { courtDocs ->
                val document = courtDocs.documents.first()
                val path = document.get("filename") as String
                courtImagePath.postValue(path)

            }
    }

    fun setCourtImageCloud() {
        val pathReference = storageRef.child("courtImages/" + courtImagePath.value!!)
        val TEN_MEGS: Long = 1024 * 1024 * 10
        pathReference.getBytes(TEN_MEGS).addOnSuccessListener {
            // Data for "images/island.jpg" is returned, use this as needed
            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
            courtImageBitmap.value = bmp
        }
    }

    fun setSportImage(sportName: String?): Int {
        if (sportName == null) {
            return R.drawable.football_icon
        }
        return getSportIcon(sportName)
    }

    fun getCourtImage() {
        courtImageId.value = setCourtImage(court_.value?.court_id)
    }

    fun getSportImage() {
        sportImageId.value = setSportImage(court.value?.sport)
    }

    fun deleteBooking(onComplete: () -> Unit) {
        db_.collection("bookings")
            .whereEqualTo("booking_id", bookingId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    if (document["user_email"] == LoggedInUser.user.email) {
                        document.reference.delete().addOnSuccessListener {
                            db_.collection("pendings")
                                .whereEqualTo("booking_id", document["booking_id"])
                                .get()
                                .addOnSuccessListener { querySnapshot ->
                                    val totalDocuments = querySnapshot.size()
                                    var deletedDocuments = 0

                                    querySnapshot.forEach { pendingDocument ->
                                        pendingDocument.reference.delete().addOnSuccessListener {
                                            deletedDocuments++

                                            // Check if all documents have been deleted
                                            if (deletedDocuments == totalDocuments) {
                                                onComplete()
                                            }
                                        }
                                    }
                                }
                        }
                    } else {
                        val teamsMap = document["teams"] as? MutableMap<String, String>
                        teamsMap?.remove(LoggedInUser.user.email)

                        document.reference
                            .update("teams", teamsMap)
                            .addOnSuccessListener {
                                db_.collection("pendings")
                                    .whereEqualTo("booking_id", document["booking_id"])
                                    .whereEqualTo("user_email", LoggedInUser.user.email)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        querySnapshot.documents[0].reference.delete()
                                            .addOnSuccessListener {
                                                onComplete()
                                            }
                                        onComplete()
                                    }
                            }
                    }
                }
                onComplete()
            }
    }

}