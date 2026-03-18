package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.viewModels.viewmode_seguridad_salud
import kotlinx.coroutines.delay


@Composable
fun TypewriterClickableText(
    viewmodelSeguridadSalud: viewmode_seguridad_salud,
    text: String,
    modifier: Modifier = Modifier,
    speed: Long = 50L,
    onClickRuta: (lat: Double,lng: Double) -> Unit ,
    onClickTelefono: (numero: String, tipo: String) -> Unit = { _, _ -> }
) {

    val datos_lista by viewmodelSeguridadSalud.datos_filtrado.collectAsState()
    var displayText by remember { mutableStateOf("") }

    var latituld_datos by remember { mutableStateOf(0.0) }
    var longitud_datos by remember { mutableStateOf(0.0) }

    LaunchedEffect(datos_lista) {
        // Logueamos los valores actuales
        Log.d("DEBUG", "Antes -> lat: $latituld_datos, lon: $longitud_datos")
        Log.d("DEBUG", "Datos_lista -> lat: ${datos_lista.latidud}, lon: ${datos_lista.longitud}")

        // Solo actualizamos si los datos no son 0
        if(datos_lista.latidud != 0.0 && datos_lista.longitud != 0.0){
            latituld_datos = datos_lista.latidud
            longitud_datos = datos_lista.longitud

            Log.d("DEBUG", "Actualizado -> lat: $latituld_datos, lon: $longitud_datos")
        }
    }


    // Efecto Typewriter
    LaunchedEffect(text) {
        displayText = ""
        for (i in text.indices) {
            displayText += text[i]
            delay(speed)
        }
    }

    val annotatedText = buildAnnotatedString {
        // Todo el texto base blanco
        append(displayText)

        // Subrayado + morado para "tocando aquí"
        val palabraRuta = "tocando aquí"
        val startRuta = displayText.indexOf(palabraRuta)
        if (startRuta >= 0) {
            addStyle(
                style = SpanStyle(
                    color = Color(0xFF874BD0),
                    textDecoration = TextDecoration.Underline
                ),
                start = startRuta,
                end = startRuta + palabraRuta.length
            )
            addStringAnnotation(
                tag = "RUTA",
                annotation = "RUTA",
                start = startRuta,
                end = startRuta + palabraRuta.length
            )
        }

        // Detectar números de teléfono en el texto basado en contexto
        val regexNumero = """(\(\d{2,3}\)\s*|\d\s*){7,9}""".toRegex()
        var ultimoTipo: String? = null

        regexNumero.findAll(displayText).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val numeroLimpio = matchResult.value.replace("""[\s\(\)]""".toRegex(), "")

            // Revisar contexto antes del número
            val textoAntes = displayText.substring(0, start).takeLast(20)
            val tipo = when {
                textoAntes.contains("WhatsApp", ignoreCase = true) -> "WHATSAPP"
                textoAntes.contains("llama", ignoreCase = true) -> "LLAMADA"
                textoAntes.contains("o al", ignoreCase = true) -> ultimoTipo // hereda del anterior
                else -> null
            }

            // Solo agregar si tipo no es null
            tipo?.let { t ->
                ultimoTipo = t

                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF874BD0),
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = t,
                    annotation = numeroLimpio,
                    start = start,
                    end = end
                )
            }
        }

    }


    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
        onClick = { offset ->
            annotatedText.getStringAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                when (annotation.tag) {
                    "RUTA" -> onClickRuta(latituld_datos,longitud_datos)
                    "LLAMADA" -> onClickTelefono(annotation.item, "LLAMADA")
                    "WHATSAPP" -> onClickTelefono(annotation.item, "WHATSAPP")
                }
            }
        }
    )
}


@Composable
fun TypewriterTexto(
    text: String,
    modifier: Modifier = Modifier,
    speed: Long = 70L
) {
    // rememberSaveable sobrevive al scroll, remember no
    var displayText by rememberSaveable(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        // Si ya está completo, no hace nada
        if (displayText == text) return@LaunchedEffect

        // Continúa desde donde quedó
        for (i in displayText.length until text.length) {
            displayText = text.substring(0, i + 1)
            delay(speed)
        }
    }

    Text(
        text = displayText,
        modifier = modifier.padding(vertical = 10.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White
    )
}





