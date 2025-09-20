package com.geinzz.geinzwork

import android.app.Application
import com.geinzz.geinzwork.Network_internet.ConnectivityObserver
import com.geinzz.geinzwork.Network_internet.DefaultConnectivityObserver
import com.geinzz.geinzwork.utils.constantes.constantes.NetworkMonitor

class App : Application() {
    lateinit var connectivityObserver: ConnectivityObserver
        private set

    override fun onCreate() {
        super.onCreate()
        connectivityObserver = DefaultConnectivityObserver(this)
    }
}
