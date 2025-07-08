package com.example.geinzwork

import android.app.Application
import com.example.geinzwork.constantesGeneral.NetworkMonitor

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        NetworkMonitor.init(applicationContext)
    }
}
