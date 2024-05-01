package com.example.mainactivity.models

import com.google.firebase.firestore.Exclude

class FireCourt {
    var court_id: Int = 0
    lateinit var name: String
    lateinit var sport: String
    lateinit var address: String
    var price: Double = 0.0
    var favorite: Boolean = false

    @Exclude
    @JvmField
    var averageRating: Float = 0f

    constructor()
    constructor(
        court_id: Int,
        name: String,
        sport: String,
        address: String,
        price: Double,
        favorite: Boolean
    ) {
        this.court_id = court_id
        this.name = name
        this.sport = sport
        this.address = address
        this.price = price
        this.favorite = favorite
    }
}