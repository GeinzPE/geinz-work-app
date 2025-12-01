package com.geinzz.geinzwork.utils.constantes.localizate_geinz

import android.R
import android.app.TimePickerDialog
import android.content.Context
import android.location.Location
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import java.time.Duration
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioAtencion_box
import com.geinzz.geinzwork.data.model.localizate_geinz.HorarioBloque
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
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object constantes_horas {

    val motivos = listOf(
        "Mantenimiento",
        "Renovación",
        "Inventario",
        "Cierre",
        "Emergencia",
        "Limpieza",
        "Clausura",
        "No disponible",
        "Descanso",
    )

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
        hAperturaAM: String = "",
        hCierreAM: String = "",
        hAperturaPM: String = "",
        hCierrePM: String = ""
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


    fun convertir_timesTAmp_fecha(timestampparams:String):String{
    val timestamp = timestampparams.toLong()
    val date = Date(timestamp)

        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return  formato.format(date)
    }


    fun DiaHoy(): String {
        val localeEs = Locale("es", "ES")

        val dia = LocalDate.now().dayOfWeek.getDisplayName(TextStyle.FULL, localeEs)

        return dia.replaceFirstChar { it.titlecase(localeEs) }

    }


    fun obtenerDiasYColor(fechaFin: String): Pair<Long, Color> {
        return try {
            val formato = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val hoy = LocalDate.now()
            val fin = LocalDate.parse(fechaFin, formato)

            var diasRestantes = ChronoUnit.DAYS.between(hoy, fin)

            // 🔥 Evitar negativos
            if (diasRestantes < 0) diasRestantes = 0

            val color = when {
                diasRestantes >= 10 -> Color(0xFF00FF0C)   // Verde
                diasRestantes in 5..9 -> Color(0xFFFFC107) // Amarillo
                diasRestantes in 0..2 -> Color(0xFFFF0F00) // Rojo
                else -> Color.Gray
            }

            Pair(diasRestantes, color)

        } catch (e: Exception) {
            Pair(0, Color.Gray)
        }
    }



    fun calcularHorasDiaLegible(horarioDia: HorarioDia_box): String {
        if (horarioDia.cerrado || horarioDia.bloques.isEmpty()) return "0h 0m"

        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        var totalMinutos = 0L

        for (bloque in horarioDia.bloques) {
            val apertura = LocalTime.parse(bloque.h_apertura, formatter)
            val cierre = LocalTime.parse(bloque.h_cierre, formatter)
            val duracion = Duration.between(apertura, cierre).toMinutes()
            totalMinutos += duracion
        }

        val horas = totalMinutos / 60
        val minutos = totalMinutos % 60

        return "${horas}h ${minutos}m"
    }

    fun obtenerBloquesDeHoy(
        diaHoy: String,
        horarioMap: HorarioAtencion_box
    ): List<HorarioBloque> {
        return when (diaHoy.lowercase()) {
            "lunes" -> horarioMap.lunes.bloques
            "martes" -> horarioMap.martes.bloques
             "miércoles" -> horarioMap.miércoles.bloques
            "jueves" -> horarioMap.jueves.bloques
            "viernes" -> horarioMap.viernes.bloques
             "sábado" -> horarioMap.sábado.bloques
            "domingo" -> horarioMap.domingo.bloques
            else -> emptyList()
        }
    }



    fun abreviarNumero(num: Long): String {
        return try {
            if (num < 1000) return num.toString()

            val unidades = listOf("K", "M", "B", "T")
            var valor = num.toDouble()
            var index = -1

            while (valor >= 1000 && index < unidades.size - 1) {
                valor /= 1000
                index++
            }

            // Redondeo a 2 decimales sin ceros innecesarios
            val valorStr = when {
                valor >= 100 -> String.format("%.0f", valor)     // ej. 150K → "150K"
                valor >= 10  -> String.format("%.1f", valor)     // ej. 12.3K
                else         -> String.format("%.2f", valor)     // ej. 1.23K
            }.trimEnd('0').trimEnd('.')  // Elimina ".0"

            valorStr + unidades[index]
        } catch (e: Exception) {
            // En caso de error, devuelve el número original como string
            num.toString()
        }
    }



    fun convertir24a12(hora24: String): String {
        return try {
            val formato24 = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val formato12 = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

            val date = formato24.parse(hora24)
            formato12.format(date!!).uppercase() // Para AM / PM
        } catch (e: Exception) {
            "Hora inválida"
        }
    }


    @Composable
    fun HorarioSemanal123(
        filtrado:String= "todos",
        id_tienda: String,
        tick: Long,
        viewModelFiltros: viewModel_filtado_tiendas,
        isConnected: Boolean,
        horario: HorarioAtencion_box,
        cerrar_tienda: (nombre_dia: String, motivo_cierre: String, List<Map<String, String>>) -> Unit,
        abrir_tienda: (nombre_dia: String, List<Map<String, String>>) -> Unit,
        error_sin_internet: () -> Unit,
        onclick_expand: () -> Unit,
        error_campos_incompletos: () -> Unit,
        error_horas_invalidas: (String) -> Unit,
        color_left:List<Color>,
        color_right: List<Color>

    ) {
        Log.d("datos_teinda_ente","${horario.sábado.cerrado.toString()}")

        val DELAY_REBOTE_UI_MS = 1000L

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
        var seleciondao by remember { mutableStateOf(filtrado) }

        val listState = rememberLazyListState()

        val diasFiltrados = when (seleciondao) {
            "hoy" -> diasConDatos.filter { it.first == DiaHoy() }

            "dias abiertos" -> {
                diasConDatos.filter { !it.second.cerrado }
            }

            "dias cerrados" -> diasConDatos.filter { it.second.cerrado }

            else -> diasConDatos
        }

        Log.d("diasFiltrados","${diasFiltrados.toString()}")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 5.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
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
                }, color_left = color_left, color_right = color_right
            )

            diasFiltrados.forEach { (nombreDia, datosDia) ->

                val bloqueManana = datosDia.bloques.getOrNull(0)
                val bloqueTarde = datosDia.bloques.getOrNull(1)
                val trabajoCorrido = bloqueTarde == null

                var dia_enable by remember(nombreDia, datosDia.cerrado) {
                    mutableStateOf(datosDia.cerrado)
                }
                var motivo_cierre_tienda by remember { mutableStateOf(datosDia.motivo) }

                var expndir_todo by remember(nombreDia) { mutableStateOf(false) }

                var btn_guardado_abierto_oculto by remember(nombreDia) { mutableStateOf(false) }
                var btn_guardado_cerrado_oculto by remember(nombreDia) { mutableStateOf(false) }

                val hAperturaAM =
                    remember(nombreDia) { mutableStateOf(bloqueManana?.h_apertura ?: "") }
                val hCierreAM = remember(nombreDia) { mutableStateOf(bloqueManana?.h_cierre ?: "") }

                val hAperturaPM =
                    remember(nombreDia) { mutableStateOf(bloqueTarde?.h_apertura ?: "") }
                val hCierrePM = remember(nombreDia) { mutableStateOf(bloqueTarde?.h_cierre ?: "") }

                val abiertoAM_casteado_12h =convertir24a12(hAperturaAM.value)
                val cierreAM_casteado_12h =convertir24a12(hCierreAM.value)
                val aperturaPM_casteado12h =convertir24a12(hAperturaPM.value)
                val cierrePM_casteado12h =convertir24a12(hCierrePM.value)




                var corrido by remember(nombreDia) { mutableStateOf(trabajoCorrido) }

                var motivo_cierre by remember(nombreDia) { mutableStateOf(motivo_cierre_tienda) }

                var hubo_cambios by remember(nombreDia) { mutableStateOf(false) }

                var cambios_tiene_horario_activarlo by remember(nombreDia) { mutableStateOf(false) }
                var cambios_tiene_horario_cerrarlo by remember(nombreDia) { mutableStateOf(false) }

                var leersolo_si_no_fue by remember(nombreDia) { mutableStateOf(false) }

                var mostar_conversion_real_time by remember { mutableStateOf(false) }

                val cambioSwitchYTrabajo = remember(dia_enable, corrido) {
                    mutableStateOf(!dia_enable || !corrido)  // true si abierto + trabajo con descanso
                }
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
                                        cambioSwitchYTrabajo.value = true

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
                                                        modifier = Modifier.clickable (indication = null,interactionSource = remember { MutableInteractionSource()}) {
                                                            motivo_cierre =
                                                                if (motivo_cierre == motivo) "" else motivo
                                                        })
                                                }
                                            }
                                        }


                                        if (!btn_guardado_cerrado_oculto && ((motivo_cierre.isNotEmpty() && motivo_cierre != motivo_cierre_tienda) || cambios_tiene_horario_cerrarlo)
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
                                        val initCorrido = remember(nombreDia) { corrido }
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
                                            hCierrePM.value,
                                        ) {
                                            val cambio =
                                                corrido != initCorrido ||
                                                        hAperturaAM.value != initHApAM ||
                                                        hCierreAM.value != initHCiAM ||
                                                        hAperturaPM.value != initHApPM ||
                                                        hCierrePM.value != initHCiPM

                                            hubo_cambios = cambio
                                            cambios_tiene_horario_activarlo = cambio
                                        }
                                        Text(
                                            text = if(!mostar_conversion_real_time)"Mostrar conversion a 12 h" else "Ocultar conversion a 12 h",
                                            style = LocalTextStyle.current.copy(
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                textDecoration = TextDecoration.Underline
                                            ), modifier = Modifier.clickable (indication = null,interactionSource = remember { MutableInteractionSource()}) {
                                                mostar_conversion_real_time = !mostar_conversion_real_time
                                            }
                                        )

                                        // --- Trabajo de corrido / descanso ---
                                        LazyRow {

                                            item {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Checkbox(
                                                        checked = corrido, onCheckedChange = {
                                                            corrido = true
                                                            hubo_cambios = true
                                                            cambios_tiene_horario_activarlo = true
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
                                                            corrido = false
                                                            hubo_cambios = true
                                                            cambios_tiene_horario_activarlo = true
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
                                                etiqueta = "Apertura 1",
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
                                                etiqueta = if (corrido) "Cierre 1" else "Cierre 1",
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
                                                    etiqueta = "Apertura 2",
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
                                                    etiqueta = "Cierre 2",
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

                                        spacer_vertical(5.dp)
                                        AnimatedVisibility(mostar_conversion_real_time, modifier = Modifier.clip(
                                            RoundedCornerShape(20.dp)).background(if(DiaHoy()==nombreDia){
                                                MaterialTheme.colorScheme.surface
                                        }else{
                                            MaterialTheme.colorScheme.surfaceVariant
                                        })) {
                                            Column(
                                                modifier = Modifier.padding(10.dp).animateContentSize(),
                                                verticalArrangement = Arrangement.spacedBy(7.dp)
                                            ) {
                                                texto_generico_one_line("Conversión automática a 12 h")
                                                texto_generico_multilinea(
                                                    "Geinz convierte tu horario en tiempo real al formato de 12 horas para facilitar la lectura.",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                spacer_vertical(2.dp)

                                                if (corrido) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            10.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        bloques_foramtos_horario(
                                                            "Apertura",
                                                            abiertoAM_casteado_12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        texto_generico_one_line(" a ")
                                                        bloques_foramtos_horario(
                                                            "Cierre",
                                                            cierreAM_casteado_12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                } else {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            10.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        bloques_foramtos_horario(
                                                            "Apertura",
                                                            abiertoAM_casteado_12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        texto_generico_one_line(" a ")
                                                        bloques_foramtos_horario(
                                                            "Cierre ",
                                                            cierreAM_casteado_12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    spacer_vertical(2.dp)
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(
                                                            10.dp
                                                        ),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        bloques_foramtos_horario(
                                                            "Apertura",
                                                            aperturaPM_casteado12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                        texto_generico_one_line(" a ")
                                                        bloques_foramtos_horario(
                                                            "Cierre",
                                                            cierrePM_casteado12h,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }

                                        }




                                        Log.d(
                                            "valores_establecidos",
                                            "${!btn_guardado_abierto_oculto} ${hubo_cambios} $cambios_tiene_horario_activarlo"
                                        )

                                        if (!btn_guardado_abierto_oculto &&
                                            (hubo_cambios || cambios_tiene_horario_activarlo || cambioSwitchYTrabajo.value)
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
                                                                val bloque = if (corrido) {
                                                                    construirBloques(
                                                                        hAperturaAM.value,
                                                                        hCierreAM.value
                                                                    )
                                                                } else {
                                                                    construirBloques(
                                                                        hAperturaAM.value,
                                                                        hCierreAM.value,
                                                                        hAperturaPM.value,
                                                                        hCierrePM.value
                                                                    )
                                                                }

                                                                if (isConnected) {
                                                                    val camposCompletos =
                                                                        if (corrido) {
                                                                            hAperturaAM.value.isNotEmpty() && hCierreAM.value.isNotEmpty()
                                                                        } else {
                                                                            hAperturaAM.value.isNotEmpty() &&
                                                                                    hCierreAM.value.isNotEmpty() &&
                                                                                    hAperturaPM.value.isNotEmpty() &&
                                                                                    hCierrePM.value.isNotEmpty()
                                                                        }

// VALIDACIÓN DE HORAS CON MENSAJES DIFERENCIADOS
                                                                    var mensajeError: String? = null

                                                                    try {
                                                                        if (corrido) {
                                                                            // Validación AM
                                                                            if (!horaValida(
                                                                                    hAperturaAM.value,
                                                                                    hCierreAM.value
                                                                                )
                                                                            ) {
                                                                                mensajeError =
                                                                                    "La hora de cierre AM no puede ser menor o igual a la hora de apertura."
                                                                            }
                                                                        } else {
                                                                            // Validación AM
                                                                            if (!horaValida(
                                                                                    hAperturaAM.value,
                                                                                    hCierreAM.value
                                                                                )
                                                                            ) {
                                                                                mensajeError =
                                                                                    "En el bloque AM, la hora de cierre debe ser mayor que la de apertura."
                                                                            }

                                                                            // Validación PM
                                                                            else if (!horaValida(
                                                                                    hAperturaPM.value,
                                                                                    hCierrePM.value
                                                                                )
                                                                            ) {
                                                                                mensajeError =
                                                                                    "En el bloque PM, la hora de cierre debe ser mayor que la de apertura."
                                                                            }
                                                                        }
                                                                    } catch (e: Exception) {
                                                                        mensajeError =
                                                                            "Formato de hora inválido."
                                                                    }


// ORDEN DE LOS MENSAJES
                                                                    if (!camposCompletos) {
                                                                        error_campos_incompletos()

                                                                    } else if (mensajeError != null) {
                                                                        error_horas_invalidas(
                                                                            mensajeError
                                                                        )

                                                                    } else {
                                                                        motivo_cierre = ""
                                                                        motivo_cierre_tienda = ""
                                                                        mostar_conversion_real_time=false
                                                                        abrir_tienda(
                                                                            nombreDia,
                                                                            bloque
                                                                        )
                                                                        btn_guardado_abierto_oculto =
                                                                            false
                                                                        expndir_todo = false
                                                                        leersolo_si_no_fue = true
                                                                    }


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

    fun horaValida(hInicio: String, hFin: String): Boolean {
        return try {
            val formato = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            val inicio = java.time.LocalTime.parse(hInicio, formato)
            val fin = java.time.LocalTime.parse(hFin, formato)
            fin.isAfter(inicio)
        } catch (e: Exception) {
            false // si no se puede parsear, se considera inválido
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

    @Composable
    fun bloques_foramtos_horario(etiqueta: String, horario_convertido: String, modifier: Modifier) {
        OutlinedTextField(
            value = horario_convertido,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { texto_generico_one_line(etiqueta, style = MaterialTheme.typography.bodyMedium) },
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp)
        )

    }

}

