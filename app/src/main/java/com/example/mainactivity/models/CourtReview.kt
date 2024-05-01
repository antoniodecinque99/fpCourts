package com.example.mainactivity.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "courtReviews")
class CourtReview(
    @PrimaryKey(autoGenerate = true)
    var review_id: Int,

    @ColumnInfo(name = "user_id")
    val user_id: Int,

    @ColumnInfo(name = "court_id")
    val court_id: Int,

    @ColumnInfo(name = "rating")
    var rating: Int,

    @ColumnInfo(name = "review")
    var review: String,

    @ColumnInfo(name = "date")
    val date: String
) {
    @Ignore
    lateinit var user: Person
}