package com.example.mainactivity

import com.example.mainactivity.exceptions.UserNotLoggedInException
import com.example.mainactivity.models.FireUser

object LoggedInUser {
    private var _user: FireUser? = null

    var user: FireUser
    set(u) {
        this._user = u
    }

    get(): FireUser {
        if(_user != null) {
            return _user as FireUser
        } else {
            throw UserNotLoggedInException("No logged in user")
        }
    }
}