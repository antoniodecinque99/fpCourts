package com.example.mainactivity.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.Companion.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.example.mainactivity.joins.BookingCourt

@Dao
interface CourtDao {

    @Query("SELECT * FROM courts ORDER BY name")
    fun getAllCourts() : List<Court>

    @Query("SELECT * FROM courts WHERE sport LIKE :courtSport ORDER BY name")
    fun getCourtsBySport(courtSport: String): List<Court>

    @Query("SELECT * FROM courts WHERE court_id LIKE :courtId")
    fun getCourtById(courtId: Int): Court

    @Query("SELECT * FROM courts WHERE court_id LIKE :courtId")
    fun getCourtByIdAsCourtObject(courtId: Int): Court


    @Insert(onConflict = REPLACE)
    fun save(court: Court)
}