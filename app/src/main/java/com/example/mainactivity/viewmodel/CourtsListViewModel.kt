package com.example.mainactivity.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.models.Court
import com.example.mainactivity.models.CourtReview
import com.example.mainactivity.models.FireCourt
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class CourtsListViewModel(application: Application): AndroidViewModel(Application()) {
    val courts = MutableLiveData<List<FireCourt>>()
    val showCourtInfoId = MutableLiveData<Int>()

    val db = Firebase.firestore

    fun loadCourts(sport: String?) {
        /*Thread {
            val courts: List<Court> = if(sport == null) courtsDao.getAllCourts() else courtsDao.getCourtsBySport(sport)
            val ratings = reviewsDao.getCourtsAverageRating()
            courts.forEach {
                it.averageRating = ratings[it.court_id] ?: 0f
            }
            this.courts.postValue(courts)
        }.start()*/

        var query = db.collection("courts")
        if(sport != null) {
            query.whereEqualTo("sport", sport)
        }

        query
            .get()
            .addOnSuccessListener { q ->
                val courts = q.map { c -> c.toObject(FireCourt::class.java) }
                val courtsSize = courts.size
                var completedCourts = 0

                courts.forEach { court ->
                    var averageRating = 0f
                    db
                        .collection("courtReviews")
                        .whereEqualTo("court_id", court.court_id)
                        .get()
                        .addOnSuccessListener {q ->
                            completedCourts++
                            q.forEach {
                                averageRating += (it.getLong("rating")?.toInt() ?: 0).toFloat() /q.size()
                            }
                            court.averageRating = averageRating
                            if(completedCourts == courtsSize) this.courts.postValue(courts.sortedBy { it.name })
                        }
                }

                this.courts.postValue(courts.sortedBy { it.name })
            }

    }

    fun getCourtBySport(courtSport: String) {
        db.collection("courts")
            .whereEqualTo("sport", courtSport)
            .get()
            .addOnSuccessListener { q ->
                val courts = q.map { c -> c.toObject(FireCourt::class.java) }
                val courtsSize = courts.size
                var completedCourts = 0

                courts.forEach { court ->
                    var averageRating = 0f
                    db
                        .collection("courtReviews")
                        .whereEqualTo("court_id", court.court_id)
                        .get()
                        .addOnSuccessListener { q ->
                            completedCourts++
                            q.forEach {
                                averageRating += (it.getLong("rating")?.toInt()
                                    ?: 0).toFloat() / q.size()
                            }
                            court.averageRating = averageRating
                            if (completedCourts == courtsSize) this.courts.postValue(courts.sortedBy { it.name })
                        }
                }

                this.courts.postValue(courts.sortedBy { it.name })
            }
    }

    fun showCourtInfo(id: Int) {
        showCourtInfoId.postValue(id)
    }
}