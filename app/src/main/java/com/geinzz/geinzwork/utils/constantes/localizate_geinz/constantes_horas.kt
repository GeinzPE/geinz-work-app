package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.app.TimePickerDialog
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.geinzz.geinzwork.data.model.localizate_geinz.filtrado_tiendas.HorarioDia_box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.retornar_color_estado_tienda_Box
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_one_line
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_horizonta
import com.geinzz.geinzwork.ui.adapters.ui.dialog_general.spacer_vertical
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.campoHora
import com.geinzz.geinzwork.ui.adapters.ui.pantallas.filtrado_tiendas.chips_filtrado
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_botonm_filtrado_v1
import com.geinzz.geinzwork.utils.constantes.localizate_geinz.constantes_lista_localidades.shadow_top_filtrado_v1
import com.geinzz.geinzwork.viewModels.viewModel_filtado_tiendas
import kotlinx.coroutines.delay
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object constantes_horas {
    fun obtenerProximoDiaAbierto(
        horario: Map<String, Any>, diaActual: String
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
        val fecha = LocalDate.now().plusWeeks(3)
        return fecha.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun horaActual(): String {
        val hora = LocalTime.now()
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    fun abrirTimePicker(

        context: Context, valorActual: String, onSelect: (String) -> Unit
    ) {
        val parts = valorActual.split(":")
        val horaInicial = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val minutoInicial = parts.getOrNull(1)?.toIntOrNull() ?: 0

        TimePickerDialog(
            context, { _, hour: Int, minute: Int ->
                val resultado = "%02d:%02d".format(hour, minute)
                onSelect(resultado)
            }, horaInicial, minutoInicial, true
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

    fun construirBloques(
        hAperturaAM: String, hCierreAM: String, hAperturaPM: String, hCierrePM: String
    ): List<Map<String, String>> {

        val bloques = mutableListOf<Map<String, String>>()


        if (hAperturaAM.isNotEmpty() && hCierreAM.isNotEmpty()) {
            bloques.add(
                mapOf(
                    "h_apertura" to hAperturaAM,
                    "h_cierre" to hCierreAM,
                )
            )
        }

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


    fun DiaHoy(): String {
        val localeEs = Locale("es", "ES")

        val dia = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, localeEs)

        return dia.replaceFirstChar { it.titlecase(localeEs) }

    }


    fun obtenerDiasYColor(fechaFin: String): Pair<Long, Color> {
        val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val hoy = LocalDate.now()
        val fin = LocalDate.parse(fechaFin, formato)

        val diasRestantes = ChronoUnit.DAYS.between(hoy, fin)

        val color = when {
            diasRestantes >= 10 -> Color(0xFF4CAF50)   // Verde
            diasRestantes in 5..9 -> Color(0xFFFFC107) // Amarillo
            diasRestantes in 0..2 -> Color(0xFFFF0F00) // Rojo
            diasRestantes < 0 -> Color.Gray            // Fecha ya pasó
            else -> Color.Gray
        }

        return Pair(diasRestantes, color)
    }

    @Composable
    fun HorarioSemanal123(
        id_tienda: String,
        tick: Long,
        viewModelFiltros: viewModel_filtado_tiendas,
        isConnected: Boolean,
        horario: HorarioAtencion_box,
        cerrar_tienda: (nombre_dia: String, motivo_cierre: String, List<Map<String, String>>) -> Unit,
        abrir_tienda: (nombre_dia: String, List<Map<String, String>>) -> Unit,
        error_sin_internet: () -> Unit,
        onclick_expand: () -> Unit
    ) {

        val DELAY_REBOTE_UI_MS = 1000L
        val motivos = listOf(
            "Mantenimiento",
            "Renovación",
            "Inventario",
            "Capacitación del personal",
            "Cierre",
            "Emergencia",
            "Limpieza",
            "Clausura",
            "No disponible",
            "Descanso",
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
        val lista_filtrado = listOf("hoy", "dias abiertos", "dias cerrados")
        var seleciondao by remember { mutableStateOf("Todos") }

        val listState = rememberLazyListState()

        val diasFiltrados = when (seleciondao) {
            "hoy" -> diasConDatos.filter { it.first == DiaHoy() }

            "dias abiertos" -> {
                diasConDatos.filter { !it.second.cerrado }
            }

            "dias cerrados" -> diasConDatos.filter { it.second.cerrado }

            else -> diasConDatos
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }){
                    onclick_expand()
                }) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    texto_generico_one_line(
                        "Horario semanal",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                spacer_vertical(7.dp)
                texto_generico_multilinea(
                    "Actualiza tu horario en tiempo real y GEINZ lo mostrará al instante.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(7.dp)
                Row() {
                    texto_generico_one_line(
                        "Horario de hoy : ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    retornar_color_estado_tienda_Box(
                        "",
                        viewModelFiltros.horariosTiendas.collectAsState().value[id_tienda]
                            ?: HorarioDia_box(),
                        tick,
                        true,
                        { color, txt -> }
                    )
                }
            }



            chips_filtrado(
                listState = listState,
                sub_categoria_selecionada = seleciondao,
                lista_subcategorias = lista_filtrado,
                expandir_carta = { expandir -> },
                selecionado = { categoria_selecionada ->
                    seleciondao = categoria_selecionada
                }, color_left = shadow_top_filtrado_v1, color_right = shadow_botonm_filtrado_v1
            )

            diasFiltrados.forEach { (nombreDia, datosDia) ->
                var expndir_todo by remember(seleciondao) { mutableStateOf(false) }
                var btn_guardado_abierto_oculto by remember { mutableStateOf(false) }
                var btn_guardado_cerrado_oculto by remember { mutableStateOf(false) }

                val bloqueManana = datosDia.bloques.getOrNull(0)
                val bloqueTarde = datosDia.bloques.getOrNull(1)
                val trabajoCorrido = bloqueTarde == null

                var dia_enable by remember(nombreDia, datosDia.cerrado) {
                    mutableStateOf(datosDia.cerrado)
                }
                var motivo_cierre_tienda by remember { mutableStateOf(datosDia.motivo) }

                var corrido by remember { mutableStateOf(trabajoCorrido) }

                // Bloque Mañana
                val hAperturaAM = remember { mutableStateOf(bloqueManana?.h_apertura ?: "") }
                val hCierreAM = remember { mutableStateOf(bloqueManana?.h_cierre ?: "") }


                val hAperturaPM = remember { mutableStateOf(bloqueTarde?.h_apertura ?: "") }
                val hCierrePM = remember { mutableStateOf(bloqueTarde?.h_cierre ?: "") }


                var hubo_cambios by remember { mutableStateOf(false) }


                var motivo_cierre by remember { mutableStateOf(motivo_cierre_tienda) }

                var cambios_tiene_horario_activarlo by remember { mutableStateOf(false) }
                var cambios_tiene_horario_cerrarlo by remember { mutableStateOf(false) }
                var leersolo_si_no_fue by remember { mutableStateOf(false) }

                LaunchedEffect(expndir_todo, leersolo_si_no_fue) {

                    if (!leersolo_si_no_fue) {
                        Log.d("cambiamosdadasd123", "${expndir_todo} $dia_enable")

                        if (!expndir_todo && !dia_enable) {
                            dia_enable = datosDia.cerrado
                        }

                        if (!expndir_todo && dia_enable) {
                            dia_enable = datosDia.cerrado
                        }
                    }

                    delay(DELAY_REBOTE_UI_MS)
                    leersolo_si_no_fue = false

                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(20.dp),
                    colors = if (nombreDia == DiaHoy()) {
                        CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
                    } else {
                        CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                    }
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
                            val tieneBloque =
                                (hAperturaAM.value.isNotEmpty() && hCierreAM.value.isNotEmpty()) ||
                                        (hAperturaPM.value.isNotEmpty() && hCierrePM.value.isNotEmpty())
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (!dia_enable) "Abierto" else "Cerrado")
                                spacer_horizonta(5.dp)
                                Switch(
                                    checked = !dia_enable, onCheckedChange = { value ->
                                        dia_enable = !value
                                        expndir_todo = true

                                        onSwitchChange(
                                            value = value,
                                            tieneBloque = tieneBloque,
                                            motivo_cierre = motivo_cierre,
                                            motivo_cierre_tienda = motivo_cierre_tienda,
                                            setActivar = { cambios_tiene_horario_activarlo = it },
                                            setCerrar = { cambios_tiene_horario_cerrarlo = it },
                                            setOcultarAbierto = {
                                                btn_guardado_abierto_oculto = it
                                            },
                                            setOcultarCerrado = {
                                                btn_guardado_cerrado_oculto = it
                                            },
                                            valor_seleciondao = { valor ->
//                                                motivo_cierre=valor
                                            }
                                        )


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
                                                            vertical = 15.dp, horizontal = 5.dp
                                                        )
                                                        .clickable {
                                                            motivo_cierre =
                                                                if (motivo_cierre == motivo) "" else motivo
                                                        }) {
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
                                                        })
                                                }
                                            }
                                        }


                                        if (!btn_guardado_cerrado_oculto &&
                                            ((motivo_cierre.isNotEmpty() && motivo_cierre != motivo_cierre_tienda)
                                                    || cambios_tiene_horario_cerrarlo)
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.BottomEnd
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.primary)
                                                        .clickable {
                                                            val bloque = construirBloques(
                                                                hAperturaAM.value,
                                                                hCierreAM.value,
                                                                hAperturaPM.value,
                                                                hCierrePM.value
                                                            )
                                                            if (isConnected) {
                                                                cerrar_tienda(
                                                                    nombreDia, motivo_cierre, bloque
                                                                )
                                                                btn_guardado_cerrado_oculto = true
                                                                expndir_todo = false
                                                                leersolo_si_no_fue = true
                                                            } else {
                                                                error_sin_internet()
                                                            }
                                                        }) {
                                                    texto_generico_one_line(
                                                        "Guardar cambios",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.padding(
                                                            horizontal = 10.dp, vertical = 10.dp
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
                                            val cambio =
                                                corrido != initCorrido || hAperturaAM.value != initHApAM || hCierreAM.value != initHCiAM || hAperturaPM.value != initHApPM || hCierrePM.value != initHCiPM

                                            hubo_cambios = cambio


                                        }
                                        // --- Trabajo de corrido / descanso ---
                                        LazyRow {
                                            item {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = corrido, onCheckedChange = {
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
                                                        checked = !corrido, onCheckedChange = {
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
                                                        context, valorActual, onSelect
                                                    )
                                                })

                                            texto_generico_one_line(" a ")

                                            campoHora(
                                                valor = hCierreAM.value,
                                                etiqueta = if (corrido) "Cierre PM" else "Cierre AM",
                                                onHoraSeleccionada = { new ->
                                                    hCierreAM.value = new
                                                },
                                                abrirTimePicker = { valorActual, onSelect ->
                                                    abrirTimePicker(
                                                        context, valorActual, onSelect
                                                    )
                                                })
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
                                                            context, valorActual, onSelect
                                                        )
                                                    })

                                                texto_generico_one_line(" a ")

                                                campoHora(
                                                    valor = hCierrePM.value,
                                                    etiqueta = "Cierre PM",
                                                    onHoraSeleccionada = { new ->
                                                        hCierrePM.value = new
                                                    },
                                                    abrirTimePicker = { valorActual, onSelect ->
                                                        abrirTimePicker(
                                                            context, valorActual, onSelect
                                                        )
                                                    })
                                            }
                                        }



                                        if (!btn_guardado_abierto_oculto &&
                                            (hubo_cambios || cambios_tiene_horario_activarlo)
                                        ) {

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
                                                                horizontal = 10.dp, vertical = 10.dp
                                                            )
                                                            .clickable {
                                                                val bloque = construirBloques(
                                                                    hAperturaAM.value,
                                                                    hCierreAM.value,
                                                                    hAperturaPM.value,
                                                                    hCierrePM.value
                                                                )
                                                                if (isConnected) {
                                                                    motivo_cierre = ""
                                                                    motivo_cierre_tienda = ""
                                                                    abrir_tienda(nombreDia, bloque)
                                                                    btn_guardado_abierto_oculto =
                                                                        true
                                                                    expndir_todo = false
                                                                    leersolo_si_no_fue = true
                                                                } else {
                                                                    error_sin_internet()
                                                                }
                                                            })
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

    fun onSwitchChange(
        value: Boolean,
        tieneBloque: Boolean,
        motivo_cierre: String,
        motivo_cierre_tienda: String,
        setActivar: (Boolean) -> Unit,
        setCerrar: (Boolean) -> Unit,
        setOcultarAbierto: (Boolean) -> Unit,
        setOcultarCerrado: (Boolean) -> Unit,
        valor_seleciondao: (String) -> Unit,
    ) {

        val abriendo = value       // true = abrir tienda
        val cerrando = !value      // false = cerrar tienda

        if (abriendo) {
            // Estamos abriendo la tienda
            setCerrar(false)

            if (tieneBloque) {
                setActivar(true)
            } else {
                setActivar(false)
            }

            setOcultarAbierto(false)
            setOcultarCerrado(true)
            valor_seleciondao("")
        } else {
            // Estamos cerrando la tienda
            setActivar(false)

            val debeGuardar = motivo_cierre.isNotEmpty() &&
                    motivo_cierre != motivo_cierre_tienda

            setCerrar(debeGuardar)

            setOcultarCerrado(false)
            setOcultarAbierto(true)
        }
    }

}

