package com.geinzz.geinzwork.ui.adapters.ui.pantallas

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Looper
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults

import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.geinzz.geinzwork.data.model.dataclass_repo_agregar_datos
import com.geinzz.geinzwork.model.repo_agregar_datos
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng


@Composable
fun datos_teindas() {

    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    var texto_nombre_lugar by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var numero_telefoono by remember { mutableStateOf("") }
    Column() {
        OutlinedTextField(
            value = texto_nombre_lugar,
            onValueChange = { it ->
                texto_nombre_lugar = it
            },
            label = { texto_generico_one_line("nombre") },
            placeholder = { texto_generico_one_line("nombre") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
        )
        spacer_vertical(20.dp)

        OutlinedTextField(
            value = numero_telefoono,
            onValueChange = { it ->
                numero_telefoono = it
            },
            label = { texto_generico_one_line("numero de celular") },
            placeholder = { texto_generico_one_line("numero de calular") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number


            )
        )
        spacer_vertical(20.dp)
        OutlinedTextField(
            value = latitud,
            onValueChange = { it ->
                latitud = it
            },
            label = { texto_generico_one_line("Latitud") },
            placeholder = { texto_generico_one_line("Latitud") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedBorderColor = MaterialTheme.colorScheme.onBackground,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary
            ),readOnly = true
        )

        spacer_vertical(20.dp)
        OutlinedTextField(
            value = longitud,
            onValueChange = { it ->
                longitud = it
            },
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

        Button(onClick = {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                obtenerLatLogNoComposable(fusedLocationClient) { ubicacion ->
                    if (ubicacion != null) {
                        latitud = ubicacion.latitude.toString()
                        longitud = ubicacion.longitude.toString()
                    } else {
                        latitud = "No disponible"
                        longitud = "No disponible"
                    }
                }
            } else {
                Toast.makeText(context, "Activa el permiso de ubicación", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Obtener lat/log", color = Color.White)
        }

        spacer_vertical(20.dp)

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
