package com.geinzz.geinzwork.herramientas_geinz.constantes

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.geinzz.geinzwork.R
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.util.UUID

object constantes_subir_img_panel_tienda {
    fun esUriLocal(valor: String?): Boolean =
        valor?.startsWith("content://") == true

    fun esUrlRemota(valor: String?): Boolean =
        valor?.startsWith("http") == true

    @Composable
    fun BoxImagen(
        valor: String?,
        estaEliminada: Boolean,
        onClick: () -> Unit,
        onCancelarOEliminar: () -> Unit,
        onExpandir: (() -> Unit)? = null // 👈 callback opcional
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onClick() }
        ) {

            // 🖼️ IMAGEN / PLACEHOLDER
            when {
                estaEliminada || valor == null -> {
                    PlaceholderInterno()
                }

                else -> {
                    SubcomposeAsyncImage(
                        model = valor,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = { PlaceholderInterno() },
                        error = { PlaceholderInterno() }
                    )
                }
            }

            // ❌ / 🔁 CANCELAR / RESTAURAR
            if (valor != null || estaEliminada) {
                Icon(
                    imageVector = if (estaEliminada) Icons.Default.Undo else Icons.Default.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .clickable { onCancelarOEliminar() }
                        .padding(4.dp)
                )
            }


            // 🔍 EXPANDIR (solo si tiene URI o URL y NO está eliminada)
            if (!estaEliminada && valor != null && (esUriLocal(valor) || esUrlRemota(valor))) {
                Icon(
                    imageVector = Icons.Default.OpenInFull, // o ZoomOutMap
                    contentDescription = "Expandir imagen",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .size(22.dp)
                        .background(
                            Color.Black.copy(alpha = 0.6f),
                            CircleShape
                        )
                        .clickable {
                            onExpandir?.invoke()
                        }
                        .padding(4.dp)
                )
            }
        }
    }

    @Composable
    fun PlaceholderInterno() {
        Image(
            painter = painterResource(R.drawable.cargando_img_categorias),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun guardarCambiosImagenes(
        tipo: String,
        context: Context,
        imagenes: List<String?>,
        eliminadas: List<Int>,
        imagenesOriginales: List<String?>,
        idTienda: String,
        localidad: String,
        onFinish: (List<String>) -> Unit
    ) {


        val TAG = "GUARDAR_IMAGENES"
        val storage = FirebaseStorage.getInstance().reference
        val firestore = FirebaseFirestore.getInstance()

        val nuevasUrls = imagenesOriginales.toMutableList()
        val idImagenes = mutableMapOf<Int, String>() // mapa índice -> idImagen para promociones

        // 🔢 total de operaciones (subidas + eliminaciones)
        val totalOperaciones =
            imagenes.count { esUriLocal(it) } + eliminadas.size

        if (totalOperaciones == 0) {
            onFinish(nuevasUrls.filterNotNull())
            return
        }

        var operacionesCompletadas = 0


        fun checkFinish() {
            operacionesCompletadas++

            if (operacionesCompletadas == totalOperaciones) {

                val urlsFinales = nuevasUrls.filterNotNull()

                // 🔥 actualizar Firestore UNA SOLA VEZ

                firestore
                    .collection("Tiendas")
                    .document(localidad)
                    .collection(localidad)
                    .document(idTienda)
                    .update("img_tienda.lista_img.$tipo", urlsFinales)
                    .addOnSuccessListener {
                        Log.d(TAG, "🔥 Firestore actualizado ($tipo)")
                        onFinish(urlsFinales)
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ Error Firestore", it)
                        onFinish(urlsFinales) // devolvemos igual
                    }

            }
        }

        // ===============================
        // 1️⃣ SUBIR NUEVAS IMÁGENES
        // ===============================
        imagenes.forEachIndexed { index, valor ->
            if (esUriLocal(valor)) {

                val uri = Uri.parse(valor)
                val bytes = procesarImagenWebPSinRecorte(context, uri)
                val ref = storage.child("tiendas/$idTienda/imagenes/$tipo/slot_$index.webp")
                ref.putBytes(bytes)
                    .continueWithTask { ref.downloadUrl }
                    .addOnSuccessListener { downloadUrl ->
                        nuevasUrls[index] = downloadUrl.toString()
                        checkFinish()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ Error subiendo slot $index", it)
                        checkFinish()
                    }
            }
        }

        // ===============================
        // 2️⃣ ELIMINAR IMÁGENES
        // ===============================
        eliminadas.forEach { index ->
            val ref = storage.child("tiendas/$idTienda/imagenes/$tipo/slot_$index.webp")
            ref.delete()
                .addOnSuccessListener {
                    nuevasUrls[index] = null
                    checkFinish()
                }
                .addOnFailureListener {
                    Log.e(TAG, "❌ Error eliminando slot $index", it)
                    nuevasUrls[index] = null
                    checkFinish()
                }
        }
    }



    @RequiresApi(Build.VERSION_CODES.R)
    fun guardandoCambiosPromociones(
        tipo: String,
        context: Context,
        imagenes: Map<String, String?>, // Map<ID, URL o URI local>
        eliminadas: List<String>,       // IDs eliminadas
        idTienda: String,
        localidad: String,
        onFinish: (Map<String, String>, tipo: String) -> Unit
    ) {

        val TAG = "GUARDAR_IMAGENES"
        val storage = FirebaseStorage.getInstance().reference
        val firestore = FirebaseFirestore.getInstance()

        val nuevasUrls = imagenes.toMutableMap() // mantiene todos los IDs existentes

        val totalOperaciones = imagenes.count { esUriLocal(it.value) } + eliminadas.size
        if (totalOperaciones == 0) {
            onFinish(nuevasUrls.filterValues { it != null }.mapValues { it.value!! }, tipo)
            return
        }

        var operacionesCompletadas = 0
        fun checkFinish() {
            operacionesCompletadas++
            if (operacionesCompletadas == totalOperaciones) {
                val urlsFinales = nuevasUrls.filterValues { it != null }.mapValues { it.value!! }

                firestore.collection("Tiendas")
                    .document(localidad)
                    .collection(localidad)
                    .document(idTienda)
                    .set(
                        mapOf("img_tienda" to mapOf("lista_img" to mapOf(tipo to urlsFinales))),
                        SetOptions.merge()
                    )
                    .addOnSuccessListener { onFinish(urlsFinales, tipo) }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ Error Firestore", it)
                        onFinish(urlsFinales, tipo)
                    }
            }
        }

        // ===============================
        // 1️⃣ SUBIR SOLO IMÁGENES CAMBIADAS CON ID DINÁMICO
        // ===============================
        imagenes.forEach { (id, valor) ->
            if (esUriLocal(valor)) { // solo sube si es URI local
                val uri = Uri.parse(valor)
                val bytes = procesarImagenWebPSinRecorte(context, uri)

                val idImagen = if (id.isEmpty()) generarIdImagen() else id // 🔹 ID único dinámico
                val ref = storage.child("tiendas/$idTienda/imagenes/$tipo/$idImagen.webp")

                ref.putBytes(bytes)
                    .continueWithTask { ref.downloadUrl }
                    .addOnSuccessListener { downloadUrl ->
                        nuevasUrls.remove(id) // eliminamos ID temporal si existía
                        nuevasUrls[idImagen] = downloadUrl.toString() // reemplaza o agrega
                        checkFinish()
                    }
                    .addOnFailureListener {
                        Log.e(TAG, "❌ Error subiendo $idImagen", it)
                        checkFinish()
                    }
            }
        }

        // ===============================
        // 2️⃣ ELIMINAR IMÁGENES
        // ===============================
        eliminadas.forEach { id ->
            val ref = storage.child("tiendas/$idTienda/imagenes/$tipo/$id.webp")
            ref.delete()
                .addOnSuccessListener {
                    nuevasUrls.remove(id)
                    checkFinish()

                    // 🔹 Eliminamos solo el ID del map en Firestore
                    val docRef = firestore.collection("Tiendas")
                        .document(localidad)
                        .collection(localidad)
                        .document(idTienda)
                    val path = "img_tienda.lista_img.$tipo.$id"
                    docRef.update(path, FieldValue.delete())
                }
                .addOnFailureListener {
                    Log.e(TAG, "❌ Error eliminando $id", it)
                    nuevasUrls.remove(id)
                    checkFinish()
                }
        }
    }




    fun guardarImagenesEnFirestore(
        localidad: String,
        idTienda: String,
        tipo: String,
        urls: List<String>,
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        FirebaseFirestore.getInstance()
            .collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(idTienda)
            .update("img_tienda.lista_img.$tipo", urls)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }


    fun guardarImagenesEnFirestore_promociones(
        localidad: String,
        idTienda: String,
        tipo: String,
        fotos: Map<String, String>, // ID -> URL
        onSuccess: () -> Unit = {},
        onError: (Exception) -> Unit = {}
    ) {
        val TAG = "FirestorePromociones"

        if (tipo.isEmpty()) return // evita path inválido

        Log.d(TAG, "🚀 Guardando imágenes en Firestore")
        Log.d(TAG, "Localidad: $localidad, Tienda: $idTienda, Tipo: $tipo")
        Log.d(TAG, "Fotos a guardar: $fotos")

        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("Tiendas")
            .document(localidad)
            .collection(localidad)
            .document(idTienda)
            .set(
                mapOf("img_tienda" to mapOf("lista_img" to mapOf(tipo to fotos))),
                SetOptions.merge()
            )
            .addOnSuccessListener {
                Log.d(TAG, "✅ Firestore actualizado correctamente")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error guardando en Firestore", e)
                onError(e)
            }
    }




    @RequiresApi(Build.VERSION_CODES.R)
    fun subir_storage_perfil_img(
        context: Context,
        idTienda: String,
        valor: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {

        // 👉 Si ya es URL remota, no subir
        if (esUrlRemota(valor)) {
            onSuccess(valor)
            return
        }

        val uri = Uri.parse(valor)

        val storageRef = FirebaseStorage.getInstance()
            .reference
            .child("tiendas/$idTienda/logo/logo.webp")

        val bytes = procesarImagenWebPSinRecorte(context, uri)

        storageRef.putBytes(bytes)
            .continueWithTask { storageRef.downloadUrl }
            .addOnSuccessListener { downloadUrl ->
                onSuccess(downloadUrl.toString())
            }
            .addOnFailureListener {
                onError(it)
            }
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun procesarImagenWebPSinRecorte(
        context: Context,
        uri: Uri,
        maxSize: Int = 1280, // 🔥 máximo lado
        quality: Int = 80    // 🔥 alta calidad
    ): ByteArray {

        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        val width = bitmapOriginal.width
        val height = bitmapOriginal.height

        // 1️⃣ Escalar SOLO si es muy grande
        val scale = if (width > height) {
            if (width > maxSize) maxSize.toFloat() / width else 1f
        } else {
            if (height > maxSize) maxSize.toFloat() / height else 1f
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val bitmapFinal = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmapOriginal, newWidth, newHeight, true)
        } else {
            bitmapOriginal
        }

        // 2️⃣ Comprimir WebP
        val output = ByteArrayOutputStream()
        bitmapFinal.compress(
            Bitmap.CompressFormat.WEBP_LOSSY,
            quality,
            output
        )

        return output.toByteArray()
    }


    @RequiresApi(Build.VERSION_CODES.R)
    fun procesarImagenParaWhatsappDB(
        context: Context,
        uri: Uri,
        maxSize: Int = 1280,
        calidadJpg: Int = 80
    ): ByteArray {

        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("No se pudo abrir el stream")

        val bitmapOriginal = BitmapFactory.decodeStream(inputStream)
            ?: throw IllegalArgumentException("No se pudo leer la imagen")

        inputStream.close()

        val width = bitmapOriginal.width
        val height = bitmapOriginal.height

        // 🔥 1. Escalado inteligente
        val scale = if (width > height) {
            if (width > maxSize) maxSize.toFloat() / width else 1f
        } else {
            if (height > maxSize) maxSize.toFloat() / height else 1f
        }

        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()

        val bitmapFinal = if (scale < 1f) {
            Bitmap.createScaledBitmap(bitmapOriginal, newWidth, newHeight, true)
        } else {
            bitmapOriginal
        }

        // 🔥 2. Detectar si es tipo captura (texto/UI)
        val esCaptura = esProbableCaptura(bitmapFinal)

        val output = ByteArrayOutputStream()

        if (esCaptura) {
            // 🖼️ PNG para texto nítido
            bitmapFinal.compress(
                Bitmap.CompressFormat.PNG,
                100,
                output
            )
        } else {
            // 📸 JPG para fotos livianas
            bitmapFinal.compress(
                Bitmap.CompressFormat.JPEG,
                calidadJpg,
                output
            )
        }

        // 🔥 3. Liberar memoria
        if (bitmapFinal != bitmapOriginal) {
            bitmapOriginal.recycle()
        }
        bitmapFinal.recycle()

        return output.toByteArray()
    }
    fun esProbableCaptura(bitmap: Bitmap): Boolean {

        val width = bitmap.width
        val height = bitmap.height

        // 🔥 Si es muy rectangular tipo pantalla
        val ratio = width.toFloat() / height.toFloat()

        val esFormatoPantalla = ratio in 0.4f..0.6f || ratio in 1.6f..2.2f

        // 🔥 Si tiene pocos colores (UI suele tener menos variación)
        val sampleSize = 50
        val colores = HashSet<Int>()

        for (x in 0 until width step width / sampleSize) {
            for (y in 0 until height step height / sampleSize) {
                colores.add(bitmap.getPixel(x, y))
                if (colores.size > 1000) return false // mucha variación → foto
            }
        }

        return esFormatoPantalla || colores.size < 500
    }

    suspend fun subir_foto_perfil_algolia_normal(
        id_tienda: String,
        urlFinal: String
    ): Boolean {
        return try {
            val db = FirebaseFirestore.getInstance()

            val updateTienda = db
                .collection("Tiendas")
                .document("barranca")
                .collection("barranca")
                .document(id_tienda)
                .update("img_tienda.logo_tienda", urlFinal)

            val updateLugar = db
                .collection("lugares")
                .document(id_tienda)
                .update("img", urlFinal)

            // Espera a que ambas terminen
            Tasks.whenAll(updateTienda, updateLugar).await()

            true
        } catch (e: Exception) {
            false
        }
    }

    fun generarIdImagen(): String {
        val min = 1_000_000    // el número más pequeño de 7 dígitos
        val max = 9_999_999    // el número más grande de 7 dígitos
        return (min..max).random().toString()
    }


    fun generarIdImagen_nueve(): String {
        val min = 100_000_000    // número más pequeño de 9 dígitos
        val max = 999_999_999    // el número más grande de 7 dígitos
        return (min..max).random().toString()
    }

    fun generarIdImagen_cinco(): String {
        val min = 10_000   // número más pequeño de 5 dígitos
        val max = 99_999   // número más grande de 5 dígitos
        return (min..max).random().toString()
    }





}