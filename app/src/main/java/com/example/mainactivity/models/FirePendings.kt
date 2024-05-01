package com.example.mainactivity.models

class FirePendings {
    var booking_id: Long = 0
    lateinit var user_email: String
    lateinit var team:  String
    var status: String = "pending"
    var seen: Boolean = false

    constructor()

    constructor(
        booking_id: Long,
        user_email: String,
        team: String,
        status: String,
        seen: Boolean
    ) {
        this.booking_id = booking_id
        this.user_email = user_email
        this.team = team
        this.status = status
        this.seen = seen
    }
}

