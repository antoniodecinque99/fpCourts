package com.example.mainactivity.models;

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")

data class Booking (
        @PrimaryKey(autoGenerate = true)
        val booking_id: Int = 0,
        @ColumnInfo(name = "user_id")
        val user_id: Int,
        @ColumnInfo(name = "user_request")
        val user_request: String,

        @ColumnInfo(name = "court_id")
        val court_id: Int,

        @ColumnInfo(name = "date")
        val date: String,
        @ColumnInfo(name = "time_slot_id")
        val time_slot_id: Int)


{



}




