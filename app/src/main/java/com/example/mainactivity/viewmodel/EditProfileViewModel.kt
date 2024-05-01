package com.example.mainactivity.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.Storage
import com.example.mainactivity.joins.BookingCourt
import com.example.mainactivity.models.Booking
import com.example.mainactivity.models.FireUser
import com.example.mainactivity.models.PersonSports
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URL
import java.time.LocalDate

class EditProfileViewModel(application: Application): AndroidViewModel(application) {
    val userStatistics = MutableLiveData<HashMap<String, HashMap<String, Int>>>()
    val db = Firebase.firestore
    val user = MutableLiveData<FireUser>()
    val picture = MutableLiveData<Bitmap>()
    val saved = MutableLiveData<Boolean>(false)
    val validName = MutableLiveData<Boolean>(false)

    fun getUser(email: String) {
        db.collection("users").whereEqualTo("email", email).get()
            .addOnSuccessListener {
                val fireUser = it.first().toObject(FireUser::class.java)
                this.user.postValue(fireUser)
                this.userStatistics.postValue(fireUser.statistics)
            }
    }

    fun getPicture(email: String) {
        Storage.getProfilePic(email) {
            this.picture.postValue(it)
        }
    }


    fun updateUserStatistics(user: FireUser) {
        db.collection("users").whereEqualTo("email", user.email).get()
            .addOnSuccessListener {q ->
                val reference = q.documents[0].reference
                reference.set(user)
                Storage.saveProfilePic(user.email, this.picture.value!!) {
                    saved.postValue(true)
                    validName.postValue(true)
                }
            }
        //user.postValue(newUser)
    }
}