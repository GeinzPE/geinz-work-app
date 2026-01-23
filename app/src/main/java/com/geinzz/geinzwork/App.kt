package com.geinzz.geinzwork

import android.app.Application
import com.geinzz.geinzwork.Network_internet.ConnectivityObserver
import com.geinzz.geinzwork.Network_internet.DefaultConnectivityObserver
import com.geinzz.geinzwork.herramientas_geinz.constantes.FirebaseSecundario
import com.geinzz.geinzwork.utils.constantes.constantes.NetworkMonitor
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestoreSettings

class App : Application() {

    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()

        // 🔹 Inicializa Firebase principal
        FirebaseApp.initializeApp(this)

        // 🔹 Firestore settings
        val settings = firestoreSettings {
            isPersistenceEnabled = true
            cacheSizeBytes = FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED
        }
        FirebaseFirestore.getInstance().firestoreSettings = settings

        // 🔹 Inicializa Firebase secundario
        FirebaseSecundario.inicializar(this)

        // 🔹 Red
        connectivityObserver = DefaultConnectivityObserver(this)
    }

}