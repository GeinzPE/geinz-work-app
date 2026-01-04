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
import com.geinzz.geinzwork.data.model.dataclass_promos.promociones_tiendas_negocios
import com.geinzz.geinzwork.data.model.dataclass_review.ImagenReview
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
            categoria = categoria
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

            val params_noti = datos["params_noti"] as? Map<String, Any> ?: emptyMap()
//            Log.d("PROMO_DEBUG", "Params_notificacion: $params_noti")

            val img_container = params_noti["img_notificacion"] as? String ?: ""
            val logo_img = params_noti["logo_notificacion"] as? String ?: ""
            val nombre_tienda = params_noti["nombre_tienda"] as? String ?: ""
            val numero = params_noti["numero_contacto"] as? String ?: ""
            val categoria = params_noti["categoria_tienda"] as? String ?: ""

//            Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios")
            return promociones_tiendas_negocios(
                id_tienda = id_tienda,
                nombre_tienda = nombre_tienda,
                url_img = img_container,
                numero_contacto_teinda = numero,
                img_logo_tienda = logo_img,
                localidad = localidad,
                categoria = categoria
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

                val informacion_notificacion = datos["informacion"] as? Map<String, Any> ?: emptyMap()
                val img_container = datos["img_container"] as? Map<String, Any> ?: emptyMap()
                val lista_img = img_container["lista_img"] as? List<String> ?: emptyList()
                val logo_img = img_container["logo_img"] as? String ?: ""
                val nombre_tienda = informacion_notificacion["nombre_tienda"] as? String ?: ""
                val numero = informacion_notificacion["numero"] as? String ?: ""
                val categoria = informacion_notificacion["categoria"] as? String ?: ""

//                Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios con primera imagen de lista: ${lista_img.firstOrNull()}")
                return promociones_tiendas_negocios(
                    id_tienda = id_tienda,
                    nombre_tienda = nombre_tienda,
                    url_img = lista_img.firstOrNull() ?: "",
                    numero_contacto_teinda = numero,
                    img_logo_tienda = logo_img,
                    localidad = localidad,
                    categoria = categoria
                )
            }
        }else if(id_promo.length>9){
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

            val informacion_notificacion = datos["informacion"] as? Map<String, Any> ?: emptyMap()
            val img_container = datos["img_container"] as? Map<String, Any> ?: emptyMap()
            val lista_img = img_container["lista_img"] as? List<String> ?: emptyList()
            val logo_img = img_container["logo_img"] as? String ?: ""
            val nombre_tienda = informacion_notificacion["nombre_tienda"] as? String ?: ""
            val numero = informacion_notificacion["numero"] as? String ?: ""
            val categoria = informacion_notificacion["categoria"] as? String ?: ""

//                Log.d("PROMO_DEBUG", "Preparando objeto promociones_tiendas_negocios con primera imagen de lista: ${lista_img.firstOrNull()}")
            return promociones_tiendas_negocios(
                id_tienda = id_tienda,
                nombre_tienda = nombre_tienda,
                url_img = lista_img.firstOrNull() ?: "",
                numero_contacto_teinda = numero,
                img_logo_tienda = logo_img,
                localidad = localidad,
                categoria = categoria
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
            categoria = ""
        )
    }











}