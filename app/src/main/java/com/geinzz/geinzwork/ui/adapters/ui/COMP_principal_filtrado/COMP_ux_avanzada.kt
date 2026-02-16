package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay


@Composable
fun TypewriterClickableText(
    text: String,
    modifier: Modifier = Modifier,
    speed: Long = 50L,
    onClickRuta: () -> Unit = {},
    onClickTelefono: (numero: String, tipo: String) -> Unit = { _, _ -> }
) {
    var displayText by remember { mutableStateOf("") }

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
                    color = Color(0xFF49078D),
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
        // Busca “llama al” o “WhatsApp al” antes del número
        val regexNumero = """(\(\d{2,3}\)\s*|\d\s*){7,9}""".toRegex()
        regexNumero.findAll(displayText).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1
            val numeroLimpio = matchResult.value.replace("""[\s\(\)]""".toRegex(), "")

            // Revisar el contexto antes del número
            val textoAntes = displayText.substring(0, start).takeLast(20) // los 20 caracteres antes
            val tipo = when {
                textoAntes.contains("WhatsApp", ignoreCase = true) -> "WHATSAPP"
                textoAntes.contains("llama", ignoreCase = true) -> "LLAMADA"
                else -> "DESCONOCIDO"
            }

            // Solo marcar si es llamada o WhatsApp
            if (tipo != "DESCONOCIDO") {
                addStyle(
                    style = SpanStyle(
                        color = Color(0xFF49078D),
                        textDecoration = TextDecoration.Underline
                    ),
                    start = start,
                    end = end
                )
                addStringAnnotation(
                    tag = tipo,
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
                    "RUTA" -> onClickRuta()
                    "LLAMADA" -> onClickTelefono(annotation.item, "LLAMADA")
                    "WHATSAPP" -> onClickTelefono(annotation.item, "WHATSAPP")
                }
            }
        }
    )
}




