package com.example.mainactivity.models

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.Companion.REPLACE
import com.example.mainactivity.joins.BookingCourt

@Dao
interface BookingDao {

    @Query("SELECT * FROM bookings")
    fun getAllBookings() : LiveData<List<Booking>>

    @Query("SELECT " +
            "bookings.booking_id as booking_id, " +
            "bookings.user_id as user_id, " +
            "bookings.user_request as user_request, " +
            "bookings.court_id as court_id, " +
            "bookings.date as date, " +
            "bookings.time_slot_id as time_slot_id, " +
            "courts.name as court_name, " +
            "courts.sport as court_sport, " +
            "courts.address as court_address, " +
            "courts.price as court_price FROM bookings, courts WHERE bookings.booking_id = courts.court_id")
    fun getAllBookingsCourts(): List<BookingCourt>

    @Query("SELECT " +
            "bookings.booking_id as booking_id, " +
            "bookings.user_id as user_id, " +
            "bookings.user_request as user_request, " +
            "bookings.court_id as court_id, " +
            "bookings.date as date, " +
            "bookings.time_slot_id as time_slot_id " +
            "FROM bookings")
    fun getAllBookings_(): List<Booking>


    @Query("SELECT " +
            "bookings.booking_id as booking_id, " +
            "bookings.user_id as user_id, " +
            "bookings.user_request as user_request, " +
            "bookings.court_id as court_id, " +
            "bookings.date as date, " +
            "bookings.time_slot_id as time_slot_id, " +
            "courts.name as court_name, " +
            "courts.sport as court_sport, " +
            "courts.address as court_address, " +
            "courts.price as court_price FROM bookings, courts WHERE courts.court_id = bookings.court_id AND bookings.date = :today_date ORDER BY time_slot_id")
    fun getTodayBookings(today_date: String): List<BookingCourt>

    @MapInfo(keyColumn = "ci", valueColumn = "tsi")
    @Query("SELECT distinct court_id as ci, time_slot_id as tsi FROM bookings WHERE date LIKE :date GROUP BY court_id, time_slot_id")
    fun getBusySlots(date: String): Map<Int, List<Int>>

    @Query("SELECT * FROM bookings WHERE booking_id LIKE :bookingId")
    fun getBookingById(bookingId: Int): Booking

    @Insert(onConflict = REPLACE)
    fun save(booking: Booking)

    @Query("DELETE FROM bookings WHERE booking_id LIKE :bookingId")
    fun deleteBooking(bookingId: Int): Int



}