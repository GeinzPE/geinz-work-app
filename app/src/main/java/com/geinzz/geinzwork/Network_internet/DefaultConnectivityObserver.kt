package com.geinzz.geinzwork.Network_internet

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

sealed interface ConnectivityObserver {
    enum class Status { Available, Unavailable, Losing, Lost }
    fun observe(): Flow<Status>
}
class DefaultConnectivityObserver (private val context: Context) : ConnectivityObserver {
    private val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun observe(): Flow<ConnectivityObserver.Status> = callbackFlow {
        // scope = this (ProducerScope)
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                // al detectar una red disponible, validamos si tiene internet real
                this@callbackFlow.launch {
                    val ok = isNetworkValidated(network)
                    trySend(if (ok) ConnectivityObserver.Status.Available else ConnectivityObserver.Status.Unavailable)
                }
            }

            override fun onLost(network: Network) {
                trySend(ConnectivityObserver.Status.Lost)
            }

            override fun onLosing(network: Network, maxMsToLive: Int) {
                trySend(ConnectivityObserver.Status.Losing)
            }

            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                // si Android ya validó la red, usamos esa info
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                    ) {
                        trySend(ConnectivityObserver.Status.Available)
                    } else {
                        // fallback: hacemos ping ligero para confirmar
                        this@callbackFlow.launch {
                            val ok = hasInternetByHttp()
                            trySend(if (ok) ConnectivityObserver.Status.Available else ConnectivityObserver.Status.Unavailable)
                        }
                    }
                } else {
                    trySend(ConnectivityObserver.Status.Unavailable)
                }
            }
        }

        // registrar callback (usa registerDefaultNetworkCallback si quieres escuchar red "por defecto")
        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (e: Exception) {
            // algunos devices pueden lanzar si no se puede registrar; enviamos Unavailable
            trySend(ConnectivityObserver.Status.Unavailable)
        }

        awaitClose {
            try { cm.unregisterNetworkCallback(callback) } catch (_: Exception) {}
        }
    }.distinctUntilChanged()

    private suspend fun isNetworkValidated(network: Network): Boolean {
        val caps = cm.getNetworkCapabilities(network) ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return true
        }
        // fallback general:
        return hasInternetByHttp()
    }

    private suspend fun hasInternetByHttp(): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://clients3.google.com/generate_204")
            (url.openConnection() as HttpURLConnection).run {
                connectTimeout = 1_500
                readTimeout = 1_500
                requestMethod = "GET"
                doInput = true
                connect()
                val code = responseCode
                disconnect()
                return@withContext (code == 204)
            }
        } catch (e: Exception) {
            return@withContext false
        }
    }

    fun initialNetworkState(): Boolean {
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}