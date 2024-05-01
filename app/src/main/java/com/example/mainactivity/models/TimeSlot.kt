package com.example.mainactivity.models

import java.sql.Time

class TimeSlot(private val id: Int) {
    val startTime: String
    val endTime: String

    init {
        when (id) {
            1 -> {
                startTime = "9:00 AM"
                endTime = "10:30 AM"
            }
            2 -> {
                startTime = "10:30 AM"
                endTime = "12:00 PM"
            }
            3 -> {
                startTime = "12:00 PM"
                endTime = "1:30 PM"
            }
            4 -> {
                startTime = "3:00 PM"
                endTime = "4:30 PM"
            }
            5 -> {
                startTime = "4:30 PM"
                endTime = "6:00 PM"
            }
            6 -> {
                startTime = "6:00 PM"
                endTime = "7:30 PM"
            }
            else -> throw IllegalArgumentException("Invalid Time Slot ID: $id")
        }
    }

    override fun toString(): String {
        return "$startTime - $endTime"
    }
}
