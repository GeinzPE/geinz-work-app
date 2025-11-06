package com.geinzz.geinzwork.ui.adapters.ui.dialog_general


import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.btn_aceptar_etc_dialog_general
import com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado.texto_generico_multilinea

@Composable
fun dialog_verificacion_proceso(ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                ondimis()
            }
        },
        dismissButton = {

        },
        title = {},
        text = {
            Column() {
                texto_generico_multilinea(
                    "Proceso de verificacion",
                    style = MaterialTheme.typography.titleLarge
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "Tu reseña pasará por un proceso de verificación para garantizar su autenticidad. Si te encuentras en el lugar al momento de dejarla, Geinz confirmará tu ubicación mediante el GPS y marcará tu reseña como *verificada*.Si no estás en el lugar, igual será publicada, pero sin la etiqueta de verificada.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },

        )
}

@Composable
fun dialog_normas_de_verificacion(ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                ondimis()
            }
        },
        dismissButton = {

        },
        title = {},
        text = {
            Column() {
                texto_generico_multilinea(
                    "Normas de verificación de reseñas",
                    style = MaterialTheme.typography.titleLarge
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "El proceso de verificación en Geinz busca garantizar que las reseñas sean auténticas y útiles para todos.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "• Las reseñas marcadas como verificadas provienen de usuarios que se encuentran en el lugar, confirmado mediante su ubicación GPS.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "• Se espera que el comentario sea respetuoso, honesto y directamente relacionado con la experiencia en el negocio.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "• Las reseñas que no cumplan con estas normas seguirán siendo publicadas, pero no recibirán el distintivo de verificación.",
                    style = MaterialTheme.typography.bodyMedium
                )
                spacer_vertical(5.dp)
                texto_generico_multilinea(
                    "Gracias por ayudar a mantener la comunidad de Geinz confiable y transparente.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },

        )
}


@Composable
fun dialog_verificada_automatico(ondimis: () -> Unit) {
    AlertDialog(
        onDismissRequest = { ondimis() },
        confirmButton = {
            btn_aceptar_etc_dialog_general {
                ondimis()
            }
        },
        dismissButton = {},
        title = {},
        text = {
            Column() {
                texto_generico_multilinea(
                    "Verificación automática",
                    style = MaterialTheme.typography.titleLarge
                )
                spacer_vertical(10.dp)
                texto_generico_multilinea(
                    "Cuando escaneas el código QR del negocio mientras te encuentras físicamente en el lugar .Geinz verifica automáticamente tu ubicación mediante el GPS de tu dispositivo.Esto permite que tu reseña sea marcada como 'verificada automáticamente',De esta forma, ayudamos a mantener reseñas auténticas y de confianza dentro de Geinz.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },

        )
}