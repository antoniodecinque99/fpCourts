package com.example.mainactivity.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "persons")

data class Person (
    @PrimaryKey(autoGenerate = true)
    val person_id: Int = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "surname")
    val surname: String,
    @ColumnInfo(name = "nickname")
    val nickname: String

){

}