package com.example.mainactivity.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "person_sports", primaryKeys = ["person_id", "sports_id"])

data class PersonSports (
    @ColumnInfo(name = "person_id")
    val person_id: Int,
    @ColumnInfo(name = "sports_id")
    val sports_id: Int,
    @ColumnInfo(name = "sport_name")
    val sport_name: String,
    @ColumnInfo(name = "level")
    var level: Int,
    @ColumnInfo(name = "matches_played")
    val played: Int,
    @ColumnInfo(name = "matches_won")
    val won: Int,
    @ColumnInfo(name = "matches_lost")
    val lost: Int,
    @ColumnInfo(name = "seen_users")
    val seenUsers: Int,
    @ColumnInfo(name = "matches_organized")
    val organized: Int,
    @ColumnInfo(name = "matches_planned")
    val planned: Int,

){

}