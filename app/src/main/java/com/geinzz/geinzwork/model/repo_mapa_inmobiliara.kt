package com.geinzz.geinzwork.model

import android.util.Log
import com.geinzz.geinzwork.BuildConfig.MAPBOX_ACCESS_TOKEN
import com.geinzz.geinzwork.data.model.localizate_geinz.Exitosa
import com.google.firebase.firestore.FirebaseFirestore
import com.mapbox.geojson.Point
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class repo_mapa_inmobiliara {
    private val db= FirebaseFirestore.getInstance()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
        .build()


    suspend fun obtenerRuta(
        originLat: Double, originLng: Double, destLat: Double, destLng: Double, profile: String
    ): Exitosa? {

        val url = "https://api.mapbox.com/directions/v5/mapbox/$profile/" +
                "$originLng,$originLat;$destLng,$destLat" +
                "?geometries=geojson&overview=full&steps=true&access_token=$MAPBOX_ACCESS_TOKEN"

        Log.d("DESVIO_DEBUG", "🌐 URL: $url")

        val request = Request.Builder().url(url).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = httpClient.newCall(request).execute()
                val body = response.body?.string()

                Log.d("DESVIO_DEBUG", "HTTP ${response.code} — body: ${body?.take(300)}")

                if (response.isSuccessful && body != null) {
                    val json = JSONObject(body)
                    val routes = json.getJSONArray("routes")

                    Log.d("DESVIO_DEBUG", "Rutas en respuesta: ${routes.length()}")

                    if (routes.length() > 0) {
                        val route = routes.getJSONObject(0)
                        val distanciaMetros = route.getDouble("distance")
                        val duracionSegundos = route.getDouble("duration")
                        val coordinates = route.getJSONObject("geometry").getJSONArray("coordinates")

                        val points = mutableListOf<Point>()
                        for (i in 0 until coordinates.length()) {
                            val coord = coordinates.getJSONArray(i)
                            points.add(Point.fromLngLat(coord.getDouble(0), coord.getDouble(1)))
                        }

                        // 👈 Exitosa en vez de Triple
                        return@withContext Exitosa(points, distanciaMetros, duracionSegundos)
                    } else {
                        Log.e("DESVIO_DEBUG", "❌ routes vacío")
                        null
                    }
                } else {
                    Log.e("DESVIO_DEBUG", "❌ HTTP error ${response.code}: ${body?.take(200)}")
                    null
                }
            } catch (e: Exception) {
                Log.e("DESVIO_DEBUG", "💥 Excepción: ${e.javaClass.simpleName} — ${e.message}")
                null
            }
        }
    }

}