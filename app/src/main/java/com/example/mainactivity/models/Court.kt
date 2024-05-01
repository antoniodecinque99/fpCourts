package com.example.mainactivity.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "courts")
data class Court(
    @PrimaryKey(autoGenerate = true)
    val court_id: Int,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "sport")
    val sport: String,
    @ColumnInfo(name = "address")
    val address: String,
    @ColumnInfo(name = "price")
    val price: Double,
    @ColumnInfo(name = "favorite")
    var favorite: Boolean,

    ) {
    @Ignore var averageRating: Float = 0.0f
}