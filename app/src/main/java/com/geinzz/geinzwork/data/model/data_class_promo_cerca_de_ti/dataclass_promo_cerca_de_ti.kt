package com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti

import android.R
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.google.firebase.Timestamp

data class obj_completo(
    val dataclass_promociones_cerca_de_ti: dataclass_promociones_cerca_de_ti = dataclass_promociones_cerca_de_ti(),
    val lista_filtrado: List<String> = emptyList(),
    val lista_tiendas_con_mas_promo: List<tiendas_con_mas_de_una_promo> = emptyList()
)

data class tiendas_con_mas_de_una_promo(
    val id: String = "",
    val logo_img: String = "",
    val nombre_tienda: String = ""
)

data class dataclass_promociones_cerca_de_ti(
    val informacion_publcacion: informacion_publcacion = informacion_publcacion(),
    val img: img_content = img_content(),
    val exclussivo: Boolean = false,
    val dias_restantes: String = "",
    val estadisticas: estadisticas_publiccaciones = estadisticas_publiccaciones(),
    val texto_msje_whatsapp: msjes_predeteminados_generales = msjes_predeteminados_generales(),
    val fecha_fin: Timestamp=Timestamp.now(),
    val estado_publicacion:String=""
)

data class estadisticas_publiccaciones(
    val total_clicks_whatsapp: Number = 0,
    val total_total_compartidos: Number = 0
)

data class img_content(
    val logo_img: String = "",
    val lista_img: List<String> = emptyList(),
)

data class informacion_publcacion(
    val descripcion: String = "",
    val numero: String = "",
    val titulo: String = "",
    val nombre_tienda: String = "",
    val id_promocion: String = "",
    val id_tienda: String = "",
    val categoria: String = "",
    val compartir: Boolean = false,
    val contactar: Boolean = false,
    val msjes_predeteminados_generales: msjes_predeteminados_generales = msjes_predeteminados_generales(),
)

data class ubicacion(
    val direccion: String = "",
    val lat: Number = 0,
    val lng: Number = 0,
    val ref: String = ""
)


data class compartir_contacto_pulicaciones(
    val id_promocion: String = "",
    val iod_tienda: String = "",
    val localidad_tineda: String = "",
    val categoria: String = "",
    val numero_contacto: String = "",
    val dias_restantes: String = "",
    val logo_img: String = "",
    val nombre_tienda: String = ""
)
