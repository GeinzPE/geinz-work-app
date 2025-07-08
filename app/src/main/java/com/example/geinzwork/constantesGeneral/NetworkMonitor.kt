package com.example.geinzwork.constantesGeneral

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.net.NetworkRequest
import android.os.Build

object NetworkMonitor {

    private var isConnected = true
    private val listeners = mutableSetOf<(Boolean) -> Unit>()

    fun init(context: Context) {
        // Detecta el estado actual
        isConnected = checkInternet(context)
        notifyListeners()

        // Registrar BroadcastReceiver para escuchar cambios
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val currentStatus = checkInternet(ctx!!)
                if (isConnected != currentStatus) {
                    isConnected = currentStatus
                    notifyListeners()
                }
            }
        }, filter)
    }

    fun isOnline(): Boolean = isConnected

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
        listener.invoke(isConnected) // 🔁 Notifica estado inmediato
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it.invoke(isConnected) }
    }

    private fun checkInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = cm.activeNetwork ?: return false
            val actNw = cm.getNetworkCapabilities(nw) ?: return false
            actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            val nwInfo: NetworkInfo? = cm.activeNetworkInfo
            nwInfo != null && nwInfo.isConnected
        }
    }
}

