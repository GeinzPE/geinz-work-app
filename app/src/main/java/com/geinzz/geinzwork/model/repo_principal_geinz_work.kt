package com.geinzz.geinzwork.model

import android.R
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.text.Html
import android.util.Log
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.datos_principales_user
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.localidades_filtrado
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.lugares_turisticos
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.seguridad_salud_publica

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await

class repo_principal_geinz_work {
    val db = FirebaseFirestore.getInstance()

    suspend fun obtener_lugares_turisticos(localidad: String): List<lugares_turisticos> {
        Log.d("localida_pasada", localidad)
        val lista_lugares = mutableListOf<lugares_turisticos>()
        val lugares_turisticos =
            db.collection("Tiendas").document(localidad.lowercase())
                .collection("lugares_turisticos")
                .get().await()
        for (datos in lugares_turisticos) {
            val data = datos.data
            val id = data?.get("id") as? String ?: ""
            val titulo = data?.get("titulo") as? String ?: ""
            val descripcion = data?.get("descripcion") as? String ?: ""
            val img_refencia = data?.get("img") as? Map<String, Any> ?: emptyMap()
            val lista_img_ref = img_refencia?.get("lista_img") as? List<String> ?: emptyList()
            val img_principal = img_refencia?.get("principal") as? String ?: ""
            val ubicacion = data?.get("ubicacion") as? Map<String, Any> ?: emptyMap()
            val dirección = ubicacion?.get("dirección") as? String ?: ""
            val referencia = ubicacion?.get("referencia") as? String ?: ""
            val longitud = ubicacion?.get("longitud") as? Number ?: 0
            val latitud = ubicacion?.get("latitud") as? Number ?: 0
            val lista_categorias = data?.get("categoria") as? List<String> ?: emptyList()

            val lista = lugares_turisticos(
                id,
                titulo,
                descripcion,
                lista_img_ref, img_principal,
                dirección,
                referencia,
                latitud.toDouble(),
                longitud.toDouble(), lista_categorias
            )
            lista_lugares.add(lista)
        }

        return lista_lugares
    }


    suspend fun obtenerLocalidadesFiltrados(): List<localidades_filtrado> {
        val localidadesSnapshot = db.collection("Tiendas")
            .document("categorias")
            .collection("localidades")
            .get()
            .await()

        return localidadesSnapshot.documents.mapNotNull { doc ->
            val nombre = doc.getString("nombre") ?: return@mapNotNull null
            val listaImg = doc.get("img") as? List<String> ?: emptyList()
            val aniversario = doc.get("aniversario") as? Map<String, Any> ?: emptyMap()
            val dia = aniversario.get("dia") as? Number ?: 0
            val mes = aniversario.get("mes") as? Number ?: 0
            val imgPrincipal = listaImg.randomOrNull() ?: ""
            localidades_filtrado(nombre, listOf(imgPrincipal), dia, mes)
        }
    }

    fun verificarControlVersiones(
        context: Context,
        callback: (versionRemota: String, debeActualizar: Boolean) -> Unit
    ) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        Log.d("VERSION_CHECK", "Iniciando verificación de versión...")

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds =3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)

        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->

                val fetchSuccess = task.isSuccessful
                Log.d("VERSION_CHECK", "Fetch remoto success = $fetchSuccess")

                if (!fetchSuccess) {
                    Log.d("VERSION_CHECK", "No se pudo obtener Remote Config → retornando default")
                    callback("0.0.0", false)
                    return@addOnCompleteListener
                }

                val versionRemota = remoteConfig.getString("latest_version").ifBlank { "0.0.0" }
                Log.d("VERSION_CHECK", "Versión remota recibida = $versionRemota")

                // Versión actual
                val versionActual = try {
                    val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }

                    (pkgInfo.versionName ?: "0.0.0").also {
                        Log.d("VERSION_CHECK", "Versión actual del dispositivo = $it")
                    }

                } catch (e: Exception) {
                    Log.e("VERSION_CHECK", "ERROR al obtener versión actual: ${e.message}")
                    "0.0.0"
                }

                if (!versionRemota.matches(Regex("""\d+(\.\d+)*"""))) {
                    Log.e("VERSION_CHECK", "Versión remota con formato inválido → $versionRemota")
                    callback(versionRemota, false)
                    return@addOnCompleteListener
                }

                val debeActualizar = esVersionMenor(versionActual, versionRemota)
                Log.d(
                    "VERSION_CHECK",
                    "Comparando versiones → actual=$versionActual  remota=$versionRemota  debeActualizar=$debeActualizar"
                )

                callback(versionRemota, debeActualizar)
            }
    }

    suspend fun txt_cambios_realziados(): String {
        return try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("controVersiones")
                .document("control")
                .get()
                .await()

            if (!snapshot.exists()) return ""

            val texto = snapshot.getString("texto") ?: ""

            if (texto.isNotEmpty()) {
                Html.fromHtml(texto, Html.FROM_HTML_MODE_COMPACT).toString()
            } else {
                ""
            }

        } catch (e: Exception) {
            ""
        }
    }


    fun esVersionMenor(actual: String, remota: String): Boolean {
        val a = actual.split(".")
        val b = remota.split(".")
        val max = maxOf(a.size, b.size)

        for (i in 0 until max) {
            val numA = a.getOrNull(i)?.toIntOrNull() ?: 0
            val numB = b.getOrNull(i)?.toIntOrNull() ?: 0

            if (numA < numB) return true
            if (numA > numB) return false
        }
        return false
    }



//    suspend fun obtenerDatosUser(idUser: String): datos_principales_user? {
//        val ref = db.collection("Trabajadores_Usuarios_Drivers")
//            .document("users")
//            .collection("users")
//            .document(idUser)
//            .get()
//            .await()
//
//        return if (ref.exists()) {
//            ref.toObject(datos_principales_user::class.java)
//        } else {
//            null
//        }
//    }

//    suspend fun subir_lugares(lista: List<seguridad_salud_publica>) {
//        lista.forEach { i ->
//            val ref = db.collection("Tiendas")
//                .document("salud_seguridad")
//                .collection(i.localidad)
//                .document()
//            val generatedId = ref.id
//
//            val hasmap_normal = hashMapOf<String, Any>(
//                "nombre" to i.nombre,
//                "lugar" to i.localidad,
//                "img" to i.img,
//                "categoria" to i.tipo,
//                "ubicacion" to i.datos_ubi,
//                "numeros_contactos" to i.numero_contacto,
//                "id" to generatedId
//            )
//
//            try {
//                ref.set(hasmap_normal, SetOptions.merge()).await()
//                Log.d("Firestore", "Documento subido con ID: $generatedId")
//
//                val ref2 = db.collection("lugares").document(generatedId)
//                val hashMap_algolia = hashMapOf<String, Any>(
//                    "nombre" to i.nombre,
//                    "lugar" to i.localidad,
//                    "img" to i.img,
//                    "categoria" to i.tipo,
//                    "id_tienda" to generatedId
//                )
//                ref2.set(hashMap_algolia, SetOptions.merge()).await()
//                Log.d("Firestore", "Documento en 'lugares' creado con ID: $generatedId")
//            } catch (e: Exception) {
//                Log.e("Firestore", "Error subiendo documento: ", e)
//            }
//        }
//    }


}