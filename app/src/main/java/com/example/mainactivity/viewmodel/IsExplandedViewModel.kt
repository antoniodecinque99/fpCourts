package com.example.mainactivity.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class IsExplandedViewModel (application: Application): AndroidViewModel(application) {
    val expanded = true
    fun getExpandedState() {
        Thread {

        }.start()
    }
}