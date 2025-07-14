package com.example.geinzwork.fragmentos.cuenta_config

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.geinzwork.FuncionalidadGeinz.accesos_directos_geinz_work
import com.example.geinzwork.activity_dispositivos_vinculados
import com.example.geinzwork.constantesGeneral.Variables
import com.example.geinzwork.constantesGeneral.constantes_vinculados
import com.example.geinzwork.fragmentos.apartados_compra_venta.compra_trabajador
import com.example.geinzwork.fragmentos.apartados_compra_venta.venta_trabajador
import com.example.geinzwork.fragmentos.panel_publicacion_trabajador
import com.example.geinzwork.noticias_trabajadores_guardados
import com.example.geinzwork.vista_denuncia_reporte
import com.geinzz.geinzwork.GenerarQR_trabajador
import com.geinzz.geinzwork.MainActivity
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.constantesGeneral.constantes
import com.geinzz.geinzwork.constantesGeneral.constantes_cuenta_user
import com.geinzz.geinzwork.databinding.ActivityCuentaConfigBinding
import com.geinzz.geinzwork.noticias_y_review
import com.geinzz.geinzwork.vistaTiendas.direccion_entrega_lat_log
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth

class cuenta_config : AppCompatActivity(), OnIncludeSeleccionadoListener {
    private lateinit var dialog: BottomSheetDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: ActivityCuentaConfigBinding
    private val lista = mutableListOf<dataclass_cuenta_config_filtrado>()
    private lateinit var adapterFiltrado: adapter_filtrado_cuenta_config
    private var isCamposVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCuentaConfigBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding.retroceder.setOnClickListener {
            onBackPressed()
        }
        val plan = intent.getStringExtra("plan").toString()
        val tipo_cuenta = intent.getStringExtra("tipo_cuenta").toString()
        firebaseAuth = FirebaseAuth.getInstance()
        restaurarIncluidos()
        setear_datos_include(
            binding.serviciosGeinz.lineaApartado,
            binding.serviciosGeinz.imgIcono,
            R.drawable.geinz_circular_new,
            "Servicios Geinz",
            binding.serviciosGeinz.textoContenido,
            binding.serviciosGeinz.linealPadre,
            binding.serviciosGeinz.ocultarP1,
            binding.serviciosGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",

        )
        setear_datos_include(
            binding.funcionamientoGeinz.lineaApartado,
            binding.funcionamientoGeinz.imgIcono,
            R.drawable.como_funciona_geinz_webp,
            "Funcionamiento de Geinz",
            binding.funcionamientoGeinz.textoContenido,
            binding.funcionamientoGeinz.linealPadre,
            binding.funcionamientoGeinz.ocultarP1,
            binding.funcionamientoGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",
        )

        setear_datos_include(
            binding.pagosGeinz.lineaApartado,
            binding.pagosGeinz.imgIcono,
            R.drawable.pagos_geinz_webp,
            "Pagos Geinz",
            binding.pagosGeinz.textoContenido,
            binding.pagosGeinz.linealPadre,
            binding.pagosGeinz.ocultarP1,
            binding.pagosGeinz.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",

        )

        setear_datos_include(
            binding.cuentaVerifica.lineaApartado,
            binding.cuentaVerifica.imgIcono,
            R.drawable.icon_verificado,
            "Verificar cuenta",
            binding.cuentaVerifica.textoContenido,
            binding.cuentaVerifica.linealPadre,
            binding.cuentaVerifica.ocultarP1,
            binding.cuentaVerifica.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work.",

        )

        setear_datos_include(
            binding.panelPublicacion.lineaApartado,
            binding.panelPublicacion.imgIcono,
            R.drawable.icono_panel_de_publicacion,
            "Panel de publicación",
            binding.panelPublicacion.textoContenido,
            binding.panelPublicacion.linealPadre,
            binding.panelPublicacion.ocultarP1,
            binding.panelPublicacion.textoOcultar,
            "Administra y visualiza todas las publicaciones que has realizado en Geinz Work."
        )

        setear_datos_include(
            binding.accesoDirecto.lineaApartado,
            binding.accesoDirecto.imgIcono,
            R.drawable.accesos_directo_icon,
            "Accesos directos",
            binding.accesoDirecto.textoContenido,
            binding.accesoDirecto.linealPadre,
            binding.accesoDirecto.ocultarP1,
            binding.accesoDirecto.textoOcultar,
            "Accede rápidamente a funciones clave y apartados de tu cuenta desde este menú directo."
        )

