package com.geinzz.geinzwork.model

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService

class MyFirebaseService: FirebaseMessagingService()  {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_creade", "Nuevo token: $token")
        // No hay usuario registrado aquí: guardaremos al login
    }
}