package com.geinzz.geinzwork

import android.app.Application
import com.geinzz.geinzwork.utils.constantes.constantes.NetworkMonitor

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.init(applicationContext)
    }
}