        setear_datos_include(
            binding.containerPreview.lineaApartado,
            binding.containerPreview.imgIcono,
            R.drawable.preview_icon,
            "Vista de cuenta",
            binding.containerPreview.textoContenido,
            binding.containerPreview.linealPadre,
            binding.containerPreview.ocultarP1,
            binding.containerPreview.textoOcultar,
            "Visualiza cómo se muestra tu perfil público para otros usuarios de la plataforma."
        )

        setear_datos_include(
            binding.qrTrabajador.lineaApartado,
            binding.qrTrabajador.imgIcono,
            R.drawable.qr_icon_color_webp,
            "QR trabajador",
            binding.qrTrabajador.textoContenido,
            binding.qrTrabajador.linealPadre,
            binding.qrTrabajador.ocultarP1,
            binding.qrTrabajador.textoOcultar,
            "Comparte fácilmente tu perfil profesional a través de tu código QR único."
        )

        setear_datos_include(
            binding.vinculados.lineaApartado,
            binding.vinculados.imgIcono,
            R.drawable.dispositivo_vinculado,
            "Dispositivos vinculados",
            binding.vinculados.textoContenido,
            binding.vinculados.linealPadre,
            binding.vinculados.ocultarP1,
            binding.vinculados.textoOcultar,
            "Revisa y gestiona los dispositivos que tienen acceso a tu cuenta actualmente."
        )

        setear_datos_include(
            binding.containerCerrarSeccion.lineaApartado,
            binding.containerCerrarSeccion.imgIcono,
            R.drawable.cerra_seccion_icon,
            "Cerrar sesión",
            binding.containerCerrarSeccion.textoContenido,
            binding.containerCerrarSeccion.linealPadre,
            binding.containerCerrarSeccion.ocultarP1,
            binding.containerCerrarSeccion.textoOcultar,
            "Finaliza tu sesión de forma segura y protege tu cuenta."
        )

        setear_datos_include(
            binding.containerEliminarCuenta.lineaApartado,
            binding.containerEliminarCuenta.imgIcono,
            R.drawable.eliminar_user_icon,
            "Eliminar cuenta",
            binding.containerEliminarCuenta.textoContenido,
            binding.containerEliminarCuenta.linealPadre,
            binding.containerEliminarCuenta.ocultarP1,
            binding.containerEliminarCuenta.textoOcultar,
            "Elimina tu cuenta de manera permanente junto con toda tu información registrada."
        )

        setear_datos_include(
            binding.containerGuardados.lineaApartado,
            binding.containerGuardados.imgIcono,
            R.drawable.guardados_icon,
            "Guardados",
            binding.containerGuardados.textoContenido,
            binding.containerGuardados.linealPadre,
            binding.containerGuardados.ocultarP1,
            binding.containerGuardados.textoOcultar,
            "Consulta todos los elementos y publicaciones que has guardado para ver después."
        )

        setear_datos_include(
            binding.containerReview.lineaApartado,
            binding.containerReview.imgIcono,
            R.drawable.review_icons,
            "Reseñas",
            binding.containerReview.textoContenido,
            binding.containerReview.linealPadre,
            binding.containerReview.ocultarP1,
            binding.containerReview.textoOcultar,
            "Accede a las reseñas que has recibido o dejado sobre otros usuarios o servicios."
        )

        setear_datos_include(
            binding.containerLocalizacion.lineaApartado,
            binding.containerLocalizacion.imgIcono,
            R.drawable.localizacion_icon,
            "Dirección de envíos",
            binding.containerLocalizacion.textoContenido,
            binding.containerLocalizacion.linealPadre,
            binding.containerLocalizacion.ocultarP1,
            binding.containerLocalizacion.textoOcultar,
            "Gestiona y actualiza las direcciones para recibir productos o servicios."
        )

        setear_datos_include(
            binding.lineaReportes.lineaApartado,
            binding.lineaReportes.imgIcono,
            R.drawable.reporte_user,
            "Reportes",
            binding.lineaReportes.textoContenido,
            binding.lineaReportes.linealPadre,
            binding.lineaReportes.ocultarP1,
            binding.lineaReportes.textoOcultar,
            "Envía reportes sobre usuarios, contenido inapropiado o errores en la app."
        )

