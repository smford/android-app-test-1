package com.example.modernauthapp

import android.app.Application
import com.google.firebase.FirebaseApp

class AuthApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
    }
}
