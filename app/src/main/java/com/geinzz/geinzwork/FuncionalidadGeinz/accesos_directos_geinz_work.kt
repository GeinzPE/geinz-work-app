package com.geinzz.geinzwork.FuncionalidadGeinz

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general.panel_publicacion_trabajador_accesoDirecto_panel
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general.qr_trabajador_accesoDirecto_panel
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general.servicios_accesoDirecto_panel
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general.vinculados_accesoDirecto_panel
import com.geinzz.geinzwork.utils.constantes.constantes.PinShortcut_general.vista_cuenta_accesoDirecto_panel
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityAccesosDirectosGeinzWorkBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class accesos_directos_geinz_work : AppCompatActivity() {
    private lateinit var binding: ActivityAccesosDirectosGeinzWorkBinding
    private lateinit var firebaseAuth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.N_MR1)
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
        firebaseAuth= FirebaseAuth.getInstance()

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

        binding.serviciosGeinz.tituloTextoAgregado.text = "Servicios Geinz"
        binding.serviciosGeinz.textoAgregado.text =
            "Ten acceso directo a tus publicaciones en Geinz work"
        binding.serviciosGeinz.iconoAccesoDirecto.setImageResource(R.drawable.ic_pinned_servicios_geinz)


        // Definición de los IDs y los mensajes para deshabilitar
        val panelShortcutId = "geinzwork_main_app_pinned_shortcut_panel"
        val qrTrabajadorShortcutId = "geinzwork_pinned_shortcut_qr_trabajador"
        val dispositivosShortcutId = "geinzwork_pinned_shortcut_dispositvos"
        val PreviewShortcutId = "geinzwork_pinned_shortcut_vista_previa"
        val ServiciosShortcutId = "geinzwork_pinned_shortcut_servicios"

        val panelDisabledMsg = "Panel de Publicaciones ha sido deshabilitado."
        val qrDisabledMsg = "QR Trabajador ha sido deshabilitado."
        val dispositivosDisabledMsg = "Dispositivos Vinculados ha sido deshabilitado."
        val serviciosDisabledMsg = "Servicios Geinz ha sido deshabilitado."

        val PreviewDisabledMsg = "Vista previa deshabilitado"

        // 1. Panel de Publicaciones
        val shortcutManager = getSystemService(ShortcutManager::class.java)

        // Función auxiliar para verificar si el acceso está activo (existe y está habilitado)


// 1. Panel de Publicaciones
        binding.panelPublicaciones.encenderIcono.isChecked =
            isAppShortcutActive(this, panelShortcutId)
        binding.panelPublicaciones.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                panel_publicacion_trabajador_accesoDirecto_panel(this)
                enableAppShortcut(this, panelShortcutId)
            } else {
                disableAppShortcut(this, panelShortcutId, panelDisabledMsg)
            }
        }

// 2. QR Trabajador
        binding.qrTrabajador.encenderIcono.isChecked =
            isAppShortcutActive(this, qrTrabajadorShortcutId)
        binding.qrTrabajador.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                qr_trabajador_accesoDirecto_panel(this)
                enableAppShortcut(this, qrTrabajadorShortcutId)
            } else {
                disableAppShortcut(this, qrTrabajadorShortcutId, qrDisabledMsg)
            }
        }

// 3. Dispositivos Vinculados
        binding.dispositivosVinculados.encenderIcono.isChecked =
            isAppShortcutActive(this, dispositivosShortcutId)
        binding.dispositivosVinculados.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vinculados_accesoDirecto_panel(this)
                enableAppShortcut(this, dispositivosShortcutId)
            } else {
                disableAppShortcut(this, dispositivosShortcutId, dispositivosDisabledMsg)
            }
        }

