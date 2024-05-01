package com.example.mainactivity.models

class FireBookingCourtPending {
    var booking_id: Long? = null
    var user_email_organizer: String? = null
    var court_id: Long? = null
    var date: String? = null
    var time_slot_id: Long? = null
    var court_name: String? = null
    var court_sport: String? = null
    var court_address: String? = null
    var court_price: Double? = null
    var status: String? = null
    var team: String? = null
    var seen: Boolean? = null

    constructor()

    constructor(
        bookingId: Long?,
        userEmailOrganizer: String?,
        courtId: Long?,
        date: String?,
        timeSlotId: Long?,
        courtName: String?,
        courtSport: String?,
        courtAddress: String?,
        courtPrice: Double?,
        status: String?,
        team: String?,
        seen: Boolean?
    ) {
        this.booking_id = bookingId
        this.user_email_organizer = userEmailOrganizer
        this.court_id = courtId
        this.date = date
        this.time_slot_id = timeSlotId
        this.court_name = courtName
        this.court_sport = courtSport
        this.court_address = courtAddress
        this.court_price = courtPrice
        this.status = status
        this.team = team
        this.seen = seen
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("FireBookingCourtPending{\n")
        stringBuilder.append("\tbooking_id=").append(booking_id).append(",\n")
        stringBuilder.append("\tuser_email_organizer='").append(user_email_organizer).append("',\n")
        stringBuilder.append("\tcourt_id=").append(court_id).append(",\n")
        stringBuilder.append("\tdate='").append(date).append("',\n")
        stringBuilder.append("\ttime_slot_id=").append(time_slot_id).append(",\n")
        stringBuilder.append("\tcourt_name='").append(court_name).append("',\n")
        stringBuilder.append("\tcourt_sport='").append(court_sport).append("',\n")
        stringBuilder.append("\tcourt_address='").append(court_address).append("',\n")
        stringBuilder.append("\tcourt_price=").append(court_price).append(",\n")
        stringBuilder.append("\tstatus='").append(status).append("',\n")
        stringBuilder.append("\tteam='").append(team).append("'\n")
        stringBuilder.append("\tseen='").append(seen).append("'\n")
        stringBuilder.append("}")
        return stringBuilder.toString()
    }

}