        setear_datos_include(
            binding.historialCompra.lineaApartado,
            binding.historialCompra.imgIcono,
            R.drawable.compras_geinz_webp,
            "Historial de compra",
            binding.historialCompra.textoContenido,
            binding.historialCompra.linealPadre,
            binding.historialCompra.ocultarP1,
            binding.historialCompra.textoOcultar,
            "Revisa todos los productos y servicios que has comprado desde tu cuenta."
        )

        setear_datos_include(
            binding.historialVenta.lineaApartado,
            binding.historialVenta.imgIcono,
            R.drawable.ventas_geinz_webp,
            "Historial de venta",
            binding.historialVenta.textoContenido,
            binding.historialVenta.linealPadre,
            binding.historialVenta.ocultarP1,
            binding.historialVenta.textoOcultar,
            "Consulta el historial completo de tus ventas y transacciones realizadas."
        )

        binding.verMas.setOnClickListener {
            dialog = BottomSheetDialog(this)
            constantes_cuenta_user.bottom_shett_config(dialog, this, this)
            dialog.show()
        }

        lista.addAll(agregar_lista_filtrado())
        adapterFiltrado = adapter_filtrado_cuenta_config(lista.toMutableList()) { item ->
            item.actividadDestino?.let {
                startActivity(Intent(this, it))
            } ?: run {
                when (item.titulo_texto) {
                    "Panel de publicación" -> abrirPanelPublicacion(plan)
                    "Vista de cuenta" -> abrirVistaCuenta()
                    "QR trabajador" -> abrirQRTrabajador()
                    "Accesos directos" -> abrirAccesosDirectos()
                    "Dispositivos vinculados" -> abrirDispositivosVinculados()
                    "Cerrar sesión" -> mostrarDialogCerrarSesion()
                    "Eliminar cuenta" -> mostrarDialogEliminarCuenta(tipo_cuenta, this)
                    "Guardados" -> abrirGuardados()
                    "Reseñas" -> abrirReseñas()
                    "Dirección de envíos" -> abrirDireccionEnvios()
                    "Reportes" -> abrirReportes(plan)
                    "Historial de compra" -> abrirHistorialCompra()
                    "Historial de venta" -> abrirHistorialVenta()
                }

            }
        }



        binding.recicleAjustes.layoutManager = LinearLayoutManager(this)
        binding.recicleAjustes.adapter = adapterFiltrado

