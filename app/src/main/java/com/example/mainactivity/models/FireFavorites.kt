package com.example.mainactivity.models

class FireFavorites {

    var favoriteList: MutableList<Long> = mutableListOf()
    lateinit var user_email: String

    constructor()

    constructor(
        favoriteList: MutableList<Long>,
        user_email: String,
    ) {
        this.favoriteList = favoriteList
        this.user_email = user_email

    }
}