// 4. Preview Cuenta
        binding.previewCuenta.encenderIcono.isChecked = isAppShortcutActive(this, PreviewShortcutId)
        binding.previewCuenta.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
            val existing = shortcutManager?.pinnedShortcuts?.find { it.id == PreviewShortcutId }

            if (isChecked) {
                if (existing != null) {
                    if (!existing.isEnabled) {
                        shortcutManager.enableShortcuts(listOf(PreviewShortcutId))
                    }
                } else {
                    vista_cuenta_accesoDirecto_panel(this)
                }
                enableAppShortcut(this, PreviewShortcutId)
            } else {
                disableAppShortcut(this, PreviewShortcutId, PreviewDisabledMsg)
            }
        }

        verificar_servicios(firebaseAuth.uid.toString()) { existe ->
            if (!existe) {
                // No permitir interacción, lo bloqueamos
                binding.serviciosGeinz.encenderIcono.isEnabled = false
                binding.serviciosGeinz.encenderIcono.isChecked = false
                binding.serviciosGeinz.realativeIcon.setBackgroundResource(R.drawable.round_griss_desible)
            } else {
                // Permitir interacción
                binding.serviciosGeinz.realativeIcon.setBackgroundResource(R.drawable.round_violeta)
                binding.serviciosGeinz.encenderIcono.isEnabled = true
                binding.serviciosGeinz.encenderIcono.isChecked =
                    isAppShortcutActive(this, ServiciosShortcutId)
                binding.serviciosGeinz.encenderIcono.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        servicios_accesoDirecto_panel(this)
                        enableAppShortcut(this, ServiciosShortcutId)
                    } else {
                        disableAppShortcut(this, ServiciosShortcutId, serviciosDisabledMsg)
                    }
                }
            }
        }
    }

    fun isAppShortcutActive(context: Context, shortcutId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val shortcutManager = context.getSystemService(ShortcutManager::class.java)
            val shortcut = shortcutManager?.pinnedShortcuts?.find { it.id == shortcutId }
            return shortcut != null && shortcut.isEnabled
        }
        return false
    }

    fun isAppShortcutPinned(context: Context, shortcutId: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // Android 8.0 (API 26) o superior
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
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
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            try {
                // disableShortcuts deshabilita el acceso directo. Si el usuario lo pulsa,
                // verá el mensaje 'disabledMessage'.
                // ¡IMPORTANTE!: Esto NO lo elimina de la pantalla de inicio del usuario,
                // solo lo hace no funcional y muestra un mensaje.
                shortcutManager.disableShortcuts(listOf(shortcutId), disabledMessage)
                Toast.makeText(
                    context,
                    "El acceso directo ha sido deshabilitado. Si lo tienes en tu pantalla de inicio, por favor, elimínalo manualmente.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                // Manejar posibles excepciones, aunque disableShortcuts es bastante robusta.
                Toast.makeText(
                    context,
                    "No se pudo deshabilitar el acceso directo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "La deshabilitación de accesos directos requiere Android 7.1 o superior.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // --- Función para habilitar un acceso directo (si fue deshabilitado) ---
    fun enableAppShortcut(context: Context, shortcutId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            val shortcutManager =
                context.getSystemService(Context.SHORTCUT_SERVICE) as ShortcutManager
            try {
                // enableShortcuts lo vuelve a hacer funcional.
                shortcutManager.enableShortcuts(listOf(shortcutId))
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "No se pudo habilitar el acceso directo: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun verificar_servicios(idTrabajador: String, existe: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        val refs = listOf(
            db.collection("solicitudes_servicios").document("publicidad_baner")
                .collection("activos").document(idTrabajador),
            db.collection("solicitudes_servicios").document("publicidad_noticia")
                .collection("activos").document(idTrabajador),
            db.collection("solicitudes_servicios").document("verificaciones").collection("activos")
                .document(idTrabajador)
        )

        var checksCompleted = 0
        var found = false

        for (ref in refs) {
            ref.get().addOnSuccessListener { res ->
                checksCompleted++
                if (res.exists() && !found) {
                    found = true
                    existe(true)
                } else if (checksCompleted == refs.size && !found) {
                    existe(false)
                }
            }.addOnFailureListener {
                checksCompleted++
                if (checksCompleted == refs.size && !found) {
                    existe(false)
                }
            }
        }
    }


}