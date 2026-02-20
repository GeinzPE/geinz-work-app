package com.geinzz.geinzwork.model

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.geinzz.geinzwork.R
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.dataclass_promociones_cerca_de_ti
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.estadisticas_publiccaciones
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.img_content
import com.geinzz.geinzwork.data.model.data_class_promo_cerca_de_ti.informacion_publcacion
import com.geinzz.geinzwork.data.model.dataclass_promos.promociones_tiendas_negocios
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.geinzz.geinzwork.data.model.mensaje_predeterminado
import com.geinzz.geinzwork.data.model.msjes_predeteminados_generales
import com.geinzz.geinzwork.herramientas_geinz.constantes.constantes_datos_expirados_fechas_publicaciones
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.collections.get
import kotlin.math.ceil
import kotlin.math.floor

class repo_obtener_datos_promociones {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtner_datos_promocion(
        id_tienda: String,
        localidad: String,
        index: String // ID REAL de la promoción
    ): promociones_tiendas_negocios {

        val ref = db
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(id_tienda)
            .get()
            .await()

        if (!ref.exists()) {
            return promociones_tiendas_negocios()
        }

        val datos = ref.data ?: emptyMap()

        val nombre_tienda = datos["nombre_tienda"] as? String ?: ""

        val metodo_contacto = datos["metodo_contacto"] as? Map<*, *>
        val wsap_numero = metodo_contacto
            ?.get("whatsapp") as? Map<*, *>

        val numero_contacto =
            wsap_numero?.get("numero")?.toString() ?: ""

        val img_tienda = datos["img_tienda"] as? Map<String, Any>
        val logo_tienda = img_tienda?.get("logo_tienda") as? String ?: ""
        val localidad_db = datos["localidad"] as? String ?: ""

        val lista_img = img_tienda?.get("lista_img") as? Map<String, Any>
        val promociones_map =
            lista_img?.get("promociones") as? Map<String, String>
                ?: emptyMap()

        val categoria = datos["categoria_tienda"] as? String ?: ""

        // 🔥 SOLO la imagen del ID exacto
        val img_principal = promociones_map[index] ?: ""

        return promociones_tiendas_negocios(
            id_tienda = id_tienda,
            nombre_tienda = nombre_tienda,
            url_img = img_principal,
            numero_contacto_teinda = numero_contacto,
            img_logo_tienda = logo_tienda,
            localidad = localidad_db,
            categoria = categoria,"promocion_perfil","","","",Timestamp.now()
        )
    }


