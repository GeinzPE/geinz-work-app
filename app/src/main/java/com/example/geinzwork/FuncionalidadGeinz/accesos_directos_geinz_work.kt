package com.example.geinzwork.FuncionalidadGeinz

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.geinzwork.constantesGeneral.PinShortcut_general.panel_publicacion_trabajador_accesoDirecto_panel
import com.example.geinzwork.constantesGeneral.PinShortcut_general.qr_trabajador_accesoDirecto_panel
import com.example.geinzwork.constantesGeneral.PinShortcut_general.vinculados_accesoDirecto_panel
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityAccesosDirectosGeinzWorkBinding

class accesos_directos_geinz_work : AppCompatActivity() {
    private lateinit var binding: ActivityAccesosDirectosGeinzWorkBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccesosDirectosGeinzWorkBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.panelPublicaciones.tituloTextoAgregado.text = "Panel de publicaciones"
        binding.panelPublicaciones.textoAgregado.text =
            "Gestiona y crea tus publicaciones de forma rápida desde aquí"
        binding.panelPublicaciones.iconoAccesoDirecto.setImageResource(R.drawable.ic_pinned_panel_publicacion)

        binding.previewCuenta.tituloTextoAgregado.text = "Vista previa de cuenta"
        binding.previewCuenta.textoAgregado.text =
            "Visualiza cómo se muestra tu perfil públicamente"
        binding.previewCuenta.iconoAccesoDirecto.setImageResource(R.drawable.ic_pinned_preview)

        binding.qrTrabajador.tituloTextoAgregado.text = "QR Trabajador"
        binding.qrTrabajador.textoAgregado.text =
            "Comparte tu código QR para que otros te encuentren fácilmente"
        binding.qrTrabajador.iconoAccesoDirecto.setImageResource(R.drawable.ic_pinned_qr_trabajador)

        binding.dispositivosVinculados.tituloTextoAgregado.text = "Dispositivos vinculados"
        binding.dispositivosVinculados.textoAgregado.text =
            "Administra los dispositivos autorizados en tu cuenta"
        binding.dispositivosVinculados.iconoAccesoDirecto.setImageResource(R.drawable.ic_pinned_dispsitivos_v)


        // Definición de los IDs y los mensajes para deshabilitar
        val panelShortcutId = "geinzwork_main_app_pinned_shortcut_panel"
        val qrTrabajadorShortcutId = "geinzwork_pinned_shortcut_qr_trabajador"
        val dispositivosShortcutId = "geinzwork_pinned_shortcut_dispositvos"

        val panelDisabledMsg = "Panel de Publicaciones ha sido deshabilitado."
        val qrDisabledMsg = "QR Trabajador ha sido deshabilitado."
        val dispositivosDisabledMsg = "Dispositivos Vinculados ha sido deshabilitado."

        // 1. Panel de Publicaciones
        binding.panelPublicaciones.encenderIcono.isChecked = isAppShortcutPinned(this, panelShortcutId)
        binding.panelPublicaciones.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                panel_publicacion_trabajador_accesoDirecto_panel(this)
                enableAppShortcut(this, panelShortcutId) // Asegurarse de que esté habilitado funcionalmente
            } else {
                disableAppShortcut(this, panelShortcutId, panelDisabledMsg)
            }
        }

        // 2. QR Trabajador
        binding.qrTrabajador.encenderIcono.isChecked = isAppShortcutPinned(this, qrTrabajadorShortcutId)
        binding.qrTrabajador.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                qr_trabajador_accesoDirecto_panel(this)
                enableAppShortcut(this, qrTrabajadorShortcutId)
            } else {
                disableAppShortcut(this, qrTrabajadorShortcutId, qrDisabledMsg)
            }
        }

        // 3. Dispositivos Vinculados
        binding.dispositivosVinculados.encenderIcono.isChecked = isAppShortcutPinned(this, dispositivosShortcutId)
        binding.dispositivosVinculados.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vinculados_accesoDirecto_panel(this)
                enableAppShortcut(this, dispositivosShortcutId)
            } else {
                disableAppShortcut(this, dispositivosShortcutId, dispositivosDisabledMsg)
            }
        }

    }

    fun isAppShortcutPinned(context: Context, shortcutId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 (API 26) o superior
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            val pinnedShortcuts = shortcutManager.pinnedShortcuts

            // Recorremos la lista de accesos directos anclados y buscamos nuestro ID
            for (shortcutInfo in pinnedShortcuts) {
                if (shortcutInfo.id == shortcutId) {
                    return true // Encontrado, el acceso directo está anclado
                }
            }
        }
        return false // No está anclado o la versión de Android es inferior
    }

    fun disableAppShortcut(context: Context, shortcutId: String, disabledMessage: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) { // Android 7.1 (API 25) o superior
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            try {
                // disableShortcuts deshabilita el acceso directo. Si el usuario lo pulsa,
                // verá el mensaje 'disabledMessage'.
                // ¡IMPORTANTE!: Esto NO lo elimina de la pantalla de inicio del usuario,
                // solo lo hace no funcional y muestra un mensaje.
                shortcutManager.disableShortcuts(listOf(shortcutId), disabledMessage)
                Toast.makeText(context, "El acceso directo ha sido deshabilitado. Si lo tienes en tu pantalla de inicio, por favor, elimínalo manualmente.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                // Manejar posibles excepciones, aunque disableShortcuts es bastante robusta.
                Toast.makeText(context, "No se pudo deshabilitar el acceso directo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "La deshabilitación de accesos directos requiere Android 7.1 o superior.", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Función para habilitar un acceso directo (si fue deshabilitado) ---
    fun enableAppShortcut(context: Context, shortcutId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager = context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            try {
                // enableShortcuts lo vuelve a hacer funcional.
                shortcutManager.enableShortcuts(listOf(shortcutId))
            } catch (e: Exception) {
                Toast.makeText(context, "No se pudo habilitar el acceso directo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


}