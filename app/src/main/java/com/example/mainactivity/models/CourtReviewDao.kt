package com.example.mainactivity.models

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query

@Dao
interface CourtReviewDao {

    @MapInfo(keyColumn = "court_id", valueColumn = "avg_rating")
    @Query("SELECT court_id as court_id, AVG(rating) as avg_rating FROM courtReviews GROUP BY court_id")
    fun getCourtsAverageRating() : Map<Int, Float>

    @Query("SELECT * FROM courtReviews WHERE court_id = :court_id ORDER BY date")
    fun getCourtReviews(court_id: Int): List<CourtReview>

    @Query("SELECT * FROM courtReviews WHERE user_id = :user_id ORDER BY date")
    fun getUserReviews(user_id: Int): List<CourtReview>

    @Insert(onConflict = REPLACE)
    fun save(courtReview: CourtReview): Long
}