    suspend fun obtner_datos_promocion_notificacion(
        id_tienda: String,
        localidad: String,
        id_promo: String,
    ): promociones_tiendas_negocios {

//        Log.d("PROMO_DEBUG", "Obteniendo datos para id_tienda=$id_tienda, localidad=$localidad, id_promo=$id_promo")

        // Para id de 7 dígitos
        if (id_promo.length == 7) {
//            Log.d("PROMO_DEBUG", "Caso: ID de 7 dígitos")

            val ref = db
                .collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("notificaciones_enviadas")
                .document(id_promo)
                .get()
                .await()

            val datos = ref.data ?: emptyMap<String, Any>()
//            Log.d("PROMO_DEBUG", "Datos recibidos: $datos")

            val params_notificacion = datos["params_notificacion"] as? Map<String, Any> ?: emptyMap()
            val datos_de_notificacion=datos["datos_de_notificacion"] as? Map<String, Any> ?: emptyMap()
            val fecha_caducidad= datos["fecha_caducidad"] as? Timestamp?:Timestamp.now()
            //            Log.d("PROMO_DEBUG", "Params_notificacion: $params_noti")

            val img_container = datos_de_notificacion["img_notificacion"] as? String ?: ""
            val logo_img = datos_de_notificacion["logo_notificacion"] as? String ?: ""
            val nombre_tienda = datos_de_notificacion["nombre_tienda"] as? String ?: ""
            val numero = datos_de_notificacion["numero_contacto"] as? String ?: ""
            val categoria = datos_de_notificacion["categoria_tienda"] as? String ?: ""
            val id_img_storage = datos_de_notificacion["id_img_storage"] as? String ?: ""
            val id_notificaicon_datos=params_notificacion["id_noti"] as? String?:""
            val msje_predeterminado=params_notificacion["msje_predeterminado"] as? String?:""

//            Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios")
            return promociones_tiendas_negocios(
                id_tienda = id_tienda,
                nombre_tienda = nombre_tienda,
                url_img = img_container,
                numero_contacto_teinda = numero,
                img_logo_tienda = logo_img,
                localidad = localidad,
                categoria = categoria,"notifiacion_promo_solo_seguidores",id_img_storage,id_notificaicon_datos,msje_predeterminado,fecha_caducidad
            )

            // Para id de 9 dígitos
        } else if (id_promo.length == 9) {
//            Log.d("PROMO_DEBUG", "Caso: ID de 9 dígitos")

            val ref1 = db
                .collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("notificaciones_enviadas")
                .document(id_promo)
                .get()
                .await()

            val datos1 = ref1.data ?: emptyMap<String, Any>()
//            Log.d("PROMO_DEBUG", "Datos notificacion enviados: $datos1")

            val params_noti = datos1["params_notificacion"] as? Map<String, Any> ?: emptyMap()
            val fecha_caducidad= datos1["fecha_caducidad"] as? Timestamp?:Timestamp.now()
//            Log.d("PROMO_DEBUG", "Params_notificacion: $params_noti")

            val id_promocionnoti = params_noti["id_noti"] as? String ?: ""

            if (id_promocionnoti.length == id_promo.length) {
//                Log.d("PROMO_DEBUG", "ID promocion coincide con ID notificación")
                val id_publicacion_anuncio = params_noti["id_publicacion_anuncio"] as? String ?: ""
                val ref = db
                    .collection("Tiendas")
                    .document(localidad)
                    .collection(localidad)
                    .document(id_tienda)
                    .collection("promociones_geinz")
                    .document(id_publicacion_anuncio)
                    .get()
                    .await()

                val datos = ref.data ?: emptyMap<String, Any>()
//                Log.d("PROMO_DEBUG", "Datos promocion geinz: $datos")

                val informacion_notificacion =
                    datos["informacion"] as? Map<String, Any> ?: emptyMap()

                val img_container = datos["img_container"] as? Map<String, Any> ?: emptyMap()
                val lista_img = img_container["lista_img"] as? List<String> ?: emptyList()
                val logo_img = img_container["logo_img"] as? String ?: ""
                val nombre_tienda = informacion_notificacion["nombre_tienda"] as? String ?: ""
                val numero = informacion_notificacion["numero"] as? String ?: ""
                val categoria = informacion_notificacion["categoria"] as? String ?: ""
                val id_notificaicon_datos=params_noti["id_noti"] as? String?:""
                val msje_predeterminado=params_noti["msje_predeterminado"] as? String?:""
//                Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios con primera imagen de lista: ${lista_img.firstOrNull()}")
                return promociones_tiendas_negocios(
                    id_tienda = id_tienda,
                    nombre_tienda = nombre_tienda,
                    url_img = lista_img.firstOrNull() ?: "",
                    numero_contacto_teinda = numero,
                    img_logo_tienda = logo_img,
                    localidad = localidad,
                    categoria = categoria,"notifiacion_promo",id_publicacion_anuncio,id_notificaicon_datos,msje_predeterminado,fecha_caducidad
                )
            }
        } else if (id_promo.length > 9) {
            val ref = db
                .collection("Tiendas")
                .document(localidad)
                .collection(localidad)
                .document(id_tienda)
                .collection("promociones_geinz")
                .document(id_promo)
                .get()
                .await()
            val datos = ref.data ?: emptyMap<String, Any>()
//                Log.d("PROMO_DEBUG", "Datos promocion geinz: $datos")
            val params_noti = datos["params_notificacion"] as? Map<String, Any> ?: emptyMap()
            val datos_de_notificacion=datos["datos_de_notificacion"] as? Map<String, Any> ?: emptyMap()
            val informacion_notificacion = datos["informacion"] as? Map<String, Any> ?: emptyMap()
            val img_container = datos["img_container"] as? Map<String, Any> ?: emptyMap()
            val lista_img = img_container["lista_img"] as? List<String> ?: emptyList()
            val logo_img = img_container["logo_img"] as? String ?: ""
            val nombre_tienda = informacion_notificacion["nombre_tienda"] as? String ?: ""
            val numero = informacion_notificacion["numero"] as? String ?: ""
            val categoria = informacion_notificacion["categoria"] as? String ?: ""
            val id_img_storage = datos_de_notificacion["id_img_storage"] as? String ?: ""
            val id_notificaicon_datos=params_noti["id_noti"] as? String?:""
            val msje_predeterminado=params_noti["msje_predeterminado"] as? String?:""

//                Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios con primera imagen de lista: ${lista_img.firstOrNull()}")
            return promociones_tiendas_negocios(
                id_tienda = id_tienda,
                nombre_tienda = nombre_tienda,
                url_img = lista_img.firstOrNull() ?: "",
                numero_contacto_teinda = numero,
                img_logo_tienda = logo_img,
                localidad = localidad,
                categoria = categoria,"notifiacion_promo_simple",id_img_storage,id_notificaicon_datos,msje_predeterminado,Timestamp.now()
            )
        }

        // fallback por si no entra en ningún if
//        Log.d("PROMO_DEBUG", "No se encontró promo, retornando objeto vacío")
        return promociones_tiendas_negocios(
            id_tienda = id_tienda,
            nombre_tienda = "",
            url_img = "",
            numero_contacto_teinda = "",
            img_logo_tienda = "",
            localidad = localidad,
            categoria = "","notifiacion_promo","","","",Timestamp.now()
        )
    }

