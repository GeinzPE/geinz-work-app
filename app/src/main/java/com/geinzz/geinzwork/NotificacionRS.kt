package com.geinzz.geinzwork

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import java.io.IOException
import kotlin.coroutines.resume

class NotificacionRS {
    private val FCM_URL = "https://fcm.googleapis.com/v1/projects/geinzworkapp/messages:send"
    private val CLOUD_FUNCTION_URL =
        "https://enviarnotificacion-oixttik5rq-uc.a.run.app"

    private val client = OkHttpClient()
    suspend fun sendNotification_con_parametros(
        idEnviado1: String,
        v1: String,
        Vista: String,
        context: Context,
        token: String,
        title: String,
        body: String,
    ) {
        val accessToken = getAccessToken(context)
        Log.e("token", "el token es $accessToken")
        Log.d("token_valores", "obtenemos los valoes$v1,$token")
        if (accessToken == null) {
            println("Error al obtener el token de acceso")
            Log.e("error_token", "Error al obtener el token de acceso}")
            return
        }

        val jsonPayload = JSONObject().apply {
            put("message", JSONObject().apply {
                put("token", token)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })

                put("data", JSONObject().apply {
                    put(idEnviado1, v1)
                    put("click_action", Vista)
                })
                put("android", JSONObject().apply {
                    put("notification", JSONObject().apply {
                        put("click_action", Vista)
                    })
                })

            })
        }
        Log.d("json", "obtenemos el $jsonPayload")

        val requestBody = jsonPayload.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(FCM_URL)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->

            Log.e("Response_codes", "${response.code}")
            Log.e("Response_bodys", "${response.body?.string()}")

        }
    }

    // Mapa para llevar control de resultados
    val resultados = mutableMapOf<String, Boolean>()

    fun enviarNotificacionFCM(
        id_user: String,
        token: String,
        clickAction: String,
        idAnuncio: String,
        idTienda: String,
        entrada: String,
        titulo: String,
        cuerpo: String,
        urlImagen: String? = null,
        fallo: (Boolean) -> Unit
    ) {
        val jsonBody = """
        {
          "token": "$token",
          "title": "$titulo",
          "body": "$cuerpo",
          "image": "${urlImagen ?: ""}",
          "click_action": "$clickAction",
          "idAnuncio": "$idAnuncio",
          "idTienda": "$idTienda",
          "entrada": "$entrada"
        }
    """.trimIndent()

        val client = OkHttpClient()
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(CLOUD_FUNCTION_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("noti_evadad", "Error al enviar notificación: ${e.message}")
                // Marcamos fallo en el mapa
                resultados[id_user] = false
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""
                Log.d("noti_evadad", "Respuesta: $bodyStr")

                // Detectamos errores por texto
                if (bodyStr.contains("not a valid FCM registration token", ignoreCase = true)) {
                    fallo(true)
                    resultados[id_user] = false
                } else if (bodyStr.contains("Notificación enviada", ignoreCase = true)) {
                    resultados[id_user] = true
                } else {
                    resultados[id_user] = false
                }

                Log.d("noti_evadad", "Estado de envío para $id_user: ${resultados[id_user]}")
            }
        })
    }

