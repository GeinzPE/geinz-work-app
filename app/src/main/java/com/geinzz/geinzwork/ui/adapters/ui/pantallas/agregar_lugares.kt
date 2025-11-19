package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults

import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.airbnb.lottie.model.content.CircleShape
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_localidad
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent


@Composable
fun datos_teindas() {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var lat_ by remember { mutableStateOf(0.0) }
    var lng_ by remember { mutableStateOf(0.0) }
    var direccion by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var numero_telefoono by remember { mutableStateOf("") }
    var mostar_geo by remember { mutableStateOf(false) }
    LaunchedEffect(latitud, longitud) {
        mostar_geo = latitud.isNotEmpty() && longitud.isNotEmpty()
    }
    LazyColumn(   modifier = Modifier
        .fillMaxSize()
        .imePadding() ) {
        item {
            _agregar_campos_txt(context)
            spacer_vertical(10.dp)
        }
        item {
            Row() {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = latitud,
                    onValueChange = { it ->
                        latitud = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    label = { texto_generico_one_line("Latitud") },
                    placeholder = { texto_generico_one_line("Latitud") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ), readOnly = true
                )
                spacer_horizonta(10.dp)
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = longitud,
                    onValueChange = { it ->

                        longitud = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    label = { texto_generico_one_line("longitud") },
                    placeholder = { texto_generico_one_line("longitud") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    ), readOnly = true
                )

            }
            spacer_vertical(10.dp)
            OutlinedTextField(
                value = direccion,
                onValueChange = { it ->
                    direccion = it
                },
                label = { texto_generico_one_line("direccion") },
                shape = RoundedCornerShape(20.dp),
                placeholder = { texto_generico_one_line("direccion") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
            )
            spacer_vertical(10.dp)
            OutlinedTextField(
                value = referencia,
                onValueChange = { it ->
                    referencia = it
                },
                label = { texto_generico_one_line("referencia") },
                shape = RoundedCornerShape(20.dp),
                placeholder = { texto_generico_one_line("referencia") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                ),
            )
            spacer_vertical(10.dp)
        }

        item {
            if (mostar_geo) {
                spacer_vertical(10.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(start = 10.dp)
                ) {
                    texto_generico_one_line("geohasing:")
                    texto_generico_one_line(
                        "${
                            constantes_lista_localidades.geohashing(
                                lat_,
                                lng_
                            )
                        }"
                    )
                }
                spacer_vertical(10.dp)

            }
        }

        item {
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    obtenerLatLogNoComposable(fusedLocationClient) { ubicacion ->
                        if (ubicacion != null) {
                            latitud = ubicacion.latitude.toString()
                            lat_ = ubicacion.latitude
                            lng_ = ubicacion.longitude
                            longitud = ubicacion.longitude.toString()
                        } else {
                            latitud = "No disponible"
                            longitud = "No disponible"
                        }
                    }
                } else {
                    Toast.makeText(context, "Activa el permiso de ubicación", Toast.LENGTH_SHORT)
                        .show()
                }
            }) {
                Text(text = "Obtener lat/log", color = Color.White)
            }
            spacer_vertical(20.dp)
        }

        item {
            Button(onClick = {
                var repo_agregar_datos = repo_agregar_datos(context)
//            val data = dataclass_repo_agregar_datos(
//                nombre_lugar = texto_nombre_lugar,
//                lat = latitud.toDouble(),
//                long = longitud.toDouble(),
//                numero_telefono = numero_telefoono.toInt()
//            )
//            repo_agregar_datos.agregar_datos(data)
//            longitud = ""
//            latitud = ""
//            numero_telefoono = ""
//            texto_nombre_lugar = ""
                repo_agregar_datos.pasar_datos()


            }) { texto_generico_one_line("enviar") }
        }

    }
}

@SuppressLint("MissingPermission")
fun obtenerLatLogNoComposable(
    fusedLocationClient: FusedLocationProviderClient,
    onUbicacionObtenida: (LatLng?) -> Unit
) {
    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        1000L
    ).build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            fusedLocationClient.removeLocationUpdates(this)
            val location = locationResult.lastLocation
            if (location != null) {
                onUbicacionObtenida(LatLng(location.latitude, location.longitude))
            } else {
                onUbicacionObtenida(null)
            }
        }
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        locationCallback,
        Looper.getMainLooper()
    )
}


