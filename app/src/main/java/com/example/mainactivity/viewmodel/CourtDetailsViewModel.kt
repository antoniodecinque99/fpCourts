package com.example.mainactivity.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.mainactivity.BookingDatabase
import com.example.mainactivity.LoggedInUser
import com.example.mainactivity.models.*
import com.example.mainactivity.today
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class CourtDetailsViewModel(application: Application): AndroidViewModel(application) {

    val db = Firebase.firestore
    //val reviewsDao = db.courtReviewDao()
    //val courtDao = db.courtDao()
    //val usersDao = db.personDao()

    val court = MutableLiveData<FireCourt>()
    val reviews = MutableLiveData<List<FireCourtReview>>()

    val userReviewRating = MutableLiveData<Int>()
    val userReviewText = MutableLiveData<String>()
    val bookingId = MutableLiveData<Int>()
    val bookingDate = MutableLiveData<String>()
    val user = LoggedInUser.user
    //val userId = MutableLiveData(1)

    val userReview = MutableLiveData<FireCourtReview>()

    fun fetchCourts(id: Int) {
        db
            .collection("courts")
            .whereEqualTo("court_id", id)
            .get()
            .addOnSuccessListener { q ->
            val court = q.first().toObject(FireCourt::class.java)
            this.court.postValue(court)
        }
    }

    fun fetchReviews(courtId: Int, bookingId: Int) {
        /*Thread {
            val reviews = buildReviewsObjects(courtId)
            val loggedUserReviews = reviews.filter { it.user_id == loggedUserId }
            if(loggedUserReviews.isNotEmpty()) {
                val r = loggedUserReviews[0]
                this.userReview.postValue(r)
            }
            this.reviews.postValue(reviews)
        }.start()*/
        var reviewsNumber: Int
        var completedReviews = 0
        db
            .collection("courtReviews")
            .whereEqualTo("court_id", courtId)
            .get()
            .addOnSuccessListener { q ->
                var reviews = q/*.filter {
                    if(bookingId == -1) {
                        true
                    } else if(it["booking_id"] == null) {
                        false
                    } else {
                        it["booking_id"] as Long == bookingId.toLong()
                    }
                }*/.map { review ->
                    review.toObject(FireCourtReview::class.java)
                }
                reviewsNumber = reviews.size
                reviews.forEach {reviewObject ->
                    db
                        .collection("users")
                        .whereEqualTo("email", reviewObject.user_email)
                        .get()
                        .addOnSuccessListener { q ->
                            completedReviews++
                            reviewObject.user = q.first().toObject(FireUser::class.java)
                            if(reviewsNumber == completedReviews) {
                                this.reviews.postValue(reviews.sortedBy { it.date }.reversed())
                            }
                        }
                }
            }
    }

    fun postUserReviewRating(r: Int) {
        userReviewRating.postValue(r)
    }

    fun postUserReviewText(t: String) {
        userReviewText.postValue(t)
    }

    fun saveReview() {
        //val userId = this.userId.value ?: 1
        val courtId = this.court.value?.court_id ?: 0
        val reviewRating = this.userReviewRating.value ?: 1
        val reviewText = this.userReviewText.value ?: ""
        val bookingId = this.bookingId.value ?: -1
        val bookingDate = this.bookingDate.value

        //val review = CourtReview(0, userId, courtId, reviewRating, reviewText, today())
        db
            .collection("courtReviews")
            .orderBy("review_id", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener {querySnapshot ->
                var maxReviewId = 1
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.first()
                    maxReviewId = document.getLong("review_id")?.plus(1)?.toInt() ?: 0
                }

                val courtReview = FireCourtReview(maxReviewId, user.email, courtId, reviewRating, reviewText, today(), bookingId, bookingDate)

                db
                    .collection("courtReviews")
                    .add(courtReview)
                    .addOnSuccessListener {
                        this.userReview.postValue(courtReview)
                        fetchReviews(courtId, bookingId)
                    }
            }

        /*Thread {
            val newId = reviewsDao.save(review)
            review.review_id = newId.toInt()
            this.userReview.postValue(review)

            val updatedReviews = buildReviewsObjects(court.value?.court_id ?: 1)
            this.reviews.postValue(updatedReviews)
        }.start()*/
    }

    fun updateReview() {
        val reviewRating = this.userReviewRating.value ?: 1
        val reviewText = this.userReviewText.value ?: ""
        val bookingId = this.bookingId.value ?: -1

        val r = this.userReview.value
        r?.review = reviewText
        r?.rating = reviewRating

        db
            .collection("courtReviews")
            .whereEqualTo("review_id", this.userReview.value?.review_id)
            .get()
            .addOnSuccessListener { q ->
                this.userReview.value?.let { q.documents.first().reference.set(it) }
                fetchReviews(this.court.value?.court_id ?: 0, bookingId)
            }
    }

    /*private fun buildReviewsObjects(courtId: Int): List<CourtReview> {
        val reviews = reviewsDao.getCourtReviews(courtId)
        for(review in reviews) {
            review.user = usersDao.getPersonById(review.user_id)
        }
        return reviews
    }*/
}