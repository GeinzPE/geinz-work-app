package com.geinzz.geinzwork.ui.adapters.ui.COMP_principal_filtrado

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.geinzz.geinzwork.R

@Composable
fun icon_geinz_mas_fondo_violeta(size: Dp =40.dp){
    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8700F3).copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                    ),
                    shape = RoundedCornerShape(200.dp)
                )
        )
        Image(
            painter = painterResource(R.drawable.logo_geinz_blanco),
            contentDescription = "",
            modifier = Modifier.size(size)
        )
    }
}

@Composable
fun btn_aceptar_etc_dialog_general(
    color: Color = MaterialTheme.colorScheme.primary,
    txt_btn: String = "Aceptar",
    ondimis: () -> Unit,
) {
    Button(
        onClick = { ondimis() },
        colors = ButtonDefaults.buttonColors(
            containerColor = color,        // ⬅️ color del parámetro
            contentColor = Color.White     // opcional, para el texto
        )
    ) {
        texto_generico_one_line(
            txt_btn,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun btn_cerra_etc_dialog_general(txt_btn:String="cancelar",ondimis:()-> Unit){
    TextButton(onClick = { ondimis() }) {
        texto_generico_one_line(
            txt_btn,
            MaterialTheme.typography.bodyMedium
        )
    }
}