@Composable
fun _agregar_campos_txt(context: Context) {
    val id_tienda by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var subcategoira_tienda by remember { mutableStateOf("") }
    var subcategoarias_selet by remember { mutableStateOf(listOf<String>()) }
    var modelo_negocio by remember { mutableStateOf(false) }
    var pagado by remember { mutableStateOf(false) }
    var categoria by remember { mutableStateOf("") }
    var lista_subcategoria by remember { mutableStateOf(listOf<String>()) }
    var lista_categorias by remember { mutableStateOf(listOf<String>()) }
    var lista_subcategorias_full by remember { mutableStateOf(listOf<List<String>>()) } // TODAS
    var texto_nombre_lugar by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    var txt_descipcion by remember { mutableStateOf("") }
    var repo_agregar_datos = repo_agregar_datos(context)
    var lista_modelo_negocio = listOf("Virtual", "Fisico")
    var lista_pagado = listOf("Premiun", "Free")
    var contadorClicks by remember { mutableStateOf(0) }
    scope.launch {
        var (d1, d2) = repo_agregar_datos.obtener_categorias()
        lista_categorias = d1
        lista_subcategorias_full = d2
    }

    var pedir_ayuda_ia by remember { mutableStateOf(false) }
    var mostar_progrs_var_IA by remember { mutableStateOf(false) }
    var obtner_lat_log_carga by remember { mutableStateOf(false) }


    if (pedir_ayuda_ia) {
        scope.launch {

            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-2.5-flash")

            try {
                // 1. Generar prompt
                val subcategoriaUnica = subcategoarias_selet.firstOrNull() ?: ""

                val prompt =
                    generarPrompt(texto_nombre_lugar, categoria, contadorClicks, subcategoriaUnica)

                val inicio = System.currentTimeMillis()

                // 2. Llamar al modelo
                val result = model.generateContent(prompt)
                val textoGenerado = result.text ?: ""   // evitar nulls

                val fin = System.currentTimeMillis()
                val tiempoMs = fin - inicio
                val tiempoSegundos = tiempoMs / 1000.0

                Log.d(
                    "Gemini",
                    "Tiempo de respuesta: $tiempoMs ms (${String.format("%.2f", tiempoSegundos)} s)"
                )
                Log.d("Gemini", "Resultado:\n$textoGenerado")

                // 3. Actualizar UI
                txt_descipcion = textoGenerado
                pedir_ayuda_ia = false
                mostar_progrs_var_IA = false

            } catch (e: Exception) {
                Log.e("Gemini", "Error al generar descripción: ${e.message}")
            }
        }
    }



    OutlinedTextField(
        value = texto_nombre_lugar,
        onValueChange = { it ->
            texto_nombre_lugar = it
        },
        label = { texto_generico_one_line("nombre") },
        shape = RoundedCornerShape(20.dp),
        placeholder = { texto_generico_one_line("nombre") },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
    )
    spacer_vertical(10.dp)
    ExpandDropDown(
        lista_categorias,
        false,
        "",
        "categoria"
    ) { seleccionado ->
        categoria = seleccionado
        val index = lista_categorias.indexOf(seleccionado)

        if (index != -1) {
            lista_subcategoria = lista_subcategorias_full[index]
        }
    }
    if (categoria.isNotEmpty()) {
        spacer_vertical(10.dp)
        chips_categorias(lista_subcategoria) { lista ->
            subcategoarias_selet = lista
        }
    }
    spacer_vertical(10.dp)
    OutlinedTextField(
        value = txt_descipcion,
        onValueChange = { txt_descipcion = it },
        label = { texto_generico_one_line("Descripción") },
        placeholder = { texto_generico_one_line("Escribe una descripción atractiva...") },
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),   // altura tipo textarea
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(20.dp),
        maxLines = 8,          // varias líneas
        singleLine = false     // textarea
    )
    spacer_vertical(10.dp)
    if (texto_nombre_lugar.length > 3 && categoria.isNotEmpty()) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    contadorClicks++
                    pedir_ayuda_ia = true
                    mostar_progrs_var_IA = true
                }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                texto_generico_one_line(
                    if (mostar_progrs_var_IA) "Generando..." else "Generar con IA",
                    style = MaterialTheme.typography.bodyMedium
                )

                if (mostar_progrs_var_IA) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp)           // más pequeño
                        ,
                        strokeWidth = 2.dp,
                        trackColor = Color.White// delgado y elegante
                    )
                }
            }

        }
    }
    spacer_vertical(10.dp)
    ExpandDropDown(
        lista_localidad,
        false,
        "",
        "localidad",
    ) { sub ->
        localidad = sub
    }
    spacer_vertical(10.dp)
    ExpandDropDown(
        lista_modelo_negocio,
        false,
        "",
        "modelo de negocio",
    ) { modelo ->
        if (modelo.equals("Fisico")) {
            modelo_negocio = true
        } else {
            modelo_negocio = false
        }
    }
    spacer_vertical(10.dp)
    ExpandDropDown(
        lista_pagado,
        false,
        "",
        "Pagado",
    ) { modelo ->
        if (modelo.equals("Premiun")) {
            pagado = true
        } else {
            pagado = false
        }
    }
}


