package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.Chip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch

import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.model.content.CircleShape
import com.geinzz.geinzwork.data.model.data_class_tienda_geinz
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.data.model.img_tienda
import com.geinzz.geinzwork.data.model.ingreso_date
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioDia_bloques
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_numero
import com.geinzz.geinzwork.data.model.localizate_geinz.contacto_red
import com.geinzz.geinzwork.data.model.localizate_geinz.inicio_geinz.ref_ubi
import com.geinzz.geinzwork.data.model.localizate_geinz.metodo_contacto_tienda
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_metodo_individual
import com.geinzz.geinzwork.data.model.localizate_geinz.modelo_pagos_tienda
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.ExpandDropDown
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.fechaActual
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_horas.fechaUnaSemanaDespues
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.lista_localidad
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import java.nio.file.WatchEvent
import java.security.SecureRandom


@SuppressLint("CoroutineCreationDuringComposition")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun datos_teindas() {
    val lista_metood_pago = listOf("Yape", "Plin", "Efectivo", "Agora", "visa/Mastercard")
    val lista_medood_contacto =
        listOf("whatsapp", "telefono", "tiktok", "facebook", "instagram", "sitio web")
    val lista_modelo_negocio = listOf("Fisico", "virtual")
    val lista_pagado = listOf("Premiun", "Free")

    val viewmodel_agregar_datos: viewmodel_agregar_datos = viewModel()
    val context = LocalContext.current
    val horario_atencion = viewmodel_agregar_datos.obtenerHorarioAtencion()
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    var repo_agregar_datos = repo_agregar_datos(context)
    var lat_ by rememberSaveable { mutableStateOf(0.0) }
    var lng_ by rememberSaveable { mutableStateOf(0.0) }
    var contadorClicks by rememberSaveable { mutableStateOf(0) }
    var mostar_geo by rememberSaveable { mutableStateOf(false) }
    var yape_select by rememberSaveable { mutableStateOf(false) }
    var plin_select by rememberSaveable { mutableStateOf(false) }
    var Efectivo2 by rememberSaveable { mutableStateOf(false) }
    var Agora2 by rememberSaveable { mutableStateOf(false) }
    var visa2 by rememberSaveable { mutableStateOf(false) }
    var tk2 by rememberSaveable { mutableStateOf(false) }
    var fb2 by rememberSaveable { mutableStateOf(false) }
    var ig2 by rememberSaveable { mutableStateOf(false) }
    var ws2 by rememberSaveable { mutableStateOf(false) }
    var tlf2 by rememberSaveable { mutableStateOf(false) }
    var stw2 by rememberSaveable { mutableStateOf(false) }
    var modelo_negocio by rememberSaveable { mutableStateOf(false) }
    var pagado by rememberSaveable { mutableStateOf(false) }
    var pedir_ayuda_ia by rememberSaveable { mutableStateOf(false) }
    var mostar_progrs_var_IA by remember { mutableStateOf(false) }

    var latitud by rememberSaveable { mutableStateOf("") }
    var longitud by rememberSaveable { mutableStateOf("") }
    var direccion by rememberSaveable { mutableStateOf("") }
    var referencia by rememberSaveable { mutableStateOf("") }
    var numero_yape by rememberSaveable { mutableStateOf("") }
    var titular_yape by rememberSaveable { mutableStateOf("") }
    var numero_plin by rememberSaveable { mutableStateOf("") }
    var titular_plin by rememberSaveable { mutableStateOf("") }
    var user_tk by rememberSaveable { mutableStateOf("") }
    var user_fb by rememberSaveable { mutableStateOf("") }
    var user_ig by rememberSaveable { mutableStateOf("") }
    var numero_whatsap by rememberSaveable { mutableStateOf("") }
    var numero_telefono by rememberSaveable { mutableStateOf("") }
    var sitio_web by rememberSaveable { mutableStateOf("") }
    var localidad by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf("") }
    var texto_nombre_lugar by rememberSaveable { mutableStateOf("") }
    var txt_descipcion by rememberSaveable { mutableStateOf("") }
    var valor_geohashin by rememberSaveable { (mutableStateOf("")) }

    var subcategoarias_selet by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_subcategoria by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_categorias by rememberSaveable { mutableStateOf(listOf<String>()) }
    var lista_subcategorias_full by rememberSaveable { mutableStateOf(listOf<List<String>>()) }



    scope.launch {
        val (d1, d2) = repo_agregar_datos.obtener_categorias()
        lista_categorias = d1
        lista_subcategorias_full = d2
    }
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

    LaunchedEffect(latitud, longitud) {
        mostar_geo = latitud.isNotEmpty() && longitud.isNotEmpty()
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {

        item {
            OutlinedTextField(
                value = texto_nombre_lugar,
                onValueChange = { it ->
                    texto_nombre_lugar = it
                },
                label = { texto_generico_one_line("nombre") },
                shape = RoundedCornerShape(20.dp),
                placeholder = {
                    texto_generico_one_line(
                        "nombre",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
                ),
            )
            spacer_vertical(10.dp)
        }

        item {
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
            spacer_vertical(10.dp)
        }

        item {
            if (categoria.isNotEmpty()) {
                chips_categorias(lista_subcategoria) { lista ->
                    subcategoarias_selet = lista
                }
                spacer_vertical(10.dp)
            }

        }

        item {
            OutlinedTextField(
                value = txt_descipcion,
                onValueChange = { txt_descipcion = it },
                label = { texto_generico_one_line("Descripción") },
                placeholder = {
                    texto_generico_one_line(
                        "Escribe una descripción atractiva...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),   // altura tipo textarea
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
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
        }

        item {
            ExpandDropDown(
                lista_localidad,
                false,
                "",
                "localidad",
            ) { sub ->
                localidad = sub
            }
            spacer_vertical(10.dp)
        }

        item {

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
        }

        item {
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
            spacer_vertical(10.dp)
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = latitud,
                    onValueChange = { it ->
                        latitud = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    label = { texto_generico_one_line("Latitud") },
                    placeholder = {
                        texto_generico_one_line(
                            "Latitud",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    ), readOnly = true
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = longitud,
                    onValueChange = { it ->

                        longitud = it
                    },
                    shape = RoundedCornerShape(20.dp),
                    label = { texto_generico_one_line("longitud") },
                    placeholder = {
                        texto_generico_one_line(
                            "longitud",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    ), readOnly = true
                )
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
                        Toast.makeText(
                            context,
                            "Activa el permiso de ubicación",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }) {
                    Text(text = "Obtener lat/log", color = Color.White)
                }
            }
            spacer_vertical(10.dp)
            OutlinedTextField(
                value = direccion,
                onValueChange = { it ->
                    direccion = it
                },
                label = { texto_generico_one_line("direccion") },
                shape = RoundedCornerShape(20.dp),
                placeholder = {
                    texto_generico_one_line(
                        "direccion",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
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
                placeholder = {
                    texto_generico_one_line(
                        "referencia",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,
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
                    valor_geohashin = constantes_lista_localidades.geohashing(
                        lat_,
                        lng_
                    )
                    texto_generico_one_line(
                        valor_geohashin
                    )

                }
                spacer_vertical(10.dp)

            }
        }

        item {
            spacer_vertical(10.dp)
            texto_generico_one_line(
                "Metodos de pago",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 10.dp)
            )
            spacer_vertical(5.dp)
            ChipsCategoriasCheck(lista_metood_pago) { seleccionados ->
                val yapeSelected = "Yape" in seleccionados
                val plinSelected = "Plin" in seleccionados
                val Efectivo = "Efectivo" in seleccionados
                val Agora = "Agora" in seleccionados
                val visa = "visa/Mastercard" in seleccionados

                if (yapeSelected) {
                    yape_select = true
                } else {
                    yape_select = false
                }

                if (plinSelected) {
                    plin_select = true
                } else {
                    plin_select = false
                }

                if (Efectivo) {
                    Efectivo2 = true
                } else {
                    Efectivo2 = false
                }
                if (Agora) {
                    Agora2 = true
                } else {
                    Agora2 = false
                }
                if (visa) {
                    visa2 = true
                } else {
                    visa2 = false
                }
            }
            if (yape_select) {
                valor_txt_contacto("yape", numero_yape) { numero_yape = it }
                spacer_vertical(5.dp)
                OutlinedTextField(
                    value = titular_yape,
                    onValueChange = { it ->
                        titular_yape = it
                    },
                    label = { texto_generico_one_line("titular de yape") },
                    shape = RoundedCornerShape(20.dp),
                    placeholder = {
                        texto_generico_one_line(
                            "titular de yape",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    ),
                )
                spacer_vertical(20.dp)
            }
            if (plin_select) {
                valor_txt_contacto("plin", numero_plin) { numero_plin = it }
                spacer_vertical(5.dp)
                OutlinedTextField(
                    value = titular_plin,
                    onValueChange = { it ->
                        titular_plin = it
                    },
                    label = { texto_generico_one_line("titular de plin") },
                    shape = RoundedCornerShape(20.dp),
                    placeholder = {
                        texto_generico_one_line(
                            "titular de plin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    ),
                )
                spacer_vertical(20.dp)
            }

        }

        item {
            spacer_vertical(10.dp)
            texto_generico_one_line(
                "Metodos de contacto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 10.dp)
            )
            ChipsCategoriasCheck(lista_medood_contacto) { seleccionados ->
                val tk = "tiktok" in seleccionados
                val fb = "facebook" in seleccionados
                val ig = "instagram" in seleccionados
                val ws = "whatsapp" in seleccionados
                val tlf = "telefono" in seleccionados
                val stw = "sitio web" in seleccionados
                if (tk) {
                    tk2 = true
                } else {
                    tk2 = false
                }
                if (fb) {
                    fb2 = true
                } else {
                    fb2 = false
                }
                if (ig) {
                    ig2 = true
                } else {
                    ig2 = false
                }
                if (ws) {
                    ws2 = true
                } else {
                    ws2 = false
                }
                if (tlf) {
                    tlf2 = true
                } else {
                    tlf2 = false
                }
                if (stw) {
                    stw2 = true
                } else {
                    stw2 = false
                }
            }
            if (tk2) {
                valor_txt_contacto("tiktok", user_tk) { user_tk = it }
            }
            spacer_vertical(5.dp)

            if (fb2) {
                valor_txt_contacto("facebook", user_fb) { user_fb = it }
            }
            spacer_vertical(5.dp)

            if (ig2) {
                valor_txt_contacto("instagram", user_ig) { user_ig = it }
            }
            spacer_vertical(5.dp)

            if (ws2) {
                valor_txt_contacto("whatsapp", numero_whatsap) { numero_whatsap = it }
            }
            spacer_vertical(5.dp)

            if (tlf2) {
                valor_txt_contacto("telefono", numero_telefono) { numero_telefono = it }
            }
            spacer_vertical(5.dp)
            if (stw2) {
                valor_txt_contacto("sitio web", sitio_web) { sitio_web = it }
            }
        }

        item {
            spacer_vertical(10.dp)
            texto_generico_one_line(
                "Horario",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 10.dp)
            )

            HorarioSemanal(viewmodel_agregar_datos)

        }

        item {
            spacer_vertical(5.dp)
            Button(onClick = {
                val repo_agregar_datos = repo_agregar_datos(context)
                val datos_enviar = data_class_tienda_geinz(
                    categoria_tienda = categoria,
                    descripcion = txt_descipcion,
                    geogash = valor_geohashin,
                    id_tienda = generarIdSeguro(),
                    localida_tienda = localidad.lowercase(),
                    modelo_negocio = modelo_negocio,
                    nombre_tienda = texto_nombre_lugar,
                    pagado = pagado,
                    subcategoria = subcategoarias_selet,
                    ubicacion = ref_ubi(
                        latitud = lat_,
                        longitud = lng_,
                        referencia = referencia,
                        direccion = direccion,
                    ),
                    metodo_pago = modelo_pagos_tienda(
                        visa_mastercard = modelo_metodo_individual(
                            numero = "",
                            qr = "",
                            nombre = "",
                            enable = visa2,
                        ),
                        agora = modelo_metodo_individual(
                            numero = "",
                            qr = "",
                            nombre = "",
                            enable = Agora2,
                        ),
                        efectivo = modelo_metodo_individual(
                            numero = "",
                            qr = "",
                            nombre = "",
                            enable = Efectivo2,
                        ),
                        plin = modelo_metodo_individual(
                            numero = numero_plin,
                            qr = "",
                            nombre = titular_plin,
                            enable = yape_select,
                        ),
                        yape = modelo_metodo_individual(
                            numero = numero_yape,
                            qr = "",
                            nombre = titular_yape,
                            enable = plin_select,
                        ),
                    ),
                    metodo_contacto = metodo_contacto_tienda(
                        whatsapp = contacto_numero(
                            estado = ws2,
                            numero = numero_whatsap
                        ),
                        llamada = contacto_numero(
                            estado = tlf2,
                            numero = numero_telefono
                        ),
                        facebook = contacto_red(
                            estado = fb2,
                            nombre = user_fb,
                            url = ""
                        ),
                        instagram = contacto_red(
                            estado = ig2,
                            nombre = user_ig,
                            url = ""
                        ),
                        tiktok = contacto_red(
                            estado = tk2,
                            nombre = user_tk,
                            url = ""
                        ),
                        sitio_web = contacto_red(
                            estado = stw2,
                            nombre = sitio_web,
                            url = ""
                        ),
                    ),
                    fechas = ingreso_date(
                        hora_ingreso = constantes_horas.horaActual(),
                        fecha_ingreso = fechaActual(),
                        fecha_fin = fechaUnaSemanaDespues()
                    ),
                    timeSlamp = timeStampNumero(),
                    horario_atencion = HorarioAtencion_box(
                        lunes = horario_atencion.lunes,
                        martes = horario_atencion.martes,
                        miercoles = horario_atencion.miercoles,
                        jueves = horario_atencion.jueves,
                        viernes = horario_atencion.viernes,
                        sabado = horario_atencion.sabado,
                        domingo = horario_atencion.domingo,
                    ), lista_img = img_tienda()
                )
                repo_agregar_datos.agraegar_datos_db_2(datos_enviar)
                val gson = GsonBuilder().setPrettyPrinting().create()
                Log.d("datos_enviamor", gson.toJson(datos_enviar))


            }) { texto_generico_one_line("enviar") }
        }

    }
}

@Composable
fun HorarioSemanal(viewmodel_agregar_datos: viewmodel_agregar_datos) {

    val context = LocalContext.current

    val dias = listOf(
        "Lunes", "Martes", "Miércoles",
        "Jueves", "Viernes", "Sábado", "Domingo"
    )

    val mapaHoras = viewmodel_agregar_datos.mapaHoras

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        dias.forEach { dia ->

            val item = mapaHoras[dia]!!

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp)
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            texto_generico_one_line(dia)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(if (item.cerrado.value) "Abierto" else "Cerrado")

                            spacer_horizonta(5.dp)

                            Switch(
                                checked = item.cerrado.value,
                                onCheckedChange = { value ->
                                    item.cerrado.value = value
                                    if (!value) { // si el día se marca como cerrado
                                        item.h1AM.value = ""
                                        item.h2AM.value = ""
                                        item.h1PM.value = ""
                                        item.h2PM.value = ""
                                        item.solo_horario.value = false
                                    }
                                }
                            )
                        }
                    }

                    // Mostrar horarios solo si NO está cerrado
                    AnimatedVisibility(item.cerrado.value) {

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                            Row(verticalAlignment = Alignment.CenterVertically) {

                                Checkbox(
                                    checked = item.solo_horario.value,
                                    onCheckedChange = { nuevo ->
                                        item.solo_horario.value = nuevo
                                    }
                                )

                                Text(
                                    text = if (item.solo_horario.value)
                                        "Trabajo de corrido"
                                    else
                                        "Trabajo con descanso"
                                )
                            }

                            // MAÑANA ---------------------
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                campoHora(
                                    valor = item.h1AM.value,
                                    etiqueta = "Apertura AM",
                                    onHoraSeleccionada = { new ->
                                        item.h1AM.value = new
                                    },
                                    abrirTimePicker = { valorActual, onSelect ->
                                        abrirTimePicker(context, valorActual, onSelect)
                                    }
                                )

                                texto_generico_one_line(" a ")

                                campoHora(
                                    valor = item.h2AM.value,
                                    etiqueta = "Cierre AM",
                                    onHoraSeleccionada = { new ->
                                        item.h2AM.value = new
                                    },
                                    abrirTimePicker = { valorActual, onSelect ->
                                        abrirTimePicker(context, valorActual, onSelect)
                                    }
                                )
                            }

                            // TARDE ----------------------
                            AnimatedVisibility(!item.solo_horario.value) {

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    campoHora(
                                        valor = item.h1PM.value,
                                        etiqueta = "Apertura PM",
                                        onHoraSeleccionada = { new ->
                                            item.h1PM.value = new
                                        },
                                        abrirTimePicker = { valorActual, onSelect ->
                                            abrirTimePicker(context, valorActual, onSelect)
                                        }
                                    )

                                    texto_generico_one_line(" a ")

                                    campoHora(
                                        valor = item.h2PM.value,
                                        etiqueta = "Cierre PM",
                                        onHoraSeleccionada = { new ->
                                            item.h2PM.value = new
                                        },
                                        abrirTimePicker = { valorActual, onSelect ->
                                            abrirTimePicker(context, valorActual, onSelect)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun RowScope.campoHora(
    valor: String,
    etiqueta: String,
    onHoraSeleccionada: (String) -> Unit,
    abrirTimePicker: (String, (String) -> Unit) -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clickable { abrirTimePicker(valor, onHoraSeleccionada) }
    ) {
        OutlinedTextField(
            value = valor,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { texto_generico_one_line(etiqueta) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )
    }
}


fun abrirTimePicker(
    context: Context,
    valorActual: String,
    onSelect: (String) -> Unit
) {
    val parts = valorActual.split(":")
    val horaInicial = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutoInicial = parts.getOrNull(1)?.toIntOrNull() ?: 0

    TimePickerDialog(
        context,
        { _, hour: Int, minute: Int ->
            val resultado = "%02d:%02d".format(hour, minute)
            onSelect(resultado)
        },
        horaInicial,
        minutoInicial,
        true
    ).show()
}


fun timeStampNumero(): String {
    return System.currentTimeMillis().toString()
}

@Composable
fun valor_txt_contacto(
    tipo: String,
    valor: String,
    valor_retorno: (String) -> Unit
) {
    val txt = remember(tipo) {

        when (tipo.lowercase()) {

            "whatsapp", "telefono", "yape", "plin" ->
                "Número de $tipo"

            "sitio web" ->
                "Nombre del sitio web"

            else ->
                "Usuario de $tipo"
        }
    }

    val keyboardType = remember(tipo) {
        if (tipo.equals("whatsapp", ignoreCase = true) ||
            tipo.equals("telefono", ignoreCase = true) || tipo.equals(
                "yape",
                ignoreCase = true
            ) || tipo.equals("plin", ignoreCase = true)
        ) {
            KeyboardType.Phone
        } else {
            KeyboardType.Text
        }
    }

    OutlinedTextField(
        value = valor,
        onValueChange = { valor_retorno(it) },
        label = { texto_generico_one_line(txt) },
        placeholder = {
            texto_generico_one_line(
                txt,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            focusedPlaceholderColor = Color.Gray,
            unfocusedPlaceholderColor = Color.Gray,
        )
    )
}

fun generarIdSeguro(): String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    val random = SecureRandom()
    val idLength = 20

    val sb = StringBuilder(idLength)
    repeat(idLength) {
        sb.append(chars[random.nextInt(chars.length)])
    }
    return sb.toString()
}

@Composable
fun ChipsCategoriasCheck(
    lista: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by rememberSaveable {
        mutableStateOf(listOf<String>())
    }
    spacer_vertical(5.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->

            val isSelected = item in seleccionados

            Box(
                modifier = Modifier
                    .clip(CircleShape)

                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        seleccionados = if (isSelected)
                            seleccionados - item
                        else
                            seleccionados + item

                        lista_select(seleccionados)
                    }

            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = {
                            seleccionados = if (isSelected)
                                seleccionados - item
                            else
                                seleccionados + item

                            lista_select(seleccionados)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,   // Check marcado
                            uncheckedColor = Color.White,                      // Check desmarcado
                            checkmarkColor = Color.White                       // ✔ icon
                        )
                    )

                    texto_generico_one_line(
                        texto = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }

    spacer_vertical(3.dp)
}


@Composable
fun chips_categorias(
    lista: List<String>,
    lista_select: (List<String>) -> Unit
) {
    var seleccionados by rememberSaveable { mutableStateOf(listOf<String>()) }

    spacer_vertical(10.dp)

    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(lista) { item ->

            val isSelected = seleccionados.contains(item)

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected)
                            Color.White
                        else
                            MaterialTheme.colorScheme.primary
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        seleccionados =
                            if (isSelected)
                                seleccionados - item
                            else
                                seleccionados + item

                        lista_select(seleccionados)
                    }, contentAlignment = Alignment.Center
            ) {
                texto_generico_one_line(
                    item,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
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


data class HorasDia(
    var h1AM: MutableState<String> = mutableStateOf(""),
    var h2AM: MutableState<String> = mutableStateOf(""),
    var h1PM: MutableState<String> = mutableStateOf(""),
    var h2PM: MutableState<String> = mutableStateOf(""),
    var cerrado: MutableState<Boolean> = mutableStateOf(false),
    var solo_horario: MutableState<Boolean> = mutableStateOf(false)
)


