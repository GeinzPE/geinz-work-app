package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.R
import android.app.TimePickerDialog
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.campoHora
import com.geinzz.geinzwork.viewModels.viewmodel_agregar_datos
import com.google.firebase.firestore.FirebaseFirestore
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object constantes_horas {
    fun obtenerProximoDiaAbierto(
        horario: Map<String, Any>,
        diaActual: String
    ): Pair<String, Map<String, Any>>? {
        val dias = listOf("domingo", "lunes", "martes", "miércoles", "jueves", "viernes", "sábado")
        val indiceActual = dias.indexOf(diaActual)

        // Recorremos desde el siguiente día hasta completar la semana
        for (i in 1..7) {
            val indice = (indiceActual + i) % 7
            val dia = dias[indice]
            val horarioDia = horario[dia] as? Map<String, Any> ?: continue

            val cerrado = horarioDia["cerrado"] as? Boolean ?: true
            if (!cerrado) {
                // Día abierto encontrado ✅
                return Pair(dia, horarioDia)
            }
        }

        // Si no encuentra ningún día abierto
        return null
    }

    fun calcularDistanciaKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val resultados = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultados)
        return resultados[0] / 1000.0 // Pasa de metros a kilómetros
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fechaActual(): String {
        val hoy = LocalDate.now()
        return hoy.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun fechaUnaSemanaDespues(): String {
        val fecha = LocalDate.now().plusWeeks(1)
        return fecha.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun horaActual(): String {
        val hora = LocalTime.now()
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"))
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

    fun guardar_horario_cerrado(
        id_tienda: String,
        dia: String,
        motivo: String
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "cerrado" to true,
            "motivo" to motivo
        )

        db.update("horario_atencion.${dia.lowercase()}", dataDia)
            .addOnSuccessListener { Log.d("DB", "Horario de $dia actualizado!") }
            .addOnFailureListener { Log.e("DB", "Error", it) }
    }

    fun guardar_horario_atencion_abierto(
        id_tienda: String,
        dia: String,
        bloques: List<Map<String, String>>
    ) {
        val db = FirebaseFirestore.getInstance()
            .collection("Tiendas").document("barranca")
            .collection("barranca").document(id_tienda)

        val dataDia = mapOf(
            "bloques" to bloques,
            "cerrado" to false,
            "motivo" to ""
        )
        db.update("horario_atencion.${dia.lowercase()}", dataDia)
            .addOnSuccessListener {
                Log.d("DB", "Horario de $dia actualizado correctamente")
            }
            .addOnFailureListener {
                Log.e("DB", "Error al actualizar horario de $dia", it)
            }
    }

    fun construirBloques(
        hAperturaAM: String,
        hCierreAM: String,
        hAperturaPM: String,
        hCierrePM: String
    ): List<Map<String, String>> {

        val bloques = mutableListOf<Map<String, String>>()

        // Bloque AM
        if (hAperturaAM.isNotEmpty() && hCierreAM.isNotEmpty()) {
            bloques.add(
                mapOf(
                    "h_apertura" to hAperturaAM,
                    "h_cierre" to hCierreAM,
                )
            )
        }

        // Bloque PM
        if (hAperturaPM.isNotEmpty() && hCierrePM.isNotEmpty()) {
            bloques.add(
                mapOf(
                    "h_apertura" to hAperturaPM,
                    "h_cierre" to hCierrePM,
                )
            )
        }

        return bloques
    }


    @Composable
    fun HorarioSemanal123(
        horario: HorarioAtencion_box,
        cerrar_tienda: (nombre_dia: String, motivo_cierre: String) -> Unit,
        abrir_tienda: (nombre_dia: String, List<Map<String, String>>) -> Unit
    ) {
        val context = LocalContext.current

        val motivos = listOf(
            "Mantenimiento",
            "Renovación",
            "Inventario",
            "Capacitación del personal",
            "Cierre temporal",
            "Emergencia",
            "Limpieza",
            "Clausura",
            "No disponible"
        )
        val diasConDatos = listOf(
            "Lunes" to horario.lunes,
            "Martes" to horario.martes,
            "Miércoles" to horario.miércoles,
            "Jueves" to horario.jueves,
            "Viernes" to horario.viernes,
            "Sábado" to horario.sábado,
            "Domingo" to horario.domingo
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()

                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            diasConDatos.forEach { (nombreDia, datosDia) ->

                val bloqueManana = datosDia.bloques.getOrNull(0)
                val bloqueTarde = datosDia.bloques.getOrNull(1)
                val trabajoCorrido = bloqueTarde == null

                var dia_enable by remember { mutableStateOf(datosDia.cerrado) }
                var motivo_cierre_tienda by remember { mutableStateOf(datosDia.motivo) }
                var corrido by remember { mutableStateOf(trabajoCorrido) }

                // Bloque Mañana
                val hAperturaAM = remember { mutableStateOf(bloqueManana?.h_apertura ?: "") }
                val hCierreAM = remember { mutableStateOf(bloqueManana?.h_cierre ?: "") }


                val hAperturaPM = remember { mutableStateOf(bloqueTarde?.h_apertura ?: "") }
                val hCierrePM = remember { mutableStateOf(bloqueTarde?.h_cierre ?: "") }


                var hubo_cambios by remember { mutableStateOf(false) }

                var expndir_todo by remember { mutableStateOf(false) }

                var motivo_cierre by remember { mutableStateOf(motivo_cierre_tienda) }     // lo editable



                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        // --- Encabezado Día y Switch Abierto/Cerrado ---
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                texto_generico_one_line(
                                    nombreDia,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }) {
                                            expndir_todo = !expndir_todo
                                        })
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (!dia_enable) "Abierto" else "Cerrado")
                                spacer_horizonta(5.dp)
                                Switch(
                                    checked = !dia_enable,
                                    onCheckedChange = { value ->
                                        dia_enable = !value
                                        expndir_todo = if (value) {
                                            true
                                        } else {
                                            true
                                        }

                                    }, colors = SwitchDefaults.colors(
                                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(
                                            alpha = 0.5f
                                        ),
                                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                        uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(
                                            alpha = 0.3f
                                        )
                                    )
                                )
                            }
                        }

                        AnimatedVisibility(expndir_todo) {
                            Crossfade(
                                targetState = dia_enable, animationSpec = tween(
                                    durationMillis = 300, // mismo tiempo que animateContentSize
                                    easing = FastOutSlowInEasing
                                )
                            ) { abierto ->
                                if (abierto) {
                                    // Contenido cuando el día está cerrado (mostrar motivos)
                                    Column() {
                                        texto_generico_one_line(
                                            "Selecciona tu motivo de cierre",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        LazyRow(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center,
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            items(motivos) { motivo ->
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .padding(
                                                            vertical = 15.dp,
                                                            horizontal = 5.dp
                                                        )
                                                        .clickable {
                                                            motivo_cierre =
                                                                if (motivo_cierre == motivo) "" else motivo
                                                        }
                                                ) {
                                                    RadioButton(
                                                        selected = motivo_cierre == motivo,     // ← CORREGIDO
                                                        onClick = {
                                                            motivo_cierre =
                                                                if (motivo_cierre == motivo) "" else motivo
                                                        }, modifier = Modifier.size(20.dp)
                                                    )

                                                    Text(
                                                        text = motivo,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.clickable {
                                                            motivo_cierre =
                                                                if (motivo_cierre == motivo) "" else motivo
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Log.d(
                                            "dasadadada",
                                            "${motivo_cierre.isNotEmpty()}  $motivo_cierre  $motivo_cierre_tienda"
                                        )
                                        if (motivo_cierre.isNotEmpty() && motivo_cierre != motivo_cierre_tienda) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .clickable {

                                                            Toast.makeText(
                                                                context,
                                                                "guardmos en el dia de $nombreDia de cerrado",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                            cerrar_tienda(nombreDia, motivo_cierre)

                                                        }
                                                ) {
                                                    texto_generico_one_line(
                                                        "Guardar cambios",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(
                                                            horizontal = 10.dp,
                                                            vertical = 10.dp
                                                        )
                                                    )
                                                }
                                            }
                                        }

                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        val initCorrido = remember { corrido }
                                        val initHApAM = remember { hAperturaAM.value }
                                        val initHCiAM = remember { hCierreAM.value }
                                        val initHApPM = remember { hAperturaPM.value }
                                        val initHCiPM = remember { hCierrePM.value }

                                        val context = LocalContext.current

                                        LaunchedEffect(
                                            corrido,
                                            hAperturaAM.value,
                                            hCierreAM.value,
                                            hAperturaPM.value,
                                            hCierrePM.value
                                        ) {
                                            val cambio = corrido != initCorrido ||
                                                    hAperturaAM.value != initHApAM ||
                                                    hCierreAM.value != initHCiAM ||
                                                    hAperturaPM.value != initHApPM ||
                                                    hCierrePM.value != initHCiPM

                                            hubo_cambios = cambio


                                        }
                                        // --- Trabajo de corrido / descanso ---
                                        LazyRow {
                                            item {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = corrido,
                                                        onCheckedChange = {
                                                            corrido =
                                                                true   // si hace click → activar corrido
                                                        }, colors = CheckboxDefaults.colors(
                                                            checkedColor = MaterialTheme.colorScheme.primary,
                                                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.6f
                                                            ),
                                                            checkmarkColor = Color.White   // ← importante
                                                        )
                                                    )
                                                    texto_generico_one_line(
                                                        "Trabajo de corrido",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }

                                            item {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = !corrido,
                                                        onCheckedChange = {
                                                            corrido =
                                                                false  // si hace click → activar descanso
                                                        }, colors = CheckboxDefaults.colors(
                                                            checkedColor = MaterialTheme.colorScheme.primary,
                                                            uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(
                                                                alpha = 0.6f
                                                            ),
                                                            checkmarkColor = Color.White   // ← importante
                                                        )
                                                    )
                                                    texto_generico_one_line(
                                                        "Trabajo con descanso",
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )

                                                }
                                            }
                                        }

                                        // --- Bloque Mañana ---

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            campoHora(
                                                valor = hAperturaAM.value,
                                                etiqueta = "Apertura AM",
                                                onHoraSeleccionada = { new ->
                                                    hAperturaAM.value = new
                                                },
                                                abrirTimePicker = { valorActual, onSelect ->
                                                    abrirTimePicker(
                                                        context,
                                                        valorActual,
                                                        onSelect
                                                    )
                                                }
                                            )

                                            texto_generico_one_line(" a ")

                                            campoHora(
                                                valor = hCierreAM.value,
                                                etiqueta = if (corrido) "Cierre PM" else "Cierre AM",
                                                onHoraSeleccionada = { new ->
                                                    hCierreAM.value = new
                                                },
                                                abrirTimePicker = { valorActual, onSelect ->
                                                    abrirTimePicker(
                                                        context,
                                                        valorActual,
                                                        onSelect
                                                    )
                                                }
                                            )
                                        }
                                        if (!corrido) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {

                                                campoHora(
                                                    valor = hAperturaPM.value,
                                                    etiqueta = "Apertura PM",
                                                    onHoraSeleccionada = { new ->
                                                        hAperturaPM.value = new
                                                    },
                                                    abrirTimePicker = { valorActual, onSelect ->
                                                        abrirTimePicker(
                                                            context,
                                                            valorActual,
                                                            onSelect
                                                        )
                                                    }
                                                )

                                                texto_generico_one_line(" a ")

                                                campoHora(
                                                    valor = hCierrePM.value,
                                                    etiqueta = "Cierre PM",
                                                    onHoraSeleccionada = { new ->
                                                        hCierrePM.value = new
                                                    },
                                                    abrirTimePicker = { valorActual, onSelect ->
                                                        abrirTimePicker(
                                                            context,
                                                            valorActual,
                                                            onSelect
                                                        )
                                                    }
                                                )
                                            }
                                        }


                                        if (hubo_cambios) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                ) {
                                                    texto_generico_one_line(
                                                        "Guardar",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier
                                                            .padding(
                                                                horizontal = 10.dp,
                                                                vertical = 10.dp
                                                            )
                                                            .clickable {
                                                                val bloque = construirBloques(
                                                                    hAperturaAM.value,
                                                                    hCierreAM.value,
                                                                    hAperturaPM.value,
                                                                    hCierrePM.value
                                                                )
                                                                Toast.makeText(
                                                                    context,
                                                                    "guardmos en el dia de $nombreDia de abierto",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                abrir_tienda(nombreDia, bloque)
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
            }
        }
    }
}


