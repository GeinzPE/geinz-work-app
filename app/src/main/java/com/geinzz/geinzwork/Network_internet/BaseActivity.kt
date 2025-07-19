package com.geinzz.geinzwork.Network_internet

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.geinzz.geinzwork.utils.constantes.constantes.NetworkMonitor
import com.google.android.material.snackbar.Snackbar

abstract class BaseActivity : AppCompatActivity() {

    private var snackbar: Snackbar? = null

    abstract fun getRootView(): View

    private val listener: (Boolean) -> Unit = { isConnected ->
        if (isConnected) {
            ocultarSnackbar()
        } else {
            mostrarSnackbar("Sin conexión a internet")
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // ✅ Aquí ya setContentView fue ejecutado, puedes usar getRootView() seguro
        NetworkMonitor.addListener(listener)
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkMonitor.removeListener(listener)
    }

    private fun mostrarSnackbar(msg: String) {
        val view = getRootView()
        if (snackbar == null || !snackbar!!.isShown) {
            snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_INDEFINITE)
            snackbar?.show()
        } else {
            snackbar?.setText(msg)
        }
    }

    private fun ocultarSnackbar() {
        snackbar?.dismiss()
        snackbar = null
    }

    override fun onResume() {
        super.onResume()
        // 👇 Esto asegura que al volver a la actividad, se actualiza el estado visual
        if (!NetworkMonitor.isOnline()) {
            mostrarSnackbar("Sin conexión a internet")
        } else {
            ocultarSnackbar()
        }
    }

}