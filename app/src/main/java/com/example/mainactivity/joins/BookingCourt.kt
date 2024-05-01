package com.example.mainactivity.joins

class BookingCourt(
    val booking_id: Int?,
    val user_id: Int?,
    val user_request: String?,
    val court_id: Int?,
    val date: String?,
    val time_slot_id: Int?,
    val favorite: Boolean?,
    val court_name: String?,
    val court_sport: String?,
    val court_address: String?,
    val court_price: Double?
) {
    override fun toString(): String {
        return "BookingCourt(booking_id=$booking_id, user_id=$user_id, user_request=$user_request, " +
                "court_id=$court_id, date=$date, time_slot_id=$time_slot_id, favorite=$favorite, " +
                "court_name=$court_name, court_sport=$court_sport, court_address=$court_address, " +
                "court_price=$court_price)"
    }
}


