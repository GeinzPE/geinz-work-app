package com.geinzz.geinzwork

import android.app.Application
import com.geinzz.geinzwork.Network_internet.ConnectivityObserver
import com.geinzz.geinzwork.Network_internet.DefaultConnectivityObserver
import com.geinzz.geinzwork.utils.constantes.constantes.NetworkMonitor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestoreSettings

class App : Application() {
    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()
        val settings = firestoreSettings {
            isPersistenceEnabled = true  // 🔥 Cache offline activado
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED // opcional

        }
        FirebaseFirestore.getInstance().firestoreSettings = settings
        connectivityObserver = DefaultConnectivityObserver(this)
    }
}
