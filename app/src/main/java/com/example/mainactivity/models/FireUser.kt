package com.example.mainactivity.models

class FireUser() {

    lateinit var name: String
    lateinit var nickname: String
    lateinit var email: String
    lateinit var birthDate: String
    lateinit var pictureUri: String
    lateinit var location: String
    lateinit var description: String

    constructor(
        name: String,
        nickname: String,
        email: String,
        birthDate: String,
        pictureUri: String,
        location: String,
        description: String,
        statistics: HashMap<String, HashMap<String, Int>>?
    ) : this() {
        this.name = name
        this.nickname = nickname
        this.email = email
        this.birthDate = birthDate
        this.pictureUri = pictureUri
        this.location = location
        this.description = description
        if (statistics != null) {
            this.statistics = statistics
        }
    }
    var statistics = hashMapOf<String, HashMap<String, Int>>()
}