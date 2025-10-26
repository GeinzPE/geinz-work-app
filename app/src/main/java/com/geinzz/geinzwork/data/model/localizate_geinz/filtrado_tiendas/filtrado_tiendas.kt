package com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import kotlinx.android.parcel.Parcelize


data class filtrado_tiendas_cat_sub(val categoria: String, val subcategorias: List<String>)

data class tiendas_filtradas(
    val logo_tienda: String = "",
    val img_tienda: List<String> = emptyList(),
    val nombre_tienda: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val lista_subcategoiras: List<String> = emptyList(),
    val descripcion: String = "",
    val id_tienda: String = "",
    val whatsapp: Boolean = false,
    val numero_whatsapp: String = "",
    val tiktok: Boolean = false,
    val nombre_tiktok: String = "",
    val sitio_web: Boolean = false,
    val url_sitio_web: String = "",
    val instagram: Boolean = false,
    val nombre_user_ig: String = "",
    val facebook: Boolean = false,
    val nombre_user_fb: String = ""

)

data class EstadoFiltrosUi(
    val subcategorias: List<filtrado_tiendas_cat_sub> = emptyList(),
    val tiendasFiltradas: List<tiendas_por_categoria> = emptyList(),
)

@Parcelize
data class tiendas_por_categoria(
    val nombre_tienda: String = "",
    val direccion: String = "",
    val referencia: String = "",
    val logo_tienda: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val lista_subcategoiras: List<String> = emptyList(),
    val descripcion: String = "",
    val id_tienda: String = "",
    val pagado: Boolean,
    val horario_dia: horario_tienda = horario_tienda(),
    val estaAbierto: Boolean = false,
    var contacto_tienda: metodo_contacto_tienda
) : Parcelable

data class lugares_cercanos(
    val nombre_tienda: String = "",
    val logo_tienda: String = "",
    val categoria: String = "",
    val lista_subcategoiras: List<String> = emptyList(),
    val id_tienda: String = "",
    val pagado: Boolean,
    val horario_dia: horario_tienda = horario_tienda(),
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val esta_abierto: Boolean = false,
    var contacto_tienda: metodo_contacto_tienda,
    var has_tienda:String
)

data class tiendas_cecanas_km(
    val img_tienda: String="",
    val nombre_tienda: String="",
    val kl: String="",
    val nombre_lugar: String="",val color: Color= Color.Transparent,
    val horario_total:horario_tienda=horario_tienda(),
    val hora_cierre: String="",
    val cerrado: Boolean=false,
    val motivo:String="",
    val tick:Long=0,
)

@Parcelize
data class horario_tienda(
    val h_apertura: String = "",
    val h_cierre: String = "",
    val cerrado: Boolean = false,
    val motivo: String = "",
    val dia_prox_apertura: String = "",
    val hora_prox_apertura: String = ""
) : Parcelable

//

data class obtener_tiendas_lat_log_id(
    val lat: Double,
    val log: Double,
    val id_tienda: String,
    val direccion: String,
    val referencia: String,
    val nombre_tienda: String
)