    suspend fun   obtener_datos_promociones_scroll_infinito_compartido(
        localidad: String,
        id_promo: String
    ): dataclass_promociones_cerca_de_ti {
        val ref = db.collection("Tiendas")
            .document(localidad)
            .collection("promos_ofertas")
            .document(id_promo)
            .get()
            .await()

        if (!ref.exists()) return dataclass_promociones_cerca_de_ti()

        val datos = ref.data ?: emptyMap<String, Any>()
        val estado = datos["estado"] as? String ?: "expirado"

        if (estado != "activo") {
            return dataclass_promociones_cerca_de_ti()
        }

        val tipo_hora_dias = datos["tipo_hora_dias"] as? String ?: ""
        val img_container = datos["img_container"] as? Map<*, *>
        val informacion = datos["informacion"] as? Map<*, *>
        val mensaje_predeterminado = datos["mensaje_predeterminado"] as? Map<*, *>
        val whatsapp_msje = mensaje_predeterminado?.get("whatsapp") as? Map<*, *>
        val compartir_msje = mensaje_predeterminado?.get("compartir") as? Map<*, *>

        val logo = img_container?.get("logo_img") as? String ?: ""
        val lista_img_container = img_container?.get("lista_img") as? List<String> ?: emptyList()

        val id_promocion = informacion?.get("id_promocion") as? String ?: ""
        val id_tienda = informacion?.get("id_tienda") as? String ?: ""
        val nombre_tienda = informacion?.get("nombre_tienda") as? String ?: ""
        val titulo = informacion?.get("titulo") as? String ?: ""
        val descripcion = informacion?.get("descripcion") as? String ?: ""
        val categoria = informacion?.get("categoria") as? String ?: ""
        val compartir = informacion?.get("compartir") as? Boolean ?: false
        val contactar = informacion?.get("contactar") as? Boolean ?: false
        val numero = informacion?.get("numero") as? String ?: ""
        val terminos_clave=datos.get("terminos_clave") as? List<String> ?: emptyList()

        val compartir_msj_bool = compartir_msje?.get("activo_o_no") as? Boolean ?: false
        val compartir_msj = compartir_msje?.get("msje_predermindo") as? String ?: ""
        val wsap_msj_bool = whatsapp_msje?.get("activo_o_no") as? Boolean ?: false
        val wsap_msj = whatsapp_msje?.get("msje_predermindo") as? String ?: ""
        val datos_hora_fecha = datos.get("datos_hora_fecha") as? Map<*, *> ?: emptyMap<String, Any>()
        val horasMap = datos_hora_fecha["horas"] as? Map<*, *> ?: emptyMap<String, Any>()
        val diasMap = datos_hora_fecha["dias"] as? Map<*, *> ?: emptyMap<String, Any>()
        val comodidades_filtro =datos.get("comodidades") as? Map<String, Boolean> ?:emptyMap()
        val pagos =datos.get("pagos") as? Map<String, Boolean> ?:emptyMap()
        val rango_precio =datos.get("rango_establecido") as?String?:""
        val precio =datos.get("precio_publicacion") as?String?:""
        val timestampFin = when (tipo_hora_dias) {
            "horas" -> (horasMap["timestamp_fin"] as? Timestamp)
            "dias" -> (diasMap["timestamp_fin"]  as? Timestamp)
            else -> null
        }
        // 🔹 Ajuste a milisegundos si estuviera en segundos
        val tiempo = timestampFin?.let {
            constantes_datos_expirados_fechas_publicaciones.tiempoRestante(
                it
            )
        } ?: "Expirado"




        return dataclass_promociones_cerca_de_ti(
            informacion_publcacion = informacion_publcacion(
                descripcion = descripcion,
                numero = numero,
                titulo = titulo,
                nombre_tienda = nombre_tienda,
                id_promocion = id_promocion,
                id_tienda = id_tienda,
                categoria = categoria,
                compartir = compartir,
                contactar = contactar,
                msjes_predeteminados_generales = msjes_predeteminados_generales(
                    compartir = mensaje_predeterminado(msje_predermindo = compartir_msj, activo_o_no = compartir_msj_bool),
                    whatsapp = mensaje_predeterminado(msje_predermindo = wsap_msj, activo_o_no = wsap_msj_bool)
                )
            ),
            img = img_content(logo_img = logo, lista_img = lista_img_container),
            exclussivo = false,
            dias_restantes = tiempo,
            estadisticas = estadisticas_publiccaciones(total_clicks_whatsapp = 0, total_total_compartidos = 0),
            texto_msje_whatsapp = msjes_predeteminados_generales(
                compartir = mensaje_predeterminado(msje_predermindo = compartir_msj, activo_o_no = compartir_msj_bool),
                whatsapp = mensaje_predeterminado(msje_predermindo = wsap_msj, activo_o_no = wsap_msj_bool)
            ),fecha_fin=timestampFin?: Timestamp.now(),estado,comodidades_filtro,pagos,rango_precio,precio,terminos_clave
        )
    }


    fun tiempoRestante(timestampFin: Long): String {
        val ahoraMs = System.currentTimeMillis()
        val diffMs = timestampFin - ahoraMs

        if (diffMs <= 0) return "Expirado"

        val totalHoras = diffMs.toDouble() / (1000 * 60 * 60)
        val totalMinutos = diffMs.toDouble() / (1000 * 60)

        return if (totalHoras >= 24) {
            // Mostrar días completos restantes
            val dias = ceil(totalHoras / 24).toLong()  // +1 implícito para el día actual
            "$dias ${if (dias == 1L) "día" else "días"} restantes"
        } else {
            // Mostrar horas y minutos restantes
            val horas = floor(totalHoras).toLong()
            val minutos = floor(totalMinutos % 60).toLong()
            when {
                horas > 0 && minutos > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} y $minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                horas > 0 -> "$horas ${if (horas == 1L) "hora" else "horas"} restantes"
                minutos > 0 -> "$minutos ${if (minutos == 1L) "minuto" else "minutos"} restantes"
                else -> "Menos de un minuto restante"
            }
        }
    }

}