@Composable
fun chips_categorias(
    lista: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by remember { mutableStateOf(listOf<String>()) }

    spacer_vertical(10.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->

            val isSelected = seleccionados.contains(item)

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            Color.White // Blanco
                        else
                            MaterialTheme.colorScheme.primary      // Primario
                    )
                    .clickable {
                        seleccionados =
                            if (isSelected)
                                seleccionados - item
                            else
                                seleccionados + item

                        lista_select(seleccionados)
                    }
            ) {
                texto_generico_one_line(
                    item,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected)
                        Color.Black
                    else
                        Color.White
                )
            }
        }
    }

    spacer_vertical(10.dp)
}


fun generarPrompt(
    nombre: String,
    categoria: String,
    intentos: Int,
    subcategoria: String
): String {

    val extraSub = if (subcategoria.isNotBlank()) {
        """ especializada en "$subcategoria" """
    } else ""

    return when (intentos) {

        // Primer intento — cálido, elegante, breve
        1 -> """
            Genera una descripción hermosa elegante cálida breve e inspiradora para el perfil de una tienda llamada "$nombre" dedicada a "$categoria"$extraSub Puedes usar emojis inspiradores como ✨🌟💼⚡📦🏆🌱💡 sin usar emojis románticos como corazones y sin colocar puntos ni saltos de línea El texto debe ser claro motivador y no debe superar seis líneas solo entrega la descripción final sin explicaciones
        """.trimIndent()

        // Segundo intento — creativo, moderno, inspirador
        2 -> """
            Genera una descripción creativa fluida moderna atractiva e inspiradora para la tienda "$nombre" dedicada a "$categoria"$extraSub Puedes incluir emojis motivadores como ✨🌟💡⚡📦 sin corazones y sin usar puntos ni saltos de línea La descripción debe sentirse emocional y no superar seis líneas solo entrega el texto final sin instrucciones adicionales
        """.trimIndent()

        // Tercer intento — emocional, profesional, memorable
        3 -> """
            Genera una descripción emocional profunda cálida memorable e inspiradora para la tienda "$nombre" dedicada a "$categoria"$extraSub Puedes añadir emojis inspiradores como ✨🌟💼💡 sin corazones y sin usar puntos ni saltos de línea Usa un tono cercano motivador y no excedas seis líneas solo entrega el texto final
        """.trimIndent()

        // Cuarto intento+ — poético, artístico, motivador
        else -> """
            Genera una descripción diferente artística poética motivadora y única para la tienda "$nombre" enfocada en "$categoria"$extraSub Puedes incluir emojis inspiradores como ✨🌟⚡💡🏆 sin usar corazones y sin usar puntos ni saltos de línea El texto debe transmitir autenticidad encanto y no superar seis líneas solo entrega la descripción final
        """.trimIndent()
    }
}