        binding.editexFilter.addTextChangedListener { editable ->
            val texto = editable?.toString()?.trim() ?: ""

            if (texto.isEmpty()) {
                binding.layoutVisible.isVisible = true
                binding.recicleAjustes.isVisible = false
                binding.sinResultados.isVisible = false // ocultamos mensaje si lo hubiera
            } else {
                binding.layoutVisible.isVisible = false
                binding.recicleAjustes.isVisible = true

                val normalizar = { str: String ->
                    java.text.Normalizer.normalize(str.lowercase(), java.text.Normalizer.Form.NFD)
                        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
                }

                val textoNormalizado = normalizar(texto)

                val resultadosFiltrados = lista.filter { item ->
                    val titulo = normalizar(item.titulo_texto.toString())
                    titulo.contains(textoNormalizado)
                }

                // Mostrar mensaje si no hay coincidencias
                binding.sinResultados.isVisible = resultadosFiltrados.isEmpty()
                binding.recicleAjustes.isVisible = resultadosFiltrados.isNotEmpty()

                adapterFiltrado.actualizarLista(resultadosFiltrados)
            }
        }


    }

    private fun abrirGuardados() {
        val intent = Intent(this, noticias_trabajadores_guardados::class.java)
        intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
        startActivity(intent)

    }

    private fun abrirDispositivosVinculados() {
        val vista = Intent(this, activity_dispositivos_vinculados::class.java)
        startActivity(vista)

    }

    private fun abrirQRTrabajador() {
        startActivity(Intent(this, GenerarQR_trabajador::class.java))

    }

    private fun abrirVistaCuenta() {
        constantes.showLoadingDialog(
            this,
            3000,
            "Por favor, espere",
            "Cargando datos..."
        )
        constantes_cuenta_user.mandarDatos(firebaseAuth.uid.toString(), this)

    }

    private fun mostrarDialogCerrarSesion() {
        val alerta = AlertDialog.Builder(this)
        alerta.setTitle("Cerrar Sesión")
        alerta.setMessage("¿Está seguro de que desea cerrar sesión?")
        alerta.setPositiveButton("Sí") { dialog, which ->
            constantes_vinculados.cerrarSeccion(
                this,
                firebaseAuth.uid.toString()
            ) {
                firebaseAuth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            }
        }


        alerta.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }
        alerta.show()

    }

    private fun mostrarDialogEliminarCuenta(tipoCuenta: String, activity: Activity) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Eliminar cuenta")
        builder.setMessage("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.")
        builder.setPositiveButton("Sí") { dialogs, which ->
            constantes_cuenta_user.eliminarCuenta(
                tipoCuenta,
                firebaseAuth.uid.toString(),
                this,
                activity
            )
        }
        builder.setNegativeButton("No") { dialogs, which ->
            dialogs.dismiss()
        }
        val dialogs = builder.create()
        dialogs.show()

    }

    private fun abrirPanelPublicacion(plan: String) {
        var intent = Intent(this, panel_publicacion_trabajador::class.java).apply {
            putExtra(Variables.plan, plan)
        }
        startActivity(intent)

    }

    private fun agregar_lista_filtrado(): List<dataclass_cuenta_config_filtrado> {
        return listOf(
            dataclass_cuenta_config_filtrado(
                "Panel de publicación",
                R.drawable.icono_panel_de_publicacion,
                null,
                "Administra y visualiza todas las publicaciones que has realizado en Geinz Work."
            ),
            dataclass_cuenta_config_filtrado(
                "Accesos directos",
                R.drawable.accesos_directo_icon,
                null,
                "Accede rápidamente a tus secciones favoritas de la aplicación."
            ),
            dataclass_cuenta_config_filtrado(
                "Vista de cuenta",
                R.drawable.preview_icon,
                null,
                "Visualiza cómo otros usuarios ven tu perfil dentro de la plataforma."
            ),
            dataclass_cuenta_config_filtrado(
                "QR trabajador",
                R.drawable.qr_icon_color,
                null,
                "Genera y comparte tu código QR único como trabajador."
            ),
            dataclass_cuenta_config_filtrado(
                "Dispositivos vinculados",
                R.drawable.dispositivo_vinculado,
                null,
                "Gestiona los dispositivos que tienen acceso a tu cuenta."
            ),
            dataclass_cuenta_config_filtrado(
                "Cerrar sesión",
                R.drawable.cerra_seccion_icon,
                null,
                "Cierra tu sesión actual de forma segura en Geinz Work."
            ),
            dataclass_cuenta_config_filtrado(
                "Eliminar cuenta",
                R.drawable.eliminar_user_icon,
                null,
                "Elimina permanentemente tu cuenta y todos tus datos asociados."
            ),
            dataclass_cuenta_config_filtrado(
                "Guardados",
                R.drawable.guardados_icon,
                null,
                "Revisa todos los elementos que guardaste para ver después."
            ),
            dataclass_cuenta_config_filtrado(
                "Reseñas",
                R.drawable.review_icons,
                null,
                "Consulta y administra las reseñas que has recibido o dejado."
            ),
            dataclass_cuenta_config_filtrado(
                "Dirección de envíos",
                R.drawable.localizacion_icon,
                null,
                "Gestiona y actualiza tus direcciones de envío guardadas."
            ),
            dataclass_cuenta_config_filtrado(
                "Reportes",
                R.drawable.reporte_user,
                null,
                "Accede a reportes de actividad, errores o comportamiento inadecuado."
            ),
            dataclass_cuenta_config_filtrado(
                "Historial de compra",
                R.drawable.compras_geinz_webp,
                null,
                "Consulta todas las compras que realizaste en la plataforma."
            ),
            dataclass_cuenta_config_filtrado(
                "Historial de venta",
                R.drawable.ventas_geinz_webp,
                null,
                "Revisa el historial completo de tus ventas y transacciones."
            )

        )

    }
    private fun restaurarIncluidos() {
        val prefs = getSharedPreferences("includes_gz", MODE_PRIVATE)
        if (prefs.getBoolean("servicios_geinz", false)) onIncludeSeleccionado("servicios_geinz")
        if (prefs.getBoolean("funcionamiento_geinz", false)) onIncludeSeleccionado("funcionamiento_geinz")
        if (prefs.getBoolean("pagos_geinz", false)) onIncludeSeleccionado("pagos_geinz")
        if (prefs.getBoolean("verificar_cuenta", false)) onIncludeSeleccionado("verificar_cuenta")
    }


    private fun setear_datos_include(
        lineal_principal: LinearLayout,
        imageView: ImageView,
        img: Int,
        tituloTexto: String,
        textView: TextView,
        linealPadre: LinearLayout,
        icono_oculatar: ImageView,
        textView_ocultar: TextView,
        texto_setear: String
    ) {
        var visible = false
        lineal_principal.isVisible = true
        icono_oculatar.setOnClickListener {
            visible = !visible
            textView_ocultar.visibility = if (visible) View.VISIBLE else View.GONE
            icono_oculatar.setImageResource(
                if (visible) R.drawable.ocultar_abajo else R.drawable.ocultar_arriva
            )
        }

        textView_ocultar.text = texto_setear
        Glide.with(this)
            .load(img)
            .into(imageView)

        textView.text = tituloTexto
        val plan = intent.getStringExtra("plan").toString()
        val tipo_cuenta = intent.getStringExtra("tipo_cuenta").toString()
        linealPadre.setOnClickListener {
            when (tituloTexto) {
                "Panel de publicación" -> abrirPanelPublicacion(plan)
                "Vista de cuenta" -> abrirVistaCuenta()
                "QR trabajador" -> abrirQRTrabajador()
                "Accesos directos" -> abrirAccesosDirectos()
                "Dispositivos vinculados" -> abrirDispositivosVinculados()
                "Cerrar seccion" -> mostrarDialogCerrarSesion()
                "Eliminar cuenta" -> mostrarDialogEliminarCuenta(
                    tipoCuenta = tipo_cuenta,
                    activity = this
                )

                "Guardados" -> abrirGuardados()
                "Reseñas" -> abrirReseñas()
                "Direccion de envios" -> abrirDireccionEnvios()
                "Reportes" -> abrirReportes(plan)
                "Historial de compra" -> abrirHistorialCompra()
                "Historial de venta" -> abrirHistorialVenta()
            }
        }
    }

    private fun abrirAccesosDirectos() {
        startActivity(Intent(this, accesos_directos_geinz_work::class.java))
    }


    private fun abrirHistorialVenta() {
        startActivity(Intent(this, venta_trabajador::class.java))

    }

    private fun abrirHistorialCompra() {
        startActivity(Intent(this, compra_trabajador::class.java))

    }

    private fun abrirReportes(plan: String) {
        var vista = Intent(this, vista_denuncia_reporte::class.java).apply {
            putExtra(Variables.plan, plan)
        }
        this.startActivity(vista)

    }

    private fun abrirDireccionEnvios() {
        startActivity(Intent(this, direccion_entrega_lat_log::class.java))

    }

    private fun abrirReseñas() {
        val intent = Intent(this, noticias_y_review::class.java)
        intent.putExtra(Variables.iduser, firebaseAuth.uid.toString())
        intent.putExtra(Variables.title, "Tus Reseñas")
        startActivity(intent)

    }

    override fun onIncludeSeleccionado(id: String) {
        when (id) {
            "servicios_geinz" -> {
                binding.lineaView.isVisible = true
                binding.soloGeinz.isVisible = true
                binding.serviciosGeinz.lineaApartado.isVisible = true
            }

            "funcionamiento_geinz" -> {
                binding.lineaView.isVisible = true
                binding.soloGeinz.isVisible = true
                binding.funcionamientoGeinz.lineaApartado.isVisible = true
            }

            "pagos_geinz" -> {
                binding.lineaView.isVisible = true
                binding.soloGeinz.isVisible = true
                binding.pagosGeinz.lineaApartado.isVisible = true
            }

            "verificar_cuenta" -> {
                binding.lineaView.isVisible = true
                binding.soloGeinz.isVisible = true
                binding.cuentaVerifica.lineaApartado.isVisible = true
            }
        }

        guardarEnPreferencias(id)
    }

    private fun guardarEnPreferencias(id: String) {
        val prefs = getSharedPreferences("includes_gz", MODE_PRIVATE)
        prefs.edit().putBoolean(id, true).apply()
    }



}




