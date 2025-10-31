package com.geinzz.geinzwork.Network_internet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geinzz.geinzwork.App
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ConnectivityViewModel(app: Application) : AndroidViewModel(app) {
    private val observer = (app as App).connectivityObserver as DefaultConnectivityObserver

    // ✅ Detecta el estado real al iniciar la app
    private val initial = observer.initialNetworkState()

    val isConnected = observer.observe()
        .map { it == ConnectivityObserver.Status.Available }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)
}
