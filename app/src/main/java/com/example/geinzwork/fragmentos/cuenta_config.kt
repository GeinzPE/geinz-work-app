package com.example.geinzwork.fragmentos

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.databinding.ActivityCuentaConfigBinding

class cuenta_config : AppCompatActivity() {
    private lateinit var binding: ActivityCuentaConfigBinding
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
        setear_datos_include(
            binding.panelPublicacion.imgIcono,
            R.drawable.icono_panel_de_publicacion,
            "Panel de publicacion",
            binding.panelPublicacion.textoContenido
        )
        setear_datos_include(
            binding.containerPreview.imgIcono,
            R.drawable.preview_icon,
            "Vista de cuenta",
            binding.containerPreview.textoContenido
        )
        setear_datos_include(
            binding.qrTrabajador.imgIcono,
            R.drawable.qr_icon_color,
            "QR trabajador",
            binding.qrTrabajador.textoContenido
        )
        setear_datos_include(
            binding.vinculados.imgIcono,
            R.drawable.dispositivo_vinculado,
            "Dispositivos vinculados",
            binding.vinculados.textoContenido
        )
        setear_datos_include(
            binding.containerCerrarSeccion.imgIcono,
            R.drawable.cerra_seccion_icon,
            "Cerrar seccion",
            binding.containerCerrarSeccion.textoContenido
        )
        setear_datos_include(
            binding.containerEliminarCuenta.imgIcono,
            R.drawable.eliminar_user_icon,
            "Eliminar cuenta",
            binding.containerEliminarCuenta.textoContenido
        )
        setear_datos_include(
            binding.containerGuardados.imgIcono,
            R.drawable.guardados_icon,
            "Guardados",
            binding.containerGuardados.textoContenido
        )
        setear_datos_include(
            binding.containerReview.imgIcono,
            R.drawable.review_icons,
            "Reseñas",
            binding.containerReview.textoContenido
        )
        setear_datos_include(
            binding.panelPublicacion.imgIcono,
            R.drawable.icono_panel_de_publicacion,
            "Panel de publicacion",
            binding.panelPublicacion.textoContenido
        )
        setear_datos_include(
            binding.containerLocalizacion.imgIcono,
            R.drawable.localizacion_icon,
            "Direccion de envios",
            binding.containerLocalizacion.textoContenido
        )
        setear_datos_include(
            binding.lineaReportes.imgIcono,
            R.drawable.reporte_user,
            "Reportes",
            binding.lineaReportes.textoContenido
        )
        setear_datos_include(
            binding.historialCompra.imgIcono,
            R.drawable.compras_geinz_webp,
            "Historial de compra",
            binding.historialCompra.textoContenido
        )
        setear_datos_include(
            binding.historialVenta.imgIcono,
            R.drawable.ventas_geinz_webp,
            "Historial de venta",
            binding.historialVenta.textoContenido
        )

    }

    private fun setear_datos_include(
        imge_view: ImageView,
        img: Int,
        titulo_texto: String,
        texview: TextView,
    ) {
        Glide.with(this)
            .load(img)
            .into(imge_view)
        texview.text = titulo_texto


    }
}