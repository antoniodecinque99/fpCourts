package com.example.mainactivity.models

class FireBooking {
    var booking_id: Long = 0
    lateinit var user_email: String
    lateinit var user_request: String
    var court_id: Long = 0
    lateinit var date: String
    var time_slot_id: Int = 0
    lateinit var teams: Map<String, String>

    constructor()

    constructor(
        booking_id: Long,
        user_email: String,
        user_request: String,
        court_id: Long,
        date: String,
        time_slot_id: Int,
        teams: Map<String, String>
    ) {
        this.booking_id = booking_id
        this.user_email = user_email
        this.user_request = user_request
        this.court_id = court_id
        this.date = date
        this.time_slot_id = time_slot_id
        this.teams = teams
    }
}