//    fun enviarNotificacionFCM_LINK(
//        id_user: String,
//        token: String,
//        titulo: String,
//        cuerpo: String,
//        link: String,
//        tipoNotificacion: String,
//        urlLogo: String? = null,
//        urlImagen: String? = null,
//        prioridad: String = "high",
//        resultado: (token: String, exito: Boolean) -> Unit
//    ) {
//
//        val jsonBody = """
//    {
//      "token": "$token",
//      "title": "$titulo",
//      "body": "$cuerpo",
//      "link": "$link",
//      "tipo_notificacion": "$tipoNotificacion",
//      "logo": "${urlLogo ?: ""}",
//      "image": "${urlImagen ?: ""}",
//      "prioridad": "$prioridad"
//    }
//    """.trimIndent()
//
//        val client = OkHttpClient()
//        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())
//
//        val request = Request.Builder()
//            .url(CLOUD_FUNCTION_URL)
//            .post(requestBody)
//            .addHeader("Content-Type", "application/json")
//            .build()
//
//        client.newCall(request).enqueue(object : Callback {
//
//            override fun onFailure(call: Call, e: IOException) {
//                Log.e("FCM_ENVIO", "Error con token $token: ${e.message}")
//                resultado(token, false)
//            }
//
//            override fun onResponse(call: Call, response: Response) {
//                val bodyStr = response.body?.string() ?: ""
//
//                val exito = bodyStr.contains("Notificación enviada", true)
//                val tokenInvalido =
//                    bodyStr.contains("not a valid FCM registration token", true)
//
//                when {
//                    exito -> {
//                        Log.d("FCM_ENVIO", "✅ Enviado a token: $token")
//                        resultado(token, true)
//                    }
//                    tokenInvalido -> {
//                        Log.d("FCM_ENVIO", "❌ Token inválido: $token")
//                        resultado(token, false)
//                    }
//                    else -> {
//                        Log.d("FCM_ENVIO", "⚠️ Error desconocido con token: $token")
//                        resultado(token, false)
//                    }
//                }
//            }
//        })
//    }

    suspend fun enviarNotificacionFCM_LINK_SUSPEND(
        id_user: String,
        token: String,
        titulo: String,
        cuerpo: String,
        link: String,
        tipoNotificacion: String,
        urlLogo: String? = null,
        urlImagen: String? = null,
        prioridad: String = "high"
    ): Boolean = suspendCancellableCoroutine { cont ->

        val jsonBody = """
    {
      "token": "$token",
      "title": "$titulo",
      "body": "$cuerpo",
      "link": "$link",
      "tipo_notificacion": "$tipoNotificacion",
      "logo": "${urlLogo ?: ""}",
      "image": "${urlImagen ?: ""}",
      "prioridad": "$prioridad"
    }
    """.trimIndent()

        val client = OkHttpClient()
        val requestBody = jsonBody.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(CLOUD_FUNCTION_URL)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                if (cont.isActive) cont.resume(false)
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyStr = response.body?.string() ?: ""

                val exito = bodyStr.contains("Notificación enviada", true)
                val tokenInvalido =
                    bodyStr.contains("not a valid FCM registration token", true)

                if (cont.isActive) {
                    cont.resume(exito && !tokenInvalido)
                }
            }
        })
    }




    fun eliminar_tokens_usuario(id_user: String, dispositivos: List<String>) {
        val db = FirebaseFirestore.getInstance()
            .collection("Trabajadores_Usuarios_Drivers")
            .document("users")
            .collection("tokens")
            .document(id_user)

        db.get().addOnSuccessListener { res ->
            val mapaTokens =
                (res.data?.get("tokens") as? Map<String, String>)?.toMutableMap() ?: mutableMapOf()

            var cambios = false
            dispositivos.forEach { disp ->
                if (mapaTokens.containsKey(disp)) {
                    mapaTokens.remove(disp)
                    cambios = true
                    Log.d("toksens_eliminar", "$id_user $disp eliminado")
                }
            }

            if (cambios) {
                db.update("tokens", mapaTokens)
                    .addOnSuccessListener {
                        Log.d(
                            "noti_evadad",
                            "Tokens eliminados correctamente"
                        )
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "noti_evadad",
                            "Error eliminando tokens: ${e.message}"
                        )
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("noti_evadad", "Error obteniendo tokens: ${e.message}")
        }
    }

    suspend fun getAccessToken(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.assets.open("service-account-file.json")
                val googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
                googleCredentials.refreshIfExpired()
                googleCredentials.accessToken.tokenValue
            } catch (e: Exception) {
                Log.e("error_token", "erro al obtener el token ${e.printStackTrace()}")

                null
            }
        }
    }
}