package com.example.mainactivity.models

import com.google.firebase.firestore.Exclude
import kotlin.properties.Delegates

class FireCourtReview() {
    var review_id: Int = 0
    lateinit var user_email: String
    var court_id: Int = 0
    var rating: Int = 3
    lateinit var review: String
    lateinit var date: String
    var booking_id: Int? = 0
    var booking_date: String? = null


    @Exclude
    @JvmField
    var user: FireUser? = null

    constructor(
        review_id: Int,
        user_email: String,
        court_id: Int,
        rating: Int,
        review: String,
        date: String,
        booking_id: Int?,
        booking_date: String?
    ) : this() {
        this.review_id = review_id
        this.user_email = user_email
        this.court_id = court_id
        this.rating = rating
        this.review = review
        this.date = date
        this.booking_id = booking_id
        this.booking_date = booking_date
    }

}