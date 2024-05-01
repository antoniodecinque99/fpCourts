package com.example.mainactivity.models

class FireBookingCourt {
    var booking_id: Long? = null
    var user_email: String? = null
    var user_request: String? = null
    var court_id: Long? = null
    var date: String? = null
    var time_slot_id: Long? = null
    var teams: Map<String, String>? = null
    var court_name: String? = null
    var court_sport: String? = null
    var court_address: String? = null
    var court_price: Double? = null

    constructor()

    constructor(
        bookingId: Long?,
        userEmail: String?,
        userRequest: String?,
        courtId: Long?,
        date: String?,
        timeSlotId: Long?,
        teams: Map<String, String>?,
        courtName: String?,
        courtSport: String?,
        courtAddress: String?,
        courtPrice: Double?
    ) {
        this.booking_id = bookingId
        this.user_email = userEmail
        this.user_request = userRequest
        this.court_id = courtId
        this.date = date
        this.time_slot_id = timeSlotId
        this.teams = teams
        this.court_name = courtName
        this.court_sport = courtSport
        this.court_address = courtAddress
        this.court_price = courtPrice
    }

    override fun toString(): String {
        return "FireBookingCourt\n" +
                "\tbooking_id=$booking_id\n" +
                "\tuser_email=$user_email\n " +
                "\tuser_request=$user_request\n" +
                "\tcourt_id=$court_id\n" +
                "\tdate=$date\n" +
                "\ttime_slot_id=$time_slot_id\n" +
                "\tteams=$teams\n" +
                "\tcourt_name=$court_name\n" +
                "\tcourt_sport=$court_sport\n" +
                "\tcourt_address=$court_address\n" +
                "\tcourt_price=$court_price" +
                ")"
